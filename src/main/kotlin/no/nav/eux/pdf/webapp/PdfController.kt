package no.nav.eux.pdf.webapp

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.eux.logging.clearLocalMdc
import no.nav.eux.logging.mdc
import no.nav.eux.pdf.service.U020PdfService
import no.nav.eux.pdf.service.U029PdfService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ContentDisposition
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
    val u020PdfService: U020PdfService,
    val u029PdfService: U029PdfService
) {

    private val log = logger {}

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
        mdc(rinasakId = caseId)
        val pdfBytes = u020PdfService.u020Pdf(caseId, documentId)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
            contentDisposition = ContentDisposition
                .attachment()
                .filename("U020-reimbursement-request.pdf")
                .build()
            contentLength = pdfBytes.size.toLong()
        }
        log.info { "PDF generated successfully" }
        clearLocalMdc()
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }


    @GetMapping("/rinasak/{caseId}/document/u029/{documentId}", produces = [APPLICATION_PDF_VALUE])
    @Operation(
        summary = "Generate U029 reimbursement contestation PDF",
        description = """
            Generates a PDF document for a U029 (Reimbursement Contestation) EESSI document.
            The PDF includes master document information (general info, contestation details) and 
            all related individual claims (child documents).
            
            The generated PDF contains:
            - Document metadata (case number, generation date)
            - General information (SED version, request ID, contestation ID, total amount)
            - Individual claims with person details, periods, amounts and contestation status
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
    fun getU029Pdf(
        @Parameter(
            description = "RINA case ID",
            example = "1452974",
            required = true
        )
        @PathVariable caseId: Int,
        @Parameter(
            description = "U029 master document ID (UUID format)",
            example = "9ed70cf2501049a3ad625cacc77e1087",
            required = true
        )
        @PathVariable documentId: String
    ): ResponseEntity<ByteArray> {
        mdc(rinasakId = caseId)
        val pdfBytes = u029PdfService.u029Pdf(caseId, documentId)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_PDF
            contentDisposition = ContentDisposition
                .attachment()
                .filename("U029-reimbursement-contestation.pdf")
                .build()
            contentLength = pdfBytes.size.toLong()
        }
        log.info { "PDF generated successfully" }
        clearLocalMdc()
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes)
    }
}
