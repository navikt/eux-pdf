package no.nav.eux.pdf.webapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("\${api.base-path:/api/v1}")
@RestController
class PdfController {

    @GetMapping("/test")
    fun test(): String {
        return "PDF Service is running - Test endpoint working!"
    }
}
