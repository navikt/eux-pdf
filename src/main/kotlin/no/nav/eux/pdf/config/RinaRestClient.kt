package no.nav.eux.pdf.config

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@Component
class RinaRestClient(
    val rinaRestTemplate: RestTemplate,
) {

    fun restClient() = RestClient.create(restTemplate())

    fun post() = restClient().post()

    fun put() = restClient().put()

    fun get() = restClient().get()

    fun restTemplate() =
            rinaRestTemplate

}
