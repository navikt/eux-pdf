package no.nav.eux.pdf

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springdoc.core.configuration.SpringDocConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@EnableJwtTokenValidation(
    ignore = [
        "org.springframework",
        "org.springdoc"
    ]
)
@SpringBootApplication
@Import(value = [SpringDocConfiguration::class])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
