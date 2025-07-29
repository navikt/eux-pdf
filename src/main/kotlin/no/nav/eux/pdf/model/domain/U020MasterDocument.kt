package no.nav.eux.pdf.model.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class U020MasterDocument(
    @JsonProperty("U020_Master")
    val u020Master: U020MasterContent
)

data class U020MasterContent(
    val sedGVer: String,
    val sedPackage: String,
    val sedVer: String,
    @JsonProperty("GeneralInformation")
    val generalInformation: GeneralInformation
)

data class GeneralInformation(
    val reimbursementRequestID: String,
    val numberIndividualClaims: String,
    @JsonProperty("TotalAmountRequested")
    val totalAmountRequested: TotalAmountRequested,
    @JsonProperty("BankInformation")
    val bankInformation: BankInformation
)

data class TotalAmountRequested(
    val amount: String,
    val currency: Currency
)

data class Currency(
    val value: List<String>
)

data class BankInformation(
    @JsonProperty("SEPABankDetails")
    val sepaBankDetails: SepaBankDetails,
    val bankTransferSubjectOrTransactionReference: String
)

data class SepaBankDetails(
    @JsonProperty("IBAN")
    val iban: String
)
