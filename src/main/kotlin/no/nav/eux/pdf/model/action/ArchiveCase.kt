package no.nav.eux.pdf.model.action

data class ArchiveCase(
    val id: String,
    val processDefinitionName: String,
    val startDate: String,
    val lastUpdate: String,
    val participants: List<Participant>,
    val status: String,
    val subject: Subject
) {
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

    data class Subject(
        val pid: String?,
        val name: String?,
        val surname: String?,
        val birthday: String?,
        val sex: String?,
        val contactMethods: List<Any>?
    )
}

