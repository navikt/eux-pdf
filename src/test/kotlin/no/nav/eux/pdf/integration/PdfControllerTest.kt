package no.nav.eux.pdf.integration

import no.nav.eux.pdf.integration.common.voidHttpEntity
import no.nav.eux.pdf.integration.config.TestConfig
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Import(TestConfig::class)
class PdfControllerTest: AbstractPdfApiImplTest() {

    @Test
    fun `should call U020 PDF endpoint and return PDF`() {
        // Given
        val caseId = 123456
        val documentId = "f0293bae3c494391851a76d0f6f82f46"

        // Debug: Clear any previous request bodies and log configuration
        requestBodies.clear()
        println("🔧 Mock server should be running on: http://localhost:9500")
        println("🔧 Test will call: /api/v1/rinasak/$caseId/document/u020/$documentId")

        // When - call the PDF generation endpoint
        val response = try {
            restTemplate.exchange(
                "/api/v1/rinasak/$caseId/document/u020/$documentId",
                HttpMethod.GET,
                voidHttpEntity(mockOAuth2Server),
                ByteArray::class.java
            )
        } catch (e: Exception) {
            println("❌ Exception occurred: ${e.message}")
            println("📋 Request bodies captured by mock server:")
            requestBodies.forEach { (path, body) ->
                println("  - $path: ${body.take(100)}${if (body.length > 100) "..." else ""}")
            }
            throw e
        }

        // Then - verify we get a successful PDF response
        println("✅ Response status: ${response.statusCode}")
        println("📊 Response headers: ${response.headers}")
        println("📄 Response body size: ${response.body?.size ?: 0} bytes")

        // Debug: Show what requests were captured
        println("📋 Requests captured by mock server:")
        requestBodies.forEach { (path, body) ->
            println("  - $path: ${body.take(100)}${if (body.length > 100) "..." else ""}")
        }

        // Verify successful response
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(MediaType.APPLICATION_PDF, response.headers.contentType)

        // Verify PDF content
        val pdfBytes = response.body
        assertNotNull(pdfBytes)
        assertTrue(pdfBytes.size > 1000, "PDF should have substantial content, was ${pdfBytes.size} bytes")

        // Verify PDF header (PDF files start with %PDF-)
        val pdfHeader = String(pdfBytes.take(4).toByteArray())
        assertEquals("%PDF", pdfHeader)

        // Verify content disposition header for download
        val contentDisposition = response.headers.contentDisposition
        assertNotNull(contentDisposition)
        assertEquals("attachment", contentDisposition.type)
        assertEquals("U020-reimbursement-request.pdf", contentDisposition.filename)

        // Save the PDF to target/ directory for inspection
        val targetDir = java.io.File("target")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val pdfFile = java.io.File(targetDir, "U020-IT-generated-${System.currentTimeMillis()}.pdf")
        pdfFile.writeBytes(pdfBytes)
        println("💾 PDF saved to: ${pdfFile.absolutePath}")

        println("✅ Successfully generated PDF with ${pdfBytes.size} bytes")
    }
}