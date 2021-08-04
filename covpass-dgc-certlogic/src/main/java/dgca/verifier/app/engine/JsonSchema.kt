package dgca.verifier.app.engine

const val JSON_SCHEMA_V1 = "{\n" +
    "  \"\$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
    "  \"\$id\": \"https://id.uvci.eu/DGC.combined-schema.json\",\n" +
    "  \"title\": \"EU DGC\",\n" +
    "  \"description\": \"EU Digital Green Certificate\",\n" +
    "  \"\$comment\": \"Schema version 1.0.0\",\n" +
    "  \"required\": [\n" +
    "    \"ver\",\n" +
    "    \"nam\",\n" +
    "    \"dob\"\n" +
    "  ],\n" +
    "  \"type\": \"object\",\n" +
    "  \"properties\": {\n" +
    "    \"ver\": {\n" +
    "      \"title\": \"Schema version\",\n" +
    "      \"description\": \"Version of the schema, according to Semantic versioning (ISO, https://semver.org/ version 2.0.0 or newer)\",\n" +
    "      \"type\": \"string\",\n" +
    "      \"examples\": [\n" +
    "        \"1.0.0\"\n" +
    "      ]\n" +
    "    },\n" +
    "    \"nam\": {\n" +
    "      \"description\": \"Surname(s), given name(s) - in that order\",\n" +
    "      \"\$ref\": \"#/\$defs/person_name\"\n" +
    "    },\n" +
    "    \"dob\": {\n" +
    "      \"title\": \"Date of birth\",\n" +
    "      \"description\": \"Date of Birth of the person addressed in the DGC. ISO 8601 date format restricted to range 1900-2099\",\n" +
    "      \"type\": \"string\",\n" +
    "      \"examples\": [\n" +
    "        \"1979-04-14\"\n" +
    "      ]\n" +
    "    },\n" +
    "    \"v\": {\n" +
    "      \"description\": \"Vaccination Group\",\n" +
    "      \"type\": [\"null\", \"array\"],\n" +
    "      \"items\": {\n" +
    "        \"\$ref\": \"#/\$defs/vaccination_entry\"\n" +
    "      }\n" +
    "    },\n" +
    "    \"t\": {\n" +
    "      \"description\": \"Test Group\",\n" +
    "      \"type\": [\"null\", \"array\"],\n" +
    "      \"items\": {\n" +
    "        \"\$ref\": \"#/\$defs/test_entry\"\n" +
    "      }\n" +
    "    },\n" +
    "    \"r\": {\n" +
    "      \"description\": \"Recovery Group\",\n" +
    "      \"type\": [\"null\", \"array\"],\n" +
    "      \"items\": {\n" +
    "        \"\$ref\": \"#/\$defs/recovery_entry\"\n" +
    "      }\n" +
    "    }\n" +
    "  },\n" +
    "  \"\$defs\": {\n" +
    "    \"dose_posint\": {\n" +
    "      \"description\": \"Dose Number / Total doses in Series: positive integer, range: [1,9]\",\n" +
    "      \"type\": [\"null\", \"integer\"]\n" +
    "    },\n" +
    "    \"country_vt\": {\n" +
    "      \"description\": \"Country of Vaccination / Test, ISO 3166 where possible\",\n" +
    "      \"type\": [\"null\", \"string\"]\n" +
    "    },\n" +
    "    \"issuer\": {\n" +
    "      \"description\": \"Certificate Issuer\",\n" +
    "      \"type\": [\"null\", \"string\"]\n" +
    "    },\n" +
    "    \"person_name\": {\n" +
    "      \"description\": \"Person name: Surname(s), given name(s) - in that order\",\n" +
    "      \"required\": [\n" +
    "        \"fnt\"\n" +
    "      ],\n" +
    "      \"type\": [\"null\", \"object\"],\n" +
    "      \"properties\": {\n" +
    "        \"fn\": {\n" +
    "          \"title\": \"Family name\",\n" +
    "          \"description\": \"The family or primary name(s) of the person addressed in the certificate\",\n" +
    "          \"type\": [\"null\", \"string\"],\n" +
    "          \"examples\": [\n" +
    "            \"d'Červenková Panklová\"\n" +
    "          ]\n" +
    "        },\n" +
    "        \"fnt\": {\n" +
    "          \"title\": \"Standardised family name\",\n" +
    "          \"description\": \"The family name(s) of the person transliterated\",\n" +
    "          \"type\": \"string\",\n" +
    "          \"examples\": [\n" +
    "            \"DCERVENKOVA<PANKLOVA\"\n" +
    "          ]\n" +
    "        },\n" +
    "        \"gn\": {\n" +
    "          \"title\": \"Given name\",\n" +
    "          \"description\": \"The given name(s) of the person addressed in the certificate\",\n" +
    "          \"type\": [\"null\", \"string\"],\n" +
    "          \"examples\": [\n" +
    "            \"Jiřina-Maria Alena\"\n" +
    "          ]\n" +
    "        },\n" +
    "        \"gnt\": {\n" +
    "          \"title\": \"Standardised given name\",\n" +
    "          \"description\": \"The given name(s) of the person transliterated\",\n" +
    "          \"type\": [\"null\", \"string\"],\n" +
    "          \"examples\": [\n" +
    "            \"JIRINA<MARIA<ALENA\"\n" +
    "          ]\n" +
    "        }\n" +
    "      }\n" +
    "    },\n" +
    "    \"certificate_id\": {\n" +
    "      \"description\": \"Certificate Identifier, format as per UVCI: Annex 2 in  https://ec.europa.eu/health/sites/health/files/ehealth/docs/vaccination-proof_interoperability-guidelines_en.pdf\",\n" +
    "      \"type\": [\"null\", \"string\"]\n" +
    "    },\n" +
    "    \"vaccination_entry\": {\n" +
    "      \"description\": \"Vaccination Entry\",\n" +
    "      \"required\": [\n" +
    "        \"tg\",\n" +
    "        \"vp\",\n" +
    "        \"mp\",\n" +
    "        \"ma\",\n" +
    "        \"dn\",\n" +
    "        \"sd\",\n" +
    "        \"dt\",\n" +
    "        \"co\",\n" +
    "        \"is\",\n" +
    "        \"ci\"\n" +
    "      ],\n" +
    "      \"type\": [\"null\", \"object\"],\n" +
    "      \"properties\": {\n" +
    "        \"tg\": {\n" +
    "          \"description\": \"disease or agent targeted\",\n" +
    "          \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "        },\n" +
    "        \"vp\": {\n" +
    "          \"description\": \"vaccine or prophylaxis\",\n" +
    "          \"\$ref\": \"#/\$defs/vaccine-prophylaxis\"\n" +
    "        },\n" +
    "        \"mp\": {\n" +
    "          \"description\": \"vaccine medicinal product\",\n" +
    "          \"\$ref\": \"#/\$defs/vaccine-medicinal-product\"\n" +
    "        },\n" +
    "        \"ma\": {\n" +
    "          \"description\": \"Marketing Authorization Holder - if no MAH present, then manufacturer\",\n" +
    "          \"\$ref\": \"#/\$defs/vaccine-mah-manf\"\n" +
    "        },\n" +
    "        \"dn\": {\n" +
    "          \"description\": \"Dose Number\",\n" +
    "          \"\$ref\": \"#/\$defs/dose_posint\"\n" +
    "        },\n" +
    "        \"sd\": {\n" +
    "          \"description\": \"Total Series of Doses\",\n" +
    "          \"\$ref\": \"#/\$defs/dose_posint\"\n" +
    "        },\n" +
    "        \"dt\": {\n" +
    "          \"description\": \"Date of Vaccination\",\n" +
    "          \"type\": \"string\",\n" +
    "          \"\$comment\": \"SemanticSG: constrain to specific date range?\"\n" +
    "        },\n" +
    "        \"co\": {\n" +
    "          \"description\": \"Country of Vaccination\",\n" +
    "          \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "        },\n" +
    "        \"is\": {\n" +
    "          \"description\": \"Certificate Issuer\",\n" +
    "          \"\$ref\": \"#/\$defs/issuer\"\n" +
    "        },\n" +
    "        \"ci\": {\n" +
    "          \"description\": \"Unique Certificate Identifier: UVCI\",\n" +
    "          \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "        }\n" +
    "      }\n" +
    "    },\n" +
    "    \"test_entry\": {\n" +
    "      \"description\": \"Test Entry\",\n" +
    "      \"required\": [\n" +
    "        \"tg\",\n" +
    "        \"tt\",\n" +
    "        \"sc\",\n" +
    "        \"tr\",\n" +
    "        \"co\",\n" +
    "        \"is\",\n" +
    "        \"ci\"\n" +
    "      ],\n" +
    "      \"type\": [\"null\", \"object\"],\n" +
    "      \"properties\": {\n" +
    "        \"tg\": {\n" +
    "          \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "        },\n" +
    "        \"tt\": {\n" +
    "          \"description\": \"Type of Test\",\n" +
    "          \"type\": \"string\"\n" +
    "        },\n" +
    "        \"nm\": {\n" +
    "          \"description\": \"NAA Test Name\",\n" +
    "          \"type\": [\"null\", \"string\"]\n" +
    "        },\n" +
    "        \"ma\": {\n" +
    "          \"description\": \"RAT Test name and manufacturer\",\n" +
    "          \"\$ref\": \"#/\$defs/test-manf\"\n" +
    "        },\n" +
    "        \"sc\": {\n" +
    "          \"description\": \"Date/Time of Sample Collection\",\n" +
    "          \"type\": \"string\"\n" +
    "        },\n" +
    "        \"dr\": {\n" +
    "          \"description\": \"Date/Time of Test Result\",\n" +
    "          \"type\": [\"null\", \"string\"]\n" +
    "        },\n" +
    "        \"tr\": {\n" +
    "          \"description\": \"Test Result\",\n" +
    "          \"\$ref\": \"#/\$defs/test-result\"\n" +
    "        },\n" +
    "        \"tc\": {\n" +
    "          \"description\": \"Testing Centre\",\n" +
    "          \"type\": [\"null\", \"string\"]\n" +
    "        },\n" +
    "        \"co\": {\n" +
    "          \"description\": \"Country of Test\",\n" +
    "          \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "        },\n" +
    "        \"is\": {\n" +
    "          \"description\": \"Certificate Issuer\",\n" +
    "          \"\$ref\": \"#/\$defs/issuer\"\n" +
    "        },\n" +
    "        \"ci\": {\n" +
    "          \"description\": \"Unique Certificate Identifier, UVCI\",\n" +
    "          \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "        }\n" +
    "      }\n" +
    "    },\n" +
    "    \"recovery_entry\": {\n" +
    "      \"description\": \"Recovery Entry\",\n" +
    "      \"required\": [\n" +
    "        \"tg\",\n" +
    "        \"fr\",\n" +
    "        \"co\",\n" +
    "        \"is\",\n" +
    "        \"df\",\n" +
    "        \"du\",\n" +
    "        \"ci\"\n" +
    "      ],\n" +
    "      \"type\": [\"null\", \"object\"],\n" +
    "      \"properties\": {\n" +
    "        \"tg\": {\n" +
    "          \"\$ref\": \"#/\$defs/disease-agent-targeted\"\n" +
    "        },\n" +
    "        \"fr\": {\n" +
    "          \"description\": \"ISO 8601 Date of First Positive Test Result\",\n" +
    "          \"type\": \"string\"\n" +
    "        },\n" +
    "        \"co\": {\n" +
    "          \"description\": \"Country of Test\",\n" +
    "          \"\$ref\": \"#/\$defs/country_vt\"\n" +
    "        },\n" +
    "        \"is\": {\n" +
    "          \"description\": \"Certificate Issuer\",\n" +
    "          \"\$ref\": \"#/\$defs/issuer\"\n" +
    "        },\n" +
    "        \"df\": {\n" +
    "          \"description\": \"ISO 8601 Date: Certificate Valid From\",\n" +
    "          \"type\": \"string\"\n" +
    "        },\n" +
    "        \"du\": {\n" +
    "          \"description\": \"Certificate Valid Until\",\n" +
    "          \"type\": \"string\"\n" +
    "        },\n" +
    "        \"ci\": {\n" +
    "          \"description\": \"Unique Certificate Identifier, UVCI\",\n" +
    "          \"\$ref\": \"#/\$defs/certificate_id\"\n" +
    "        }\n" +
    "      }\n" +
    "    },\n" +
    "    \"disease-agent-targeted\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.1\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/disease-agent-targeted.json\"\n" +
    "    },\n" +
    "    \"vaccine-prophylaxis\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.2\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/vaccine-prophylaxis.json\"\n" +
    "    },\n" +
    "    \"vaccine-medicinal-product\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.3\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/vaccine-medicinal-product.json\"\n" +
    "    },\n" +
    "    \"vaccine-mah-manf\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.4\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/vaccine-mah-manf.json\"\n" +
    "    },\n" +
    "    \"test-manf\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.8\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/test-manf.json\"\n" +
    "    },\n" +
    "    \"test-result\": {\n" +
    "      \"description\": \"EU eHealthNetwork: Value Sets for Digital Green Certificates. version 1.0, 2021-04-16, section 2.9\",\n" +
    "      \"type\": [\"null\", \"string\"],\n" +
    "      \"valueset-uri\": \"valuesets/test-results.json\"\n" +
    "    }\n" +
    "  }\n" +
    "}"
