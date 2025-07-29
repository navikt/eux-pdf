package no.nav.eux.pdf.client

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.config.RinaRestClient

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.toEntity
import org.springframework.web.util.UriComponentsBuilder

import no.nav.eux.pdf.model.rinasak.RinaCase

@Component
class RinaClient(
    val rinaCpiServiceProperties: RinaCpiServiceProperties,
    val rinaRestClient: RinaRestClient,
) {
    val log = logger {}

    val casesUri: String by lazy { "${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases" }

    fun rinasak(rinasakId: Int): RinaCase {
        val entity: ResponseEntity<RinaCase> = rinaRestClient
            .get()
            .uri("$casesUri/$rinasakId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", rinasakId.toString())
            .retrieve()
            .toEntity<RinaCase>()
         return entity.body!!
    }

    fun getDocument(caseId: Int, documentId: String): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<String>()
        return entity.body!!
    }

    fun getSubdocuments(caseId: Int, documentId: String): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<String>()
        return entity.body!!
    }

    fun getSubdocument(caseId: Int, documentId: String, subdocumentId: String): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments/$subdocumentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<String>()
        return entity.body!!
    }
}
