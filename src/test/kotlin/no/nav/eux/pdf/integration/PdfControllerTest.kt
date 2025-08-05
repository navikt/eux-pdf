package no.nav.eux.pdf.integration

import no.nav.eux.pdf.integration.common.voidHttpEntity
import no.nav.eux.pdf.integration.config.TestConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Import(TestConfig::class)
@DisplayName("U020 PDF Generation Integration Tests")
class PdfControllerTest : AbstractPdfApiImplTest() {

    @BeforeEach
    fun setUp() {
        requestBodies.clear()
    }

    @Nested
    @DisplayName("Successful PDF Generation")
    inner class SuccessfulPdfGeneration {

        @Test
        @DisplayName("Should generate complete U020 PDF with all document components")
        fun shouldGenerateCompleteU020Pdf() {
            logTestInfo("Testing complete U020 PDF generation")
            val response = callPdfEndpoint(VALID_CASE_ID, VALID_DOCUMENT_ID)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!
            savePdfForInspection(pdfBytes, "complete-u020")

            verifyAllRinaEndpointsWereCalled()
            verifyExpectedSubdocumentsCalled()

            logTestSuccess("Successfully generated complete U020 PDF", pdfBytes.size)
        }

        @Test
        @DisplayName("Should handle multiple individual claims correctly")
        fun shouldHandleMultipleIndividualClaims() {
            logTestInfo("Testing PDF generation with multiple individual claims")
            val response = callPdfEndpoint(VALID_CASE_ID, VALID_DOCUMENT_ID)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!
            savePdfForInspection(pdfBytes, "multiple-claims")

            assertTrue(
                pdfBytes.size > 3000,
                "PDF with 3 individual claims should be substantial, was ${pdfBytes.size} bytes"
            )

            verifyAllSubdocumentsProcessed()
            logTestSuccess("Successfully handled multiple individual claims", pdfBytes.size)
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("Should return 404 when case is not found")
        fun shouldReturn404WhenCaseNotFound() {
            logTestInfo("Testing 404 response for non-existent case")
            val response = callPdfEndpointExpectingError(NON_EXISTENT_CASE_ID, VALID_DOCUMENT_ID)
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            logTestSuccess("Correctly returned 404 for non-existent case")
        }

        @Test
        @DisplayName("Should return 404 when document is not found")
        fun shouldReturn404WhenDocumentNotFound() {
            logTestInfo("Testing 404 response for non-existent document")
            val response = callPdfEndpointExpectingError(VALID_CASE_ID, NON_EXISTENT_DOCUMENT_ID)
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            logTestSuccess("Correctly returned 404 for non-existent document")
        }

        @Test
        @DisplayName("Should return 401 when no authentication token provided")
        fun shouldReturn401WhenNotAuthenticated() {
            logTestInfo("Testing 401 response for unauthenticated request")
            val response = restTemplate.exchange(
                "/api/v1/rinasak/$VALID_CASE_ID/document/u020/$VALID_DOCUMENT_ID",
                HttpMethod.GET,
                null,
                String::class.java
            )
            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
            logTestSuccess("Correctly returned 401 for unauthenticated request")
        }
    }

    @Nested
    @DisplayName("Response Validation")
    inner class ResponseValidation {

        @Test
        @DisplayName("Should return correct HTTP headers for PDF download")
        fun shouldReturnCorrectHttpHeaders() {
            logTestInfo("Testing PDF response headers")
            val response = callPdfEndpoint(VALID_CASE_ID, VALID_DOCUMENT_ID)
            assertSuccessfulPdfResponse(response)
            val contentDisposition = response.headers.contentDisposition
            assertNotNull(contentDisposition, "Content-Disposition header should be present")
            assertEquals("attachment", contentDisposition.type, "Should be attachment type")
            assertEquals(EXPECTED_FILENAME, contentDisposition.filename, "Should have correct filename")

            logTestSuccess("All HTTP headers are correct")
        }

        @Test
        @DisplayName("Should generate valid PDF format")
        fun shouldGenerateValidPdfFormat() {
            logTestInfo("Testing PDF format validation")
            val response = callPdfEndpoint(VALID_CASE_ID, VALID_DOCUMENT_ID)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!

            val pdfHeader = String(pdfBytes.take(4).toByteArray())
            assertEquals(PDF_MAGIC_BYTES, pdfHeader, "PDF should start with correct magic bytes")

            assertTrue(
                pdfBytes.size > MIN_PDF_SIZE,
                "PDF should have substantial content, was ${pdfBytes.size} bytes"
            )

            savePdfForInspection(pdfBytes, "format-validation")
            logTestSuccess("PDF format is valid", pdfBytes.size)
        }
    }

    private fun callPdfEndpoint(caseId: Int, documentId: String) =
        restTemplate.exchange(
            "/api/v1/rinasak/$caseId/document/u020/$documentId",
            HttpMethod.GET,
            voidHttpEntity(mockOAuth2Server),
            ByteArray::class.java
        )

    private fun callPdfEndpointExpectingError(caseId: Int, documentId: String) =
        restTemplate.exchange(
            "/api/v1/rinasak/$caseId/document/u020/$documentId",
            HttpMethod.GET,
            voidHttpEntity(mockOAuth2Server),
            String::class.java
        )

    private fun assertSuccessfulPdfResponse(response: org.springframework.http.ResponseEntity<ByteArray>) {
        assertEquals(HttpStatus.OK, response.statusCode, "Should return HTTP 200")
        assertEquals(MediaType.APPLICATION_PDF, response.headers.contentType, "Should return PDF content type")
        assertNotNull(response.body, "Response body should not be null")
    }

    private fun verifyAllRinaEndpointsWereCalled() {
        assertTrue(
            requestBodies.keys.any {
                it.contains("/eessiRest/Cases/$VALID_CASE_ID/Documents/$VALID_DOCUMENT_ID") && !it.contains(
                    "/Subdocuments"
                )
            },
            "Master document endpoint should have been called"
        )

        assertTrue(
            requestBodies.keys.any {
                it.contains("/eessiRest/Cases/$VALID_CASE_ID/Documents/$VALID_DOCUMENT_ID/Subdocuments") && !it.contains(
                    "subdoc_"
                )
            },
            "Subdocuments collection endpoint should have been called"
        )
    }

    private fun verifyExpectedSubdocumentsCalled() {
        val expectedSubdocuments = listOf("subdoc_001", "subdoc_002", "subdoc_003")
        expectedSubdocuments.forEach { subdocId ->
            assertTrue(
                requestBodies.keys.any { it.contains("/Subdocuments/$subdocId") },
                "Subdocument endpoint for $subdocId should have been called"
            )
        }
    }

    private fun verifyAllSubdocumentsProcessed() {
        val subdocumentCalls = requestBodies.keys.filter { it.contains("/Subdocuments/subdoc_") }
        assertTrue(
            subdocumentCalls.size >= 3,
            "All 3 subdocuments should have been processed, " +
                    "found calls: ${subdocumentCalls.size}. Calls: $subdocumentCalls"
        )
    }

    private fun savePdfForInspection(pdfBytes: ByteArray, testName: String) {
        val targetDir = File("target")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val timestamp = System.currentTimeMillis()
        val fileName = "U020-IT-$testName-$timestamp.pdf"
        val pdfFile = File(targetDir, fileName)

        pdfFile.writeBytes(pdfBytes)
        println("💾 PDF saved to: ${pdfFile.absolutePath}")
    }

    private fun logTestInfo(message: String) {
        println("🧪 $message")
        println("📍 Mock server: http://localhost:9500")
        println("📍 Endpoint: /api/v1/rinasak/$VALID_CASE_ID/document/u020/$VALID_DOCUMENT_ID")
    }

    private fun logTestSuccess(message: String, pdfSize: Int? = null) {
        val sizeInfo = pdfSize?.let { " (${it} bytes)" } ?: ""
        println("✅ $message$sizeInfo")
        println("📊 Mock requests captured: ${requestBodies.size}")
    }
}