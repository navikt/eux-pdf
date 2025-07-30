package no.nav.eux.pdf.webapp

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.eux.pdf.client.RinaClient
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterDocument
import no.nav.eux.pdf.model.domain.U020SubdocumentsCollection
import no.nav.eux.pdf.model.rinasak.RinaCase
import no.nav.eux.pdf.service.U020PdfService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("\${api.base-path:/api/v1}")
@RestController
@Protected
@Tag(name = "PDF Generation", description = "API for generating PDF documents from RINA/EESSI data")
class PdfController(
    private val u020PdfService: U020PdfService,
    private val rinaClient: RinaClient
) {

    @GetMapping("/test")
    @Operation(
        summary = "Health check endpoint",
        description = "Simple endpoint to verify that the PDF service is running and accessible"
    )
    @ApiResponse(responseCode = "200", description = "Service is running")
    fun test(): String {
        return "PDF Service is running - Test endpoint working!"
    }

    @GetMapping("/rinasak/{caseId}/document/u020/{documentId}", produces = [APPLICATION_PDF_VALUE])
    @Operation(
        summary = "Generate U020 reimbursement request PDF",
        description = """
            Generates a PDF document for a U020 (Reimbursement Request) EESSI document.
            The PDF includes master document information (general info, bank details) and 
            all related individual claims (child documents).
            
            The generated PDF contains:
            - Document metadata (case number, generation date)
            - General information (SED version, request ID, total amount)
            - Bank information (IBAN, reference)
            - Individual claims with person details, periods, and amounts
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "PDF generated successfully",
                content = [Content(
                    mediaType = APPLICATION_PDF_VALUE,
                    schema = Schema(type = "string", format = "binary")
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Case, document, or subdocuments not found"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - valid JWT token required"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error during PDF generation or RINA communication"
            )
        ]
    )
    fun getU020Pdf(
        @Parameter(
            description = "RINA case ID",
            example = "1451675",
            required = true
        )
        @PathVariable caseId: Int,
        @Parameter(
            description = "U020 master document ID (UUID format)",
            example = "f0293bae3c494391851a76d0f6f82f46",
            required = true
        )
        @PathVariable documentId: String
    ): ResponseEntity<ByteArray> {
        val pdfBytes = u020PdfService.u020Pdf(caseId, documentId)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
            contentDisposition = org.springframework.http.ContentDisposition
                .attachment()
                .filename("U020-reimbursement-request.pdf")
                .build()
            contentLength = pdfBytes.size.toLong()
        }
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }

    @GetMapping("/rinasak/{rinasakId}")
    fun getRinaCase(@PathVariable rinasakId: Int): ResponseEntity<RinaCase> {
        val rinaCase = rinaClient.rinasak(rinasakId)
        return ResponseEntity.ok(rinaCase)
    }

    @GetMapping("/rinasak/{caseId}/document/{documentId}")
    fun getDocument(
        @PathVariable caseId: Int,
        @PathVariable documentId: String
    ): ResponseEntity<U020MasterDocument> {
        val document = rinaClient.getDocument(caseId, documentId)
        return ResponseEntity.ok(document)
    }

    @GetMapping("/rinasak/{caseId}/document/{documentId}/subdocuments")
    fun getSubdocuments(
        @PathVariable caseId: Int,
        @PathVariable documentId: String
    ): ResponseEntity<U020SubdocumentsCollection> {
        val subdocuments = rinaClient.getSubdocuments(caseId, documentId)
        return ResponseEntity.ok(subdocuments)
    }

    @GetMapping("/rinasak/{caseId}/document/{documentId}/subdocuments/{subdocumentId}")
    fun getSubdocument(
        @PathVariable caseId: Int,
        @PathVariable documentId: String,
        @PathVariable subdocumentId: String
    ): ResponseEntity<U020ChildDocument> {
        val subdocument = rinaClient.getSubdocument(caseId, documentId, subdocumentId)
        return ResponseEntity.ok(subdocument)
    }
}
