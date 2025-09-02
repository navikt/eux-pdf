package no.nav.eux.pdf.service

import no.nav.eux.pdf.client.RinaClient
import org.springframework.stereotype.Service

@Service
class U029PdfService(
    val rinaClient: RinaClient
) {

    fun u029Pdf(
        caseId: Int,
        documentId: String
    ): ByteArray {
        TODO("Implement service logic similar to how it is for u020")
    }

}
