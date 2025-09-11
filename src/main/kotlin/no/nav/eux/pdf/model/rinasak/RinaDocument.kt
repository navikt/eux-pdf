package no.nav.eux.pdf.model.rinasak

import java.time.LocalDateTime

data class RinaDocument(
    val status: String?,
    val type: String?,
    val creationDate: LocalDateTime?,
)