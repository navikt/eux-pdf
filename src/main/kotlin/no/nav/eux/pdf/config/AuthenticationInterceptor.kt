package no.nav.eux.pdf.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import org.springframework.web.server.ResponseStatusException

const val casServiceId = "../portal_new/cas/cpi"
private const val requestHeaderXsrfToken = "X-XSRF-TOKEN"
private const val headerSetCookie = "Set-Cookie"
private const val headerXAuthCookie = "X-Auth-Cookie"

open class AuthenticationInterceptor(
    private val rinaCpiServiceProperties: RinaCpiServiceProperties,
    private val serviceuserCredentials: ServiceuserCredentials
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val serviceticketUrl = serviceticketUrl()
        val serviceticket = serviceticket(serviceticketUrl)
        val headers = cpiHttpHeaders(serviceticket)
        val xauthCookie = headers.getFirst(headerXAuthCookie)
        val jsessionId = headers.getFirst(headerSetCookie)
        request.headers.add(HttpHeaders.COOKIE, jsessionId)
        request.headers.set(requestHeaderXsrfToken, xauthCookie)
        return execution.execute(request, body)
    }

    private fun serviceticketUrl() =
        RestClient.builder().baseUrl(rinaCpiServiceProperties.rinaBaseUrl).build()
            .post()
            .uri("/eessiCas/v1/tickets")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("username=${serviceuserCredentials.username}&password=${serviceuserCredentials.password}")
            .retrieve()
            .toEntity<String>()
            .headers
            .location
            .toString()
            .replace(rinaCpiServiceProperties.replaceOld, rinaCpiServiceProperties.replaceNew)

    private fun serviceticket(serviceticketUrl: String) =
        RestClient.builder().baseUrl(serviceticketUrl).build()
            .post()
            .uri("?service=$casServiceId")
            .retrieve()
            .toEntity<String>()
            .body ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tom body. Forventet serviceticket")

    open fun cpiHttpHeaders(serviceticket: String) =
        RestClient.builder().baseUrl(rinaCpiServiceProperties.rinaBaseUrl).build()
            .get()
            .uri("/eessiRest/login/cas?ticket=$serviceticket&serviceId=$casServiceId")
            .retrieve()
            .toEntity<String>()
            .headers
}
