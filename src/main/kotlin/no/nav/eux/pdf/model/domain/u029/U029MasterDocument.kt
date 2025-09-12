package no.nav.eux.pdf.model.domain.u029

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.eux.pdf.model.domain.Country
import no.nav.eux.pdf.model.domain.Institution

data class U029MasterDocument(
    @JsonProperty("U029_Master")
    val u029Master: U029MasterContent?
)

data class U029MasterContent(
    val sedGVer: String?,
    val sedPackage: String?,
    val sedVer: String?,
    @JsonProperty("LocalCaseNumbers")
    val localCaseNumbers: LocalCaseNumbers? = null,
    @JsonProperty("GeneralInformation")
    val generalInformation: GeneralInformation
)

data class LocalCaseNumbers(
    @JsonProperty("LocalCaseNumber")
    val localCaseNumber: List<LocalCaseNumber>? = null
)

data class LocalCaseNumber(
    val country: Country,
    val caseNumber: String,
    @JsonProperty("Institution")
    val institution: Institution?
)

data class GeneralInformation(
    val reimbursementRequestID: String,
    val reimbursementContestationID: String,
    val amendedReimbursementRequestID: String,
    @JsonProperty("UpdatedTotalAmountRequested")
    val updatedTotalAmountRequested: UpdatedTotalAmountRequested
)

data class UpdatedTotalAmountRequested(
    val amount: String,
    val currency: Currency
)

data class Currency(
    val value: List<String>
)
