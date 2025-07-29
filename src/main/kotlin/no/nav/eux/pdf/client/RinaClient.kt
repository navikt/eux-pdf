package no.nav.eux.pdf.client

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.config.RinaRestClient
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterDocument
import no.nav.eux.pdf.model.domain.U020SubdocumentsCollection

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

    fun getDocument(caseId: Int, documentId: String): U020MasterDocument {
        val entity: ResponseEntity<U020MasterDocument> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<U020MasterDocument>()
        return entity.body!!
    }

    fun getSubdocuments(caseId: Int, documentId: String): U020SubdocumentsCollection {
        val entity: ResponseEntity<U020SubdocumentsCollection> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<U020SubdocumentsCollection>()
        return entity.body!!
    }

    fun getSubdocument(caseId: Int, documentId: String, subdocumentId: String): U020ChildDocument {
        val entity: ResponseEntity<U020ChildDocument> = rinaRestClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments/$subdocumentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .toEntity<U020ChildDocument>()
        return entity.body!!
    }
}
