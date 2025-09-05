package no.nav.eux.pdf.integration

import no.nav.eux.pdf.integration.config.TestConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Import(TestConfig::class)
@DisplayName("U029 PDF Generation Integration Tests")
class PdfControllerU029Test : AbstractPdfApiImplTest() {

    @BeforeEach
    fun setUp() {
        requestBodies.clear()
    }

    @Nested
    @DisplayName("Successful PDF Generation")
    inner class SuccessfulPdfGeneration {

        @Test
        @DisplayName("Should generate complete U029 PDF with all document components")
        fun shouldGenerateCompleteU029Pdf() {
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing complete U029 PDF generation", endpoint)
            val response = callPdfEndpoint(endpoint)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!
            savePdfForInspection(pdfBytes, "complete-u029", "U029")

            verifyAllRinaEndpointsWereCalled()
            verifyExpectedSubdocumentsCalled()

            logTestSuccess("Successfully generated complete U029 PDF", pdfBytes.size)
        }

        @Test
        @DisplayName("Should handle multiple individual claims correctly")
        fun shouldHandleMultipleIndividualClaims() {
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing PDF generation with multiple individual claims", endpoint)
            val response = callPdfEndpoint(endpoint)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!
            savePdfForInspection(pdfBytes, "multiple-claims", "U029")

            assertTrue(
                pdfBytes.size > 3000,
                "PDF with 2 individual claims should be substantial, was ${pdfBytes.size} bytes"
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
            val endpoint = "/api/v1/rinasak/$NON_EXISTENT_CASE_ID/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing 404 response for non-existent case", endpoint)
            val response = callPdfEndpointExpectingError(endpoint)
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            logTestSuccess("Correctly returned 404 for non-existent case")
        }

        @Test
        @DisplayName("Should return 404 when document is not found")
        fun shouldReturn404WhenDocumentNotFound() {
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$NON_EXISTENT_DOCUMENT_ID"
            logTestInfo("Testing 404 response for non-existent document", endpoint)
            val response = callPdfEndpointExpectingError(endpoint)
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            logTestSuccess("Correctly returned 404 for non-existent document")
        }

        @Test
        @DisplayName("Should return 401 when no authentication token provided")
        fun shouldReturn401WhenNotAuthenticated() {
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing 401 response for unauthenticated request", endpoint)
            val response = callUnauthenticatedEndpoint(endpoint)
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
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing PDF response headers", endpoint)
            val response = callPdfEndpoint(endpoint)
            assertSuccessfulPdfResponse(response)
            validateContentDispositionHeader(response, EXPECTED_FILENAME_U029)

            logTestSuccess("All HTTP headers are correct")
        }

        @Test
        @DisplayName("Should generate valid PDF format")
        fun shouldGenerateValidPdfFormat() {
            val endpoint = "/api/v1/rinasak/$VALID_CASE_ID_U029/document/u029/$VALID_DOCUMENT_ID_U029"
            logTestInfo("Testing PDF format validation", endpoint)
            val response = callPdfEndpoint(endpoint)
            assertSuccessfulPdfResponse(response)

            val pdfBytes = response.body!!
            validatePdfFormat(pdfBytes)

            savePdfForInspection(pdfBytes, "format-validation", "U029")
            logTestSuccess("PDF format is valid", pdfBytes.size)
        }
    }

    private fun verifyAllRinaEndpointsWereCalled() {
        assertTrue(
            requestBodies.keys.any {
                it.contains("/eessiRest/Cases/$VALID_CASE_ID_U029/Documents/$VALID_DOCUMENT_ID_U029") && !it.contains(
                    "/Subdocuments"
                )
            },
            "U029 master document endpoint should have been called"
        )

        assertTrue(
            requestBodies.keys.any {
                it.contains("/eessiRest/Cases/$VALID_CASE_ID_U029/Documents/$VALID_DOCUMENT_ID_U029/Subdocuments") && !it.contains(
                    U029_CHILD_DOCUMENT_ID_1
                ) && !it.contains(U029_CHILD_DOCUMENT_ID_2)
            },
            "U029 subdocuments collection endpoint should have been called"
        )
    }

    private fun verifyExpectedSubdocumentsCalled() {
        val expectedSubdocuments = listOf(U029_CHILD_DOCUMENT_ID_1, U029_CHILD_DOCUMENT_ID_2)
        verifySubdocumentCallsContain(expectedSubdocuments)
    }

    private fun verifyAllSubdocumentsProcessed() {
        verifySubdocumentsProcessed(
            expectedCount = 2,
            subdocumentFilter = {
                it.contains("/Subdocuments/") &&
                (it.contains(U029_CHILD_DOCUMENT_ID_1) || it.contains(U029_CHILD_DOCUMENT_ID_2))
            },
            documentType = "U029"
        )
    }
}