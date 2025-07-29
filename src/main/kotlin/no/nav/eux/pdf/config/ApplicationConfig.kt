package no.nav.eux.pdf.config

import no.nav.eux.logging.RequestIdMdcFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(RinaCpiServiceProperties::class, ServiceuserCredentials::class)
class ApplicationConfig {

    @Bean
    fun requestIdMdcFilter() = RequestIdMdcFilter()

}
