package no.nav.eux.pdf.client

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import no.nav.eux.logging.mdc
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.model.domain.u020.U020ChildDocument
import no.nav.eux.pdf.model.domain.u020.U020MasterDocument
import no.nav.eux.pdf.model.domain.u020.U020SubdocumentsCollection
import no.nav.eux.pdf.model.domain.u029.U029ChildDocument
import no.nav.eux.pdf.model.domain.u029.U029MasterDocument
import no.nav.eux.pdf.model.domain.u029.U029SubdocumentsCollection
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
    val log = logger {}

    val casesUri: String by lazy { "${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases" }

    fun rinasak(rinasakId: Int): RinaCase = restClient
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

    fun u020MasterDocument(caseId: Int, documentId: String): U020MasterDocument = masterDocument(caseId, documentId)

    fun u020SubdocumentsCollection(caseId: Int, documentId: String): U020SubdocumentsCollection =
        subdocumentsCollection(caseId, documentId)

    fun u020ChildDocument(caseId: Int, documentId: String, subdocumentId: String): U020ChildDocument =
        childDocument(caseId, documentId, subdocumentId)

    fun u029MasterDocument(caseId: Int, documentId: String): U029MasterDocument = masterDocument(caseId, documentId)

    fun u029SubdocumentsCollection(caseId: Int, documentId: String): U029SubdocumentsCollection =
        subdocumentsCollection(caseId, documentId)

    fun u029ChildDocument(caseId: Int, documentId: String, subdocumentId: String): U029ChildDocument =
        childDocument(caseId, documentId, subdocumentId)

    private inline fun <reified T : Any> childDocument(caseId: Int, documentId: String, subdocumentId: String): T {
        val entity: ResponseEntity<T> = restClient
            .get()
            .uri("$casesUri/$caseId/Documents/$documentId/Subdocuments/$subdocumentId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.warn {
                    "HTTP ${response.statusCode.value()} client error for subdocument" +
                            " $subdocumentId of document $documentId: $errorBody"
                }
                throw ResponseStatusException(response.statusCode, "Underdokument ikke funnet")
            }
            .onStatus({ it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.error {
                    "HTTP ${response.statusCode.value()} server error for subdocument" +
                            " $subdocumentId of document $documentId: $errorBody"
                }
                throw ResponseStatusException(response.statusCode, "Serverfeil ved henting av underdokument")
            }
            .toEntity<T>()
        return entity.body!!
    }

    private inline fun <reified T : Any> masterDocument(caseId: Int, documentId: String): T {
        val entity: ResponseEntity<T> = restClient
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
            .toEntity()
        return entity.body!!
    }

    private inline fun <reified T : Any> subdocumentsCollection(caseId: Int, documentId: String): T {
        val entity: ResponseEntity<T> = restClient
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("$casesUri/$caseId/Documents/$documentId/Subdocuments")
                    .queryParam("currentDocument", 1)
                    .build()
            }
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", caseId.toString())
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.warn {
                    "HTTP ${response.statusCode.value()} client error for subdocuments of document " +
                            "$documentId: $errorBody"
                }
                throw ResponseStatusException(response.statusCode, "Underdokumenter ikke funnet")
            }
            .onStatus({ it.is5xxServerError }) { _, response ->
                val errorBody = String(response.body.readAllBytes())
                mdc(rinasakId = caseId)
                log.error {
                    "HTTP ${response.statusCode.value()} server error for subdocuments of document " +
                            "$documentId: $errorBody"
                }
                throw ResponseStatusException(response.statusCode, "Serverfeil ved henting av underdokumenter")
            }
            .toEntity<T>()
        return entity.body!!
    }
}
