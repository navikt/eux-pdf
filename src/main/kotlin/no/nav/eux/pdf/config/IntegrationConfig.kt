package no.nav.eux.pdf.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class IntegrationConfig {

    @Bean
    fun rinaRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        authenticationInterceptor: AuthenticationInterceptor
    ): RestTemplate = restTemplateBuilder
        .additionalInterceptors(authenticationInterceptor)
        .build()

    @Bean
    fun authenticationInterceptor(
        rinaCpiServiceProperties: RinaCpiServiceProperties,
        serviceuserCredentials: ServiceuserCredentials
    ) = AuthenticationInterceptor(rinaCpiServiceProperties, serviceuserCredentials)

}
