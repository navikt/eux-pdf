package no.nav.eux.pdf.model.rinasak

data class RinaCaseAction(
    val id: String,
    val name: String,
    val template: String,
    val operation: String,
    val documentType: String?,
)
