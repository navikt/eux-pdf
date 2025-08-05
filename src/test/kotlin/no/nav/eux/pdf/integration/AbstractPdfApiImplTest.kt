package no.nav.eux.pdf.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eux.pdf.integration.common.httpEntity
import no.nav.eux.pdf.Application
import no.nav.eux.pdf.integration.mock.RequestBodies
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AbstractPdfApiImplTest {

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var requestBodies: RequestBodies

    @BeforeEach
    fun initialiseRestAssuredMockMvcWebApplicationContext() {
    }

    val String.jsonNode: JsonNode get() = ObjectMapper().readTree(this)

    val <T> T.httpEntity: HttpEntity<T>
        get() = httpEntity(mockOAuth2Server)
}
