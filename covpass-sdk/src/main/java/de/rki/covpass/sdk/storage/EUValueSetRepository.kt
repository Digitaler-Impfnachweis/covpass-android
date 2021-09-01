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
            "EU/1/21/1529" to EUValueSetValue(
                display = "Vaxzevria",
                lang = "en",
                active = true,
                system = "https://ec.europa.eu/health/documents/community-register/html/",
                version = ""
            ),
            "EU/1/20/1525" to EUValueSetValue(
                display = "COVID-19 Vaccine Janssen",
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
            ),
            "Covishield" to EUValueSetValue(
                display = "Covishield (ChAdOx1_nCoV-19)",
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
            "ORG-100001417" to EUValueSetValue(
                display = "Janssen-Cilag International",
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
            ),
            "ORG-100001981" to EUValueSetValue(
                display = "Serum Institute Of India Private Limited",
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

    val testType: EUValueSet = EUValueSet(
        valueSetId = "covid-19-lab-test-type",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "LP6464-4" to EUValueSetValue(
                display = "Nucleic acid amplification with probe detection",
                lang = "en",
                active = true,
                system = "http://loinc.org",
                version = "2.69"
            ),
            "LP217198-3" to EUValueSetValue(
                display = "Rapid immunoassay",
                lang = "en",
                active = true,
                system = "http://loinc.org",
                version = "2.69"
            )
        )
    )

    val testResult: EUValueSet = EUValueSet(
        valueSetId = "covid-19-lab-result",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "260415000" to EUValueSetValue(
                display = "Not detected",
                lang = "en",
                active = true,
                system = "http://snomed.info/sct",
                version = "http://snomed.info/sct/900000000000207008/version/20210131"
            ),
            "260373001" to EUValueSetValue(
                display = "Detected",
                lang = "en",
                active = true,
                system = "http://snomed.info/sct",
                version = "http://snomed.info/sct/900000000000207008/version/20210131"
            )
        )
    )

    val testManufacturer: EUValueSet = EUValueSet(
        valueSetId = "covid-19-lab-test-manufacturer-and-name",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "1232" to EUValueSetValue(
                display = "Abbott Rapid Diagnostics, Panbio COVID-19 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1304" to EUValueSetValue(
                display = "AMEDA Labordiagnostik GmbH, AMP Rapid Test SARS-CoV-2 Ag",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1065" to EUValueSetValue(
                display = "Becton Dickinson, Veritor System Rapid Detection of SARS-CoV-2",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1331" to EUValueSetValue(
                display = "Beijing Lepu Medical Technology Co., Ltd, SARS-CoV-2 Antigen Rapid Test Kit",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1484" to EUValueSetValue(
                display =
                "Beijing Wantai Biological Pharmacy Enterprise Co., Ltd, Wantai SARS-CoV-2 Ag Rapid Test (FIA)",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1242" to EUValueSetValue(
                display = "Bionote, Inc, NowCheck COVID-19 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1223" to EUValueSetValue(
                display = "BIOSYNEX SWISS SA, BIOSYNEX COVID-19 Ag BSS",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1173" to EUValueSetValue(
                display = "CerTest Biotec, S.L., CerTest SARS-CoV-2 Card test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1244" to EUValueSetValue(
                display = "GenBody, Inc, Genbody COVID-19 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1360" to EUValueSetValue(
                display = "Guangdong Wesail Biotech Co., Ltd, COVID-19 Ag Test Kit",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1363" to EUValueSetValue(
                display = "Hangzhou Clongene Biotech Co., Ltd, Covid-19 Antigen Rapid Test Kit",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1767" to EUValueSetValue(
                display = "Healgen Scientific Limited Liability Company, Coronavirus Ag Rapid Test Cassette",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1333" to EUValueSetValue(
                display = "Joinstar Biomedical Technology Co., Ltd, COVID-19 Rapid Antigen Test (Colloidal Gold)",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1268" to EUValueSetValue(
                display = "LumiraDX UK Ltd, LumiraDx SARS-CoV-2 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1180" to EUValueSetValue(
                display = "MEDsan GmbH, MEDsan SARS-CoV-2 Antigen Rapid Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1481" to EUValueSetValue(
                display = "MP Biomedicals Germany GmbH, Rapid SARS-CoV-2 Antigen Test Card",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1162" to EUValueSetValue(
                display = "Nal von minden GmbH, NADAL COVID-19 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1271" to EUValueSetValue(
                display = "Precision Biosensor, Inc, Exdia COVID-19 Ag",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1341" to EUValueSetValue(
                display = "Qingdao Hightop Biotech Co., Ltd, SARS-CoV-2 Antigen Rapid Test (Immunochromatography)",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1097" to EUValueSetValue(
                display = "Quidel Corporation, Sofia SARS Antigen FIA",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1489" to EUValueSetValue(
                display = "Safecare Biotech (Hangzhou) Co. Ltd, COVID-19 Antigen Rapid Test Kit (Swab)",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "344" to EUValueSetValue(
                display = "SD BIOSENSOR Inc, STANDARD F COVID-19 Ag FIA",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "345" to EUValueSetValue(
                display = "SD BIOSENSOR Inc, STANDARD Q COVID-19 Ag Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1218" to EUValueSetValue(
                display = "Siemens Healthineers, CLINITEST Rapid Covid-19 Antigen Test",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1278" to EUValueSetValue(
                display = "Xiamen Boson Biotech Co. Ltd, Rapid SARS-CoV-2 Antigen Test Card",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            ),
            "1343" to EUValueSetValue(
                display = "Zhejiang Orient Gene Biotech, Coronavirus Ag Rapid Test Cassette (Swab)",
                lang = "en",
                active = true,
                system = "https://covid-19-diagnostics.jrc.ec.europa.eu/devices",
                version = "2021-04-22 02:23:55 CET"
            )
        )
    )

    val diseaseAgent: EUValueSet = EUValueSet(
        valueSetId = "disease-agent-targeted",
        valueSetDate = "2021-04-27",
        valueSetValues = mapOf(
            "840539006" to EUValueSetValue(
                display = "COVID-19",
                lang = "en",
                active = true,
                system = "http://snomed.info/sct",
                version = "http://snomed.info/sct/900000000000207008/version/20210131"
            )
        )
    )
}
