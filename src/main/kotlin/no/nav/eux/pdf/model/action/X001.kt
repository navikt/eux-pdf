package no.nav.eux.pdf.model.action

import com.fasterxml.jackson.annotation.JsonProperty

data class X001(
    @JsonProperty("X001")
    val x001Body: X001Body
) {

    data class X001Body(
        val sedPackage: String,
        val sedGVer: String,
        val sedVer: String,
        @JsonProperty("CaseContext")
        val caseContext: CaseContext,
        @JsonProperty("InformationAboutClose")
        val informationAboutClose: InformationAboutClose
    )

    data class CaseContext(
        @JsonProperty("PersonContext")
        val personContext: PersonContext
    )

    data class PersonContext(
        val forename: String,
        val familyName: String,
        val sex: Sex?,
        val dateBirth: String
    )

    data class Sex(
        val value: List<String>
    )

    data class InformationAboutClose(
        val closeDate: String,
        val close: Close,
        val reasonForClosing: ReasonForClosing?,
        val pleaseProvideMoreDetailsIf99OtherSelected: String
    )

    data class Close(
        val value: List<String>
    )

    data class ReasonForClosing(
        val value: List<String>
    )
}

