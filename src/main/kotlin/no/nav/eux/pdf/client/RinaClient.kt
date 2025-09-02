package no.nav.eux.pdf.client

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.logging.mdc
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterDocument
import no.nav.eux.pdf.model.domain.U020SubdocumentsCollection
import no.nav.eux.pdf.model.rinasak.RinaCase
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import org.springframework.web.server.ResponseStatusException

@Component
class RinaClient(
    val rinaCpiServiceProperties: RinaCpiServiceProperties,
    val restClient: RestClient,
) {
    val log = logger {}

    val casesUri: String by lazy { "${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases" }

    fun rinasak(rinasakId: Int): RinaCase =
        restClient
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
            .body!!

    fun u020MasterDocument(caseId: Int, documentId: String): U020MasterDocument =
        restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.warn { "HTTP ${response.statusCode.value()} client error for document $documentId: $errorBody" }
                throw ResponseStatusException(response.statusCode, "Dokument ikke funnet")
            }
            .onStatus({ it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.error { "HTTP ${response.statusCode.value()} server error for document $documentId: $errorBody" }
                throw ResponseStatusException(response.statusCode, "Serverfeil ved henting av dokument")
            }
            .toEntity<U020MasterDocument>()
            .body!!

    fun u020SubdocumentsCollection(caseId: Int, documentId: String): U020SubdocumentsCollection = restClient
        .get()
        .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments")
        .accept(APPLICATION_JSON)
        .header("Nav-Call-Id", caseId.toString())
        .retrieve()
        .onStatus({ it.is4xxClientError }) { _, response ->
            val errorBody = String(response.body.readAllBytes())
            mdc(rinasakId = caseId)
            log.warn { "HTTP ${response.statusCode.value()} client error for subdocuments of document $documentId: $errorBody" }
            throw ResponseStatusException(response.statusCode, "Underdokumenter ikke funnet")
        }
        .onStatus({ it.is5xxServerError }) { _, response ->
            val errorBody = String(response.body.readAllBytes())
            mdc(rinasakId = caseId)
            log.error { "HTTP ${response.statusCode.value()} server error for subdocuments of document $documentId: $errorBody" }
            throw ResponseStatusException(response.statusCode, "Serverfeil ved henting av underdokumenter")
        }
        .toEntity<U020SubdocumentsCollection>()
        .body!!

    fun u020ChildDocument(caseId: Int, documentId: String, subdocumentId: String): U020ChildDocument =
        restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments/$subdocumentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.warn { "HTTP ${response.statusCode.value()} client error for subdocument $subdocumentId of document $documentId: $errorBody" }
                throw ResponseStatusException(response.statusCode, "Underdokument ikke funnet")
            }
            .onStatus({ it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.error { "HTTP ${response.statusCode.value()} server error for subdocument $subdocumentId of document $documentId: $errorBody" }
                throw ResponseStatusException(response.statusCode, "Serverfeil ved henting av underdokument")
            }
            .toEntity<U020ChildDocument>()
            .body!!
}
