package no.nav.eux.pdf.integration.common

import com.nimbusds.jose.JOSEObjectType
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun <T> T.httpEntity(mockOAuth2Server: MockOAuth2Server) =
    HttpEntity<T>(this, mockOAuth2Server.httpHeaders)

fun voidHttpEntity(mockOAuth2Server: MockOAuth2Server) =
    HttpEntity<Void>(mockOAuth2Server.httpHeaders)

val MockOAuth2Server.httpHeaders: HttpHeaders
    get() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer ${this.token}")
        return headers
    }

val MockOAuth2Server.token: String
    get() = this
        .issueToken("issuer1", "theclientid", defaultOAuth2TokenCallback)
        .serialize()

var defaultOAuth2TokenCallback =
    DefaultOAuth2TokenCallback(
        "issuer1",
        "subject1",
        JOSEObjectType.JWT.type,
        listOf("demoapplication"),
        emptyMap(),
        3600
    )
