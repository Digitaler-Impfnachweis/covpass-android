/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.DisplayMetrics
import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DgcEntryDetailFragment
import de.rki.covpass.app.detail.DgcEntryDetailViewModel
import de.rki.covpass.app.scanner.CovPassCertificateStorageHelper.addNewCertificate
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationListRepository
import de.rki.covpass.sdk.revocation.validateRevocation
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.utils.DataComparison
import de.rki.covpass.sdk.utils.DccNameMatchingUtils.compareHolder
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [DgcEntryDetailViewModel] to [DgcEntryDetailFragment].
 */
internal interface ImportCertificatesEvents : BaseEvents {
    fun noValidCertificatesFound()
    fun certificatesFound(list: List<ImportCovCertificate>)
    fun errorFailedToOpenFile()
    fun addCertificatesFinish()
}

internal class ImportCertificatesSelectorViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val revocationListRepository: RevocationListRepository = sdkDeps.revocationListRepository,
) : BaseReactiveState<ImportCertificatesEvents>(scope) {

    fun getQrCodes(fileDescriptor: ParcelFileDescriptor?, isPDF: Boolean, displayMetrics: DisplayMetrics) {
        launch {
            if (fileDescriptor != null) {
                if (isPDF) {
                    getQrCodesFromPdf(fileDescriptor, displayMetrics)
                } else {
                    getQrCodesFromImages(fileDescriptor)
                }
            } else {
                eventNotifier {
                    errorFailedToOpenFile()
                }
                throw FailedToOpenFileException()
            }
        }
    }

    fun addCertificates(list: List<ImportCovCertificate>) {
        launch {
            val listOfCertificatesDistinctHolders =
                certRepository.certs.value.certificates.map {
                    it.getMainCertificate().covCertificate
                }.toMutableList()

            list.filter { newCert ->
                !newCert.isCertificateHolderInList(listOfCertificatesDistinctHolders)
            }.map { it.covCertificate }.let { listOfCertificatesDistinctHolders.addAll(it) }

            if (listOfCertificatesDistinctHolders.size > MAX_NUMBER_OF_HOLDERS) {
                throw MaxNumberOfHolderExceededException()
            }
            list.forEach {
                addNewCertificate(certRepository.certs, it.covCertificate, it.qrContent)
            }
            eventNotifier {
                addCertificatesFinish()
            }
        }
    }

    private fun ImportCovCertificate.isCertificateHolderInList(
        listOfCertificatesDistinctHolders: List<CovCertificate>
    ): Boolean {
        return listOfCertificatesDistinctHolders.any {
            compareHolder(
                it.name,
                covCertificate.name,
                it.birthDate,
                covCertificate.birthDate
            ) == DataComparison.Equal
        }
    }

    private fun getQrCodesFromPdf(parcelFileDescriptor: ParcelFileDescriptor, displayMetrics: DisplayMetrics) {
        try {
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val qrContentList = mutableListOf<ImportCovCertificate>()
            launch {
                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        displayMetrics.densityDpi * page.width / DEFAULT_PDF_RESOLUTION,
                        displayMetrics.densityDpi * page.height / DEFAULT_PDF_RESOLUTION,
                        Bitmap.Config.ARGB_8888
                    )

                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val newCovCertificates =
                        getCovCertificatesFromBitmap(bitmap, MAX_SIZE_QR_CONTENT_LIST - qrContentList.size)
                    val groupedCertificatesList = certRepository.certs.value
                    val listOfIdInTheApp =
                        groupedCertificatesList.certificates.flatMap { it.certificates }
                            .map { it.covCertificate.dgcEntry.id }
                    val filteredNewCovCertificates =
                        newCovCertificates.filter { !listOfIdInTheApp.contains(it.covCertificate.dgcEntry.id) }
                    qrContentList.addAll(filteredNewCovCertificates)
                    page.close()
                    if (qrContentList.size >= MAX_SIZE_QR_CONTENT_LIST) {
                        eventNotifier {
                            certificatesFound(qrContentList)
                        }
                        return@launch
                    }
                }

                if (qrContentList.isNotEmpty()) {
                    eventNotifier {
                        certificatesFound(qrContentList)
                    }
                } else {
                    eventNotifier {
                        noValidCertificatesFound()
                    }
                }
            }
        } catch (e: Exception) {
            eventNotifier {
                noValidCertificatesFound()
            }
        }
    }

    private fun getQrCodesFromImages(parcelFileDescriptor: ParcelFileDescriptor) {
        try {
            val fileDescriptor = parcelFileDescriptor.fileDescriptor
            val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            launch {
                val list = getCovCertificatesFromBitmap(bitmap, MAX_SIZE_QR_CONTENT_LIST)
                if (list.isNotEmpty()) {
                    eventNotifier {
                        certificatesFound(list)
                    }
                } else {
                    eventNotifier {
                        noValidCertificatesFound()
                    }
                }
            }
        } catch (e: Exception) {
            eventNotifier {
                noValidCertificatesFound()
            }
        }
    }

    private suspend fun getCovCertificatesFromBitmap(bitmap: Bitmap, maxQrContent: Int): List<ImportCovCertificate> {
        val list = mutableListOf<ImportCovCertificate>()
        val newList = bitmap.convertToQrContentList()
        for (qrContent in newList) {
            try {
                val covCertificate = qrCoder.decodeCovCert(qrContent, allowExpiredCertificates = true)
                validateEntity(covCertificate.dgcEntry.idWithoutPrefix)
                if (!validateRevocation(covCertificate, revocationListRepository)) {
                    list.add(ImportCovCertificate(covCertificate, qrContent))
                }
                if (list.size >= maxQrContent) {
                    return list
                }
            } catch (e: IllegalStateException) {
                continue
            }
        }
        return list
    }

    private companion object {
        const val MAX_SIZE_QR_CONTENT_LIST = 100
        const val MAX_NUMBER_OF_HOLDERS = 20
        const val DEFAULT_PDF_RESOLUTION = 72
    }
}
