package no.nav.eux.pdf.integration

import no.nav.eux.pdf.Application
import no.nav.eux.pdf.integration.common.token
import no.nav.eux.pdf.integration.mock.RequestBodies
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.client.EntityExchangeResult
import org.springframework.test.web.servlet.client.RestTestClient
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureRestTestClient
abstract class AbstractPdfApiImplTest {

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    lateinit var restTestClient: RestTestClient

    @Autowired
    lateinit var requestBodies: RequestBodies

    @BeforeEach
    fun initialiseRestAssuredMockMvcWebApplicationContext() {
    }

     fun callPdfEndpoint(endpoint: String) =
        restTestClient.get().uri(endpoint)
            .header("Authorization", "Bearer ${mockOAuth2Server.token}")
            .exchange()
            .expectBody(ByteArray::class.java)
            .returnResult()

     fun callPdfEndpointExpectingError(endpoint: String) =
        restTestClient.get().uri(endpoint)
            .header("Authorization", "Bearer ${mockOAuth2Server.token}")
            .exchange()
            .expectBody(String::class.java)
            .returnResult()

     fun callUnauthenticatedEndpoint(endpoint: String) =
        restTestClient.get().uri(endpoint)
            .exchange()
            .expectBody(String::class.java)
            .returnResult()

     fun assertSuccessfulPdfResponse(response: EntityExchangeResult<ByteArray>) {
        assertEquals(HttpStatus.OK, response.status, "Should return HTTP 200")
        assertEquals(MediaType.APPLICATION_PDF, response.responseHeaders.contentType, "Should return PDF content type")
        assertNotNull(response.responseBody, "Response body should not be null")
    }

     fun validatePdfFormat(pdfBytes: ByteArray) {
        val pdfHeader = String(pdfBytes.take(4).toByteArray())
        assertEquals(PDF_MAGIC_BYTES, pdfHeader, "PDF should start with correct magic bytes")

        assertTrue(
            pdfBytes.size > MIN_PDF_SIZE,
            "PDF should have substantial content, was ${pdfBytes.size} bytes"
        )
    }

     fun validateContentDispositionHeader(response: EntityExchangeResult<ByteArray>, expectedFilename: String) {
        val contentDisposition = response.responseHeaders.contentDisposition
        assertNotNull(contentDisposition, "Content-Disposition header should be present")
        assertEquals("attachment", contentDisposition.type, "Should be attachment type")
        assertEquals(expectedFilename, contentDisposition.filename, "Should have correct filename")
    }

     fun savePdfForInspection(pdfBytes: ByteArray, testName: String, documentType: String) {
        val targetDir = File("target")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val fileName = "$documentType-IT-$testName-$timestamp.pdf"
        val pdfFile = File(targetDir, fileName)

        pdfFile.writeBytes(pdfBytes)
        println("💾 PDF saved to: ${pdfFile.absolutePath}")
    }

     fun logTestInfo(message: String, endpoint: String) {
        println("🧪 $message")
        println("📍 Mock server: http://localhost:9500")
        println("📍 Endpoint: $endpoint")
    }

     fun logTestSuccess(message: String, pdfSize: Int? = null) {
        val sizeInfo = pdfSize?.let { " (${it} bytes)" } ?: ""
        println("✅ $message$sizeInfo")
        println("📊 Mock requests captured: ${requestBodies.size}")
    }
    
     fun verifySubdocumentCallsContain(expectedSubdocuments: List<String>) {
        expectedSubdocuments.forEach { subdocId ->
            assertTrue(
                requestBodies.keys.any { it.contains("/Subdocuments/$subdocId") },
                "Subdocument endpoint for $subdocId should have been called"
            )
        }
    }
    
     fun verifySubdocumentsProcessed(
         expectedCount: Int,
         subdocumentFilter: (String) -> Boolean,
         documentType: String
     ) {
        val subdocumentCalls = requestBodies.keys.filter(subdocumentFilter)
        assertTrue(
            subdocumentCalls.size >= expectedCount,
            "All $expectedCount $documentType subdocuments should have been processed, " +
                    "found calls: ${subdocumentCalls.size}. Calls: $subdocumentCalls"
        )
    }
}
