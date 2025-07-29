package no.nav.eux.pdf.webapp

import no.nav.eux.pdf.client.RinaClient
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterDocument
import no.nav.eux.pdf.model.domain.U020SubdocumentsCollection
import no.nav.eux.pdf.model.rinasak.RinaCase
import no.nav.eux.pdf.service.U020PdfService
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("\${api.base-path:/api/v1}")
@RestController
@Protected
class PdfController(
    private val u020PdfService: U020PdfService,
    private val rinaClient: RinaClient
) {

    @GetMapping("/test")
    fun test(): String {
        return "PDF Service is running - Test endpoint working!"
    }

    @GetMapping("/rinasak/{caseId}/document/u020/{documentId}", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun getU020Pdf(
        @PathVariable caseId: Int,
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
