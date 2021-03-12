package com.ibm.health.sampleapp

enum class EpaAccessGateway(val fqdn: String) {
    Itb("epaitb.ibmepatest.de"),
    Box("box.epa-dev.net"),
    Dev("dev.epa-dev.net")
}
