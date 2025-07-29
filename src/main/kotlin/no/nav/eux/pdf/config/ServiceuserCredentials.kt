package no.nav.eux.pdf.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "credentials")
class ServiceuserCredentials(
    val username: String,
    val password: String
)