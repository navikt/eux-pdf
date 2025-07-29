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

    fun hentRinasak(rinasakId: Int): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .get()
            .uri(hentRinasakUri(rinasakId))
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", rinasakId.toString())
            .retrieve()
            .toEntity()
        when {
            entity.statusCode.is2xxSuccessful -> return entity.body!!
            else -> throw hentRinasakException(rinasakId, entity)
        }
    }

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

    fun rinasakString(rinasakId: Int): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .get()
            .uri("$casesUri/$rinasakId")
            .accept(APPLICATION_JSON)
            .header("Nav-Call-Id", rinasakId.toString())
            .retrieve()
            .toEntity<String>()
        return entity.body!!
    }

    fun oppdaterRinasak(
        rinasakId: Int,
        actionId: String,
        bodyJson: String
    ): String {
        val entity: ResponseEntity<String> = rinaRestClient
            .put()
            .uri(oppdaterRinasakUri(rinasakId, actionId))
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .body(bodyJson)
            .retrieve()
            .toEntity()
        when {
            entity.statusCode.is2xxSuccessful -> return entity.body!!
            else -> throw oppdaterRinasakException(rinasakId, entity)
        }
    }

    fun hentRinasakUri(
        rinasakId: Int
    ) =
        UriComponentsBuilder
            .fromHttpUrl("${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases/")
            .path(rinasakId.toString())
            .build()
            .toUri()

    fun oppdaterRinasakUri(
        rinasakId: Int,
        actionId: String
    ) =
        UriComponentsBuilder
            .fromHttpUrl("${rinaCpiServiceProperties.rinaBaseUrl}/eessiRest/Cases/")
            .path(rinasakId.toString())
            .path("/Actions/")
            .path(actionId)
            .path("/Document")
            .queryParam("rinasakId", rinasakId)
            .build()
            .toUri()

    fun hentRinasakException(
        rinasakId: Int,
        entity: ResponseEntity<String>
    ) = rinaException("Feil under henting av rinasak. rinasakId=$rinasakId", entity.body)

    fun oppdaterRinasakException(
        rinasakId: Int,
        entity: ResponseEntity<String>
    ) = rinaException("Feil under oppdatering av rinasak=$rinasakId", entity.body)

    fun rinaException(
        msg: String,
        body: Any?
    ): RuntimeException {
        log.error { "$msg, body=$body" }
        return RuntimeException(msg)
    }
}
