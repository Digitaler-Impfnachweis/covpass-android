/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import de.rki.covpass.sdk.cert.models.EUValueSet
import de.rki.covpass.sdk.cert.models.EUValueSetValue

// TODO temporary solution until the Backend endpoint is provided
internal object EUValueSetRepository {

    val vaccineMedicalProduct: EUValueSet = EUValueSet(
        valueSetId = "vaccines-covid-19-names",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "EU/1/20/1528" to EUValueSetValue(
                display = "Comirnaty",
                lang = "en",
                active = true,
                system = "https://ec.europa.eu/health/documents/community-register/html/",
                version = ""
            ),
            "EU/1/20/1507" to EUValueSetValue(
                display = "COVID-19 Vaccine Moderna",
                lang = "en",
                active = true,
                system = "https://ec.europa.eu/health/documents/community-register/html/",
                version = ""
            ),
            "EU/1/20/1529" to EUValueSetValue(
                display = "Vaxzevria",
                lang = "en",
                active = true,
                system = "https://ec.europa.eu/health/documents/community-register/html/",
                version = ""
            ),
            "CVnCoV" to EUValueSetValue(
                display = "CVnCoV",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "Sputnik-V" to EUValueSetValue(
                display = "Sputnik-V",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "Convidecia" to EUValueSetValue(
                display = "Convidecia",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "EpiVacCorona" to EUValueSetValue(
                display = "EpiVacCorona",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "BBIBP-CorV" to EUValueSetValue(
                display = "BBIBP-CorV",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "Inactivated-SARS-CoV-2-Vero-Cell" to EUValueSetValue(
                display = "Inactivated SARS-CoV-2 (Vero Cell)",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "CoronaVac" to EUValueSetValue(
                display = "CoronaVac",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            ),
            "Covaxin" to EUValueSetValue(
                display = "Covaxin (also known as BBV152 A, B, C)",
                lang = "en",
                active = true,
                system = "http://ec.europa.eu/temp/vaccineproductname",
                version = "1.0"
            )
        )
    )

    val vaccineManufacturer: EUValueSet = EUValueSet(
        valueSetId = "vaccines-covid-19-auth-holders",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "ORG-100001699" to EUValueSetValue(
                display = "AstraZeneca AB",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100030215" to EUValueSetValue(
                display = "Biontech Manufacturing GmbH",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100031184" to EUValueSetValue(
                display = "Moderna Biotech Spain S.L.",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100006270" to EUValueSetValue(
                display = "Curevac AG",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100013793" to EUValueSetValue(
                display = "CanSino Biologics",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100020693" to EUValueSetValue(
                display = "China Sinopharm International Corp. - Beijing location",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100010771" to EUValueSetValue(
                display = "Sinopharm Weiqida Europe Pharmaceutical s.r.o. - Prague location",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100024420" to EUValueSetValue(
                display = "Sinopharm Zhijun (Shenzhen) Pharmaceutical Co. Ltd. - Shenzhen location",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "ORG-100032020" to EUValueSetValue(
                display = "Novavax CZ AS",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = ""
            ),
            "Gamaleya-Research-Institute" to EUValueSetValue(
                display = "Gamaleya Research Institute",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = "1.0"
            ),
            "Vector-Institute" to EUValueSetValue(
                display = "Vector Institute",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = "1.0"
            ),
            "Sinovac-Biotech" to EUValueSetValue(
                display = "Sinovac Biotech",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = "1.0"
            ),
            "Bharat-Biotech" to EUValueSetValue(
                display = "Bharat Biotech",
                lang = "en",
                active = true,
                system = "https://spor.ema.europa.eu/v1/organisations",
                version = "1.0"
            )
        )
    )

    val vaccineProphylaxis: EUValueSet = EUValueSet(
        valueSetId = "sct-vaccines-covid-19",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "1119349007" to EUValueSetValue(
                display = "SARS-CoV-2 mRNA vaccine",
                lang = "en",
                active = true,
                system = "http://snomed.info/sct",
                version = "http://snomed.info/sct/900000000000207008/version/20210131"
            ),
            "1119305005" to EUValueSetValue(
                display = "SARS-CoV-2 antigen vaccine",
                lang = "en",
                active = true,
                system = "http://snomed.info/sct",
                version = "http://snomed.info/sct/900000000000207008/version/20210131"
            ),
            "J07BX03" to EUValueSetValue(
                display = "covid-19 vaccines",
                lang = "en",
                active = true,
                system = "http://www.whocc.no/atc",
                version = "2021-01"
            )
        )
    )
}
