package no.nav.eux.pdf.model.rinasak

data class RinaParticipant(
    val role: String?,
    val organisation: Map<String, Any>?,
    val selected: Boolean?
)