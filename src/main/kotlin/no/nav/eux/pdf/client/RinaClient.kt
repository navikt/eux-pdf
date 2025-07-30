package no.nav.eux.pdf.client

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterDocument
import no.nav.eux.pdf.model.domain.U020SubdocumentsCollection
import no.nav.eux.pdf.model.rinasak.RinaCase
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import org.springframework.web.server.ResponseStatusException

@Component
class RinaClient(
    val rinaCpiServiceProperties: RinaCpiServiceProperties,
    val restClient: RestClient,
) {
    private val log = logger {}

    val casesUri: String by lazy { "${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases" }

    fun rinasak(rinasakId: Int): RinaCase {
        val entity: ResponseEntity<RinaCase> = restClient
            .get()
            .uri("$casesUri/$rinasakId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", rinasakId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                log.error { "HTTP ${response.statusCode.value()} error for rinasak $rinasakId: $errorBody" }
                val errorMessage = if (response.statusCode.is4xxClientError)
                    "Rinasak ikke funnet"
                else
                    "Serverfeil ved henting av rinasak"
                throw ResponseStatusException(response.statusCode, errorMessage)
            }
            .toEntity<RinaCase>()
         return entity.body!!
    }

    fun getDocument(caseId: Int, documentId: String): U020MasterDocument {
        val entity: ResponseEntity<U020MasterDocument> = restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                log.error {
                    "HTTP ${response.statusCode.value()} error for document $documentId in case $caseId: $errorBody"
                }
                val errorMessage = if (response.statusCode.is4xxClientError)
                    "Dokument ikke funnet"
                else
                    "Serverfeil ved henting av dokument"
                throw ResponseStatusException(response.statusCode, errorMessage)
            }
            .toEntity<U020MasterDocument>()
        return entity.body!!
    }

    fun getSubdocuments(caseId: Int, documentId: String): U020SubdocumentsCollection {
        val entity: ResponseEntity<U020SubdocumentsCollection> = restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                log.error {
                    "HTTP ${response.statusCode.value()} error for subdocuments of document $documentId in case $caseId: $errorBody"
                }
                val errorMessage = if (response.statusCode.is4xxClientError)
                    "Underdokumenter ikke funnet"
                else
                    "Serverfeil ved henting av underdokumenter"
                throw ResponseStatusException(response.statusCode, errorMessage)
            }
            .toEntity<U020SubdocumentsCollection>()
        return entity.body!!
    }

    fun getSubdocument(caseId: Int, documentId: String, subdocumentId: String): U020ChildDocument {
        val entity: ResponseEntity<U020ChildDocument> = restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments/$subdocumentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                log.error {
                    "HTTP ${response.statusCode.value()} error for subdocument $subdocumentId of document $documentId in case $caseId: $errorBody"
                }
                val errorMessage = if (response.statusCode.is4xxClientError)
                    "Underdokument ikke funnet"
                else
                    "Serverfeil ved henting av underdokument"
                throw ResponseStatusException(response.statusCode, errorMessage)
            }
            .toEntity<U020ChildDocument>()
        return entity.body!!
    }
}
