package no.nav.eux.pdf.model.action

data class SendRinaDocument(
    val id: String,
    val caseId: String,
    val type: String,
    val conversations: List<Conversation>
) {
    data class Conversation(
        val id: String,
        val participants: List<Participant>
    )

    data class Participant(
        val organisation: Organisation,
        val role: String
    )

    data class Organisation(
        val id: String,
        val acronym: String,
        val name: String,
        val countryCode: String,
        val activeSince: String
    )
}
