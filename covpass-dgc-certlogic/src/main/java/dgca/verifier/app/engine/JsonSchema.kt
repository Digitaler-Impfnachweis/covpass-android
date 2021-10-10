package dgca.verifier.app.engine

const val JSON_SCHEMA_V1 = "{\n" +
    "   \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
    "   \"\$id\": \"https://id.uvci.eu/DCC.combined-schema.json\",\n" +
    "   \"title\": \"EU DCC\",\n" +
    "   \"description\": \"EU Digital Covid Certificate\",\n" +
    "   \"\$comment\": \"Schema version 1.3.0\",\n" +
    "   \"type\": \"object\",\n" +
    "   \"oneOf\": [\n" +
    "      {\n" +
    "         \"required\": [\n" +
    "            \"ver\",\n" +
    "            \"nam\",\n" +
    "            \"dob\",\n" +
    "            \"v\"\n" +
    "         ]\n" +
    "      },\n" +
    "      {\n" +
    "         \"required\": [\n" +
    "            \"ver\",\n" +
    "            \"nam\",\n" +
    "            \"dob\",\n" +
    "            \"t\"\n" +
    "         ]\n" +
    "      },\n" +
    "      {\n" +
    "         \"required\": [\n" +
    "            \"ver\",\n" +
    "            \"nam\",\n" +
    "            \"dob\",\n" +
    "            \"r\"\n" +
    "         ]\n" +
    "      }\n" +
    "   ],\n" +
    "   \"properties\": {\n" +
    "      \"ver\": {\n" +
    "         \"title\": \"Schema version\",\n" +
    "         \"description\": \"Version of the schema, according to Semantic versioning (ISO, https://semver.org/ version 2.0.0 or newer)\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"pattern\": \"^\\\\d+.\\\\d+.\\\\d+\$\",\n" +
    "         \"examples\": [\n" +
    "            \"1.3.0\"\n" +
    "         ]\n" +
    "      },\n" +
    "      \"nam\": {\n" +
    "         \"description\": \"Surname(s), forename(s) - in that order\",\n" +
    "         \"\$ref\": \"#/\$defs/person_name\"\n" +
    "      },\n" +
    "      \"dob\": {\n" +
    "         \"title\": \"Date of birth\",\n" +
    "         \"description\": \"Date of Birth of the person addressed in the DCC. ISO 8601 date format restricted to range 1900-2099 or empty\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"pattern\": \"^((19|20)\\\\d\\\\d(-\\\\d\\\\d){0,2}){0,1}\$\",\n" +
    "         \"examples\": [\n" +
    "            \"1979-04-14\",\n" +
    "            \"1950\",\n" +
    "            \"1901-08\",\n" +
    "            \"\"\n" +
    "         ]\n" +
    "      },\n" +
    "      \"v\": {\n" +
    "         \"description\": \"Vaccination Group\",\n" +
    "         \"type\": \"array\",\n" +
    "         \"items\": {\n" +
    "            \"\$ref\": \"#/\$defs/vaccination_entry\"\n" +
    "         },\n" +
    "         \"minItems\": 1,\n" +
    "         \"maxItems\": 1\n" +
    "      },\n" +
    "      \"t\": {\n" +
    "         \"description\": \"Test Group\",\n" +
    "         \"type\": \"array\",\n" +
    "         \"items\": {\n" +
    "            \"\$ref\": \"#/\$defs/test_entry\"\n" +
    "         },\n" +
    "         \"minItems\": 1,\n" +
    "         \"maxItems\": 1\n" +
    "      },\n" +
    "      \"r\": {\n" +
    "         \"description\": \"Recovery Group\",\n" +
    "         \"type\": \"array\",\n" +
    "         \"items\": {\n" +
    "            \"\$ref\": \"#/\$defs/recovery_entry\"\n" +
    "         },\n" +
    "         \"minItems\": 1,\n" +
    "         \"maxItems\": 1\n" +
    "      }\n" +
    "   },\n" +
    "   \"\$defs\": {\n" +
    "      \"dose_posint\": {\n" +
    "         \"description\": \"Dose Number / Total doses in Series: positive integer\",\n" +
    "         \"type\": \"integer\",\n" +
    "         \"minimum\": 1\n" +
    "      },\n" +
    "      \"issuer\": {\n" +
    "         \"description\": \"Certificate Issuer\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"maxLength\": 80\n" +
    "      },\n" +
    "      \"person_name\": {\n" +
    "         \"description\": \"Person name: Surname(s), forename(s) - in that order\",\n" +
    "         \"required\": [\n" +
    "            \"fnt\"\n" +
    "         ],\n" +
    "         \"type\": \"object\",\n" +
    "         \"properties\": {\n" +
    "            \"fn\": {\n" +
    "               \"title\": \"Surname\",\n" +
    "               \"description\": \"The surname or primary name(s) of the person addressed in the certificate\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"maxLength\": 80,\n" +
    "               \"examples\": [\n" +
    "                  \"d'Červenková Panklová\"\n" +
    "               ]\n" +
    "            },\n" +
    "            \"fnt\": {\n" +
    "               \"title\": \"Standardised surname\",\n" +
    "               \"description\": \"The surname(s) of the person, transliterated ICAO 9303\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"pattern\": \"^[A-Z<]*\$\",\n" +
    "               \"maxLength\": 80,\n" +
    "               \"examples\": [\n" +
    "                  \"DCERVENKOVA<PANKLOVA\"\n" +
    "               ]\n" +
    "            },\n" +
    "            \"gn\": {\n" +
    "               \"title\": \"Forename\",\n" +
    "               \"description\": \"The forename(s) of the person addressed in the certificate\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"maxLength\": 80,\n" +
    "               \"examples\": [\n" +
    "                  \"Jiřina-Maria Alena\"\n" +
    "               ]\n" +
    "            },\n" +
    "            \"gnt\": {\n" +
    "               \"title\": \"Standardised forename\",\n" +
    "               \"description\": \"The forename(s) of the person, transliterated ICAO 9303\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"pattern\": \"^[A-Z<]*\$\",\n" +
    "               \"maxLength\": 80,\n" +
    "               \"examples\": [\n" +
    "                  \"JIRINA<MARIA<ALENA\"\n" +
    "               ]\n" +
    "            }\n" +
    "         }\n" +
    "      },\n" +
    "      \"certificate_id\": {\n" +
    "         \"description\": \"Certificate Identifier, format as per UVCI: Annex 2 in  https://ec.europa.eu/health/sites/health/files/ehealth/docs/vaccination-proof_interoperability-guidelines_en.pdf\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"maxLength\": 80\n" +
    "      },\n" +
    "      \"vaccination_entry\": {\n" +
    "         \"description\": \"Vaccination Entry\",\n" +
    "         \"required\": [\n" +
    "            \"tg\",\n" +
    "            \"vp\",\n" +
    "            \"mp\",\n" +
    "            \"ma\",\n" +
    "            \"dn\",\n" +
    "            \"sd\",\n" +
    "            \"dt\",\n" +
    "            \"co\",\n" +
    "            \"is\",\n" +
    "            \"ci\"\n" +
    "         ],\n" +
    "         \"type\": \"object\",\n" +
    "         \"properties\": {\n" +
    "            \"tg\": {\n" +
    "               \"description\": \"disease or agent targeted\",\n" +
    "               \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "            },\n" +
    "            \"vp\": {\n" +
    "               \"description\": \"vaccine or prophylaxis\",\n" +
    "               \"\$ref\": \"#/\$defs/vaccine-prophylaxis\"\n" +
    "            },\n" +
    "            \"mp\": {\n" +
    "               \"description\": \"vaccine medicinal product\",\n" +
    "               \"\$ref\": \"#/\$defs/vaccine-medicinal-product\"\n" +
    "            },\n" +
    "            \"ma\": {\n" +
    "               \"description\": \"Marketing Authorization Holder - if no MAH present, then manufacturer\",\n" +
    "               \"\$ref\": \"#/\$defs/vaccine-mah-manf\"\n" +
    "            },\n" +
    "            \"dn\": {\n" +
    "               \"description\": \"Dose Number\",\n" +
    "               \"\$ref\": \"#/\$defs/dose_posint\"\n" +
    "            },\n" +
    "            \"sd\": {\n" +
    "               \"description\": \"Total Series of Doses\",\n" +
    "               \"\$ref\": \"#/\$defs/dose_posint\"\n" +
    "            },\n" +
    "            \"dt\": {\n" +
    "               \"description\": \"ISO8601 complete date: Date of Vaccination\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"format\": \"date\"\n" +
    "            },\n" +
    "            \"co\": {\n" +
    "               \"description\": \"Country of Vaccination\",\n" +
    "               \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "            },\n" +
    "            \"is\": {\n" +
    "               \"description\": \"Certificate Issuer\",\n" +
    "               \"\$ref\": \"#/\$defs/issuer\"\n" +
    "            },\n" +
    "            \"ci\": {\n" +
    "               \"description\": \"Unique Certificate Identifier: UVCI\",\n" +
    "               \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "            }\n" +
    "         }\n" +
    "      },\n" +
    "      \"test_entry\": {\n" +
    "         \"description\": \"Test Entry\",\n" +
    "         \"required\": [\n" +
    "            \"tg\",\n" +
    "            \"tt\",\n" +
    "            \"sc\",\n" +
    "            \"tr\",\n" +
    "            \"co\",\n" +
    "            \"is\",\n" +
    "            \"ci\"\n" +
    "         ],\n" +
    "         \"type\": \"object\",\n" +
    "         \"properties\": {\n" +
    "            \"tg\": {\n" +
    "               \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "            },\n" +
    "            \"tt\": {\n" +
    "               \"description\": \"Type of Test\",\n" +
    "               \"\$ref\": \"#/\$defs/test-type\"\n" +
    "            },\n" +
    "            \"nm\": {\n" +
    "               \"description\": \"NAA Test Name\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"maxLength\": 80\n" +
    "            },\n" +
    "            \"ma\": {\n" +
    "               \"description\": \"RAT Test name and manufacturer\",\n" +
    "               \"\$ref\": \"#/\$defs/test-manf\"\n" +
    "            },\n" +
    "            \"sc\": {\n" +
    "               \"description\": \"Date/Time of Sample Collection\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"format\": \"date-time\"\n" +
    "            },\n" +
    "            \"tr\": {\n" +
    "               \"description\": \"Test Result\",\n" +
    "               \"\$ref\": \"#/\$defs/test-result\"\n" +
    "            },\n" +
    "            \"tc\": {\n" +
    "               \"description\": \"Testing Centre\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"maxLength\": 80\n" +
    "            },\n" +
    "            \"co\": {\n" +
    "               \"description\": \"Country of Test\",\n" +
    "               \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "            },\n" +
    "            \"is\": {\n" +
    "               \"description\": \"Certificate Issuer\",\n" +
    "               \"\$ref\": \"#/\$defs/issuer\"\n" +
    "            },\n" +
    "            \"ci\": {\n" +
    "               \"description\": \"Unique Certificate Identifier, UVCI\",\n" +
    "               \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "            }\n" +
    "         }\n" +
    "      },\n" +
    "      \"recovery_entry\": {\n" +
    "         \"description\": \"Recovery Entry\",\n" +
    "         \"required\": [\n" +
    "            \"tg\",\n" +
    "            \"fr\",\n" +
    "            \"co\",\n" +
    "            \"is\",\n" +
    "            \"df\",\n" +
    "            \"du\",\n" +
    "            \"ci\"\n" +
    "         ],\n" +
    "         \"type\": \"object\",\n" +
    "         \"properties\": {\n" +
    "            \"tg\": {\n" +
    "               \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "            },\n" +
    "            \"fr\": {\n" +
    "               \"description\": \"ISO 8601 complete date of first positive NAA test result\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"format\": \"date\"\n" +
    "            },\n" +
    "            \"co\": {\n" +
    "               \"description\": \"Country of Test\",\n" +
    "               \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "            },\n" +
    "            \"is\": {\n" +
    "               \"description\": \"Certificate Issuer\",\n" +
    "               \"\$ref\": \"#/\$defs/issuer\"\n" +
    "            },\n" +
    "            \"df\": {\n" +
    "               \"description\": \"ISO 8601 complete date: Certificate Valid From\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"format\": \"date\"\n" +
    "            },\n" +
    "            \"du\": {\n" +
    "               \"description\": \"ISO 8601 complete date: Certificate Valid Until\",\n" +
    "               \"type\": \"string\",\n" +
    "               \"format\": \"date\"\n" +
    "            },\n" +
    "            \"ci\": {\n" +
    "               \"description\": \"Unique Certificate Identifier, UVCI\",\n" +
    "               \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "            }\n" +
    "         }\n" +
    "      },\n" +
    "      \"disease-agent-targeted\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.1\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/disease-agent-targeted.json\"\n" +
    "      },\n" +
    "      \"vaccine-prophylaxis\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.2\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/vaccine-prophylaxis.json\"\n" +
    "      },\n" +
    "      \"vaccine-medicinal-product\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.3\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/vaccine-medicinal-product.json\"\n" +
    "      },\n" +
    "      \"vaccine-mah-manf\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.4\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/vaccine-mah-manf.json\"\n" +
    "      },\n" +
    "      \"country_vt\": {\n" +
    "         \"description\": \"Country of Vaccination / Test, ISO 3166 alpha-2 where possible\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"pattern\": \"[A-Z]{1,10}\",\n" +
    "         \"valueset-uri\": \"valuesets/country-2-codes.json\"\n" +
    "      },\n" +
    "      \"test-manf\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.8\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/test-manf.json\"\n" +
    "      },\n" +
    "      \"test-result\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.9\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/test-result.json\"\n" +
    "      },\n" +
    "      \"test-type\": {\n" +
    "         \"description\": \"EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.7\",\n" +
    "         \"type\": \"string\",\n" +
    "         \"valueset-uri\": \"valuesets/test-type.json\"\n" +
    "      }\n" +
    "   }\n" +
    "}"
