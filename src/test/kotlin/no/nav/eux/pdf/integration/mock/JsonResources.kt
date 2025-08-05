package no.nav.eux.pdf.integration.mock

fun getResource(resource: String) = Any::class::class.java.getResource(resource)!!.readText()
