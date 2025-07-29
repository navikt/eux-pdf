package no.nav.eux.pdf.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class IntegrationConfig {

    @Bean
    fun restClient(
        authenticationInterceptor: AuthenticationInterceptor
    ): RestClient = RestClient.builder()
        .requestInterceptor(authenticationInterceptor)
        .build()

    @Bean
    fun authenticationInterceptor(
        rinaCpiServiceProperties: RinaCpiServiceProperties,
        serviceuserCredentials: ServiceuserCredentials
    ) = AuthenticationInterceptor(rinaCpiServiceProperties, serviceuserCredentials)

}
