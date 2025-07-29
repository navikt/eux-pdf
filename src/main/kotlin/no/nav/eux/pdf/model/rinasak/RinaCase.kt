package no.nav.eux.pdf.model.rinasak

data class RinaCase(
    val id: Int,
    val subject: RinaSubject?,
    val actions: List<RinaCaseAction>,
    val processDefinitionName: String?,
    val documents: List<RinaDocument>?,
    val participants: List<RinaParticipant>?
)
