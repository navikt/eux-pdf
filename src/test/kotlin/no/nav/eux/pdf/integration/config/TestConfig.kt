package no.nav.eux.pdf.integration.config

import no.nav.eux.pdf.config.AuthenticationInterceptor
import no.nav.eux.pdf.config.RinaCpiServiceProperties
import no.nav.eux.pdf.config.ServiceuserCredentials
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun testRinaCpiServiceProperties() = RinaCpiServiceProperties(
        rinaBaseUrl = "http://localhost:9500"
    )

    @Bean
    @Primary
    fun testServiceuserCredentials() = ServiceuserCredentials(
        username = "testuser",
        password = "testpassword"
    )

    @Bean
    @Primary
    fun testAuthenticationInterceptor(
        rinaCpiServiceProperties: RinaCpiServiceProperties,
        serviceuserCredentials: ServiceuserCredentials
    ) = TestAuthenticationInterceptor(rinaCpiServiceProperties, serviceuserCredentials)

    @Bean
    @Primary
    fun testRestClient(authenticationInterceptor: AuthenticationInterceptor): RestClient =
        RestClient.builder()
            .baseUrl("http://localhost:9500")
            .requestInterceptor(authenticationInterceptor)
            .build()
}

class TestAuthenticationInterceptor(
    rinaCpiServiceProperties: RinaCpiServiceProperties,
    serviceuserCredentials: ServiceuserCredentials
) : AuthenticationInterceptor(rinaCpiServiceProperties, serviceuserCredentials) {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.add(HttpHeaders.COOKIE, "JSESSIONID=ABCD1234; Path=/; HttpOnly")
        request.headers.set("X-XSRF-TOKEN", "xauth-token-123")
        return execution.execute(request, body)
    }
}
