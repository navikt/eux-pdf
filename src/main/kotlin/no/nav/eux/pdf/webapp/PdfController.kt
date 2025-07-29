package no.nav.eux.pdf.webapp

import no.nav.eux.pdf.service.U020PdfService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("\${api.base-path:/api/v1}")
@RestController
@Protected
class PdfController(
    private val u020PdfService: U020PdfService
) {

    @GetMapping("/test")
    fun test(): String {
        return "PDF Service is running - Test endpoint working!"
    }

    @GetMapping("/u020/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getU020Pdf(): ResponseEntity<ByteArray> {
        val pdfBytes = u020PdfService.u020Pdf()

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
}
