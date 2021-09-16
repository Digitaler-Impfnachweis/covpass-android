/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassValueSetsRemoteDataSource
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.remote.valuesets.CovPassValueSetRemote
import io.mockk.*
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

internal class CovPassValueSetsRepositoryTest {

    @Test
    fun `test empty value set repository`() {
        val remoteDataSource: CovPassValueSetsRemoteDataSource = mockk()
        val localDataSource: CovPassValueSetsLocalDataSource = mockk()
        val covPassValueSetRemote: CovPassValueSetRemote = mockk()

        coEvery { remoteDataSource.getValueSetIdentifiers() } returns emptyList()
        coEvery { remoteDataSource.getValueSet("") } returns covPassValueSetRemote
        coEvery { localDataSource.getAll() } returns emptyList()
        coEvery { localDataSource.update(any(), any()) } just Runs

        val repository = CovPassValueSetsRepository(
            remoteDataSource, localDataSource
        )
        runBlockingTest {
            repository.loadValueSets()
        }

        coVerify { remoteDataSource.getValueSetIdentifiers() }
        coVerify { localDataSource.getAll() }
        coVerify { localDataSource.update(any(), any()) }
    }
}
