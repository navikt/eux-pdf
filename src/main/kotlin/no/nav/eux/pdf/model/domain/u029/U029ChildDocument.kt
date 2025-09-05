package no.nav.eux.pdf.model.domain.u029

import com.fasterxml.jackson.annotation.JsonProperty

data class U029ChildDocument(
    @JsonProperty("U029_Child")
    val u029Child: U029ChildContent
)

data class U029ChildContent(
    @JsonProperty("IndividualClaim")
    val individualClaim: IndividualClaim
)

data class IndividualClaim(
    val reimbursementRequestID: String,
    val reimbursementContestationID: String,
    val amendedReimbursementRequestID: String,
    val sequentialNumberIndividualClaim: String,
    val contestedIndividualClaimID: String? = null,
    val amendedContestedIndividualClaimID: String? = null,
    @JsonProperty("IndividualClaimStatus")
    val individualClaimStatus: IndividualClaimStatus,
    @JsonProperty("Person")
    val person: Person,
    @JsonProperty("InstitutionWhichCertifiedInsuranceRecord")
    val institutionWhichCertifiedInsuranceRecord: InstitutionWhichCertifiedInsuranceRecord,
    @JsonProperty("WorkingPeriodsConsidered")
    val workingPeriodsConsidered: WorkingPeriodsConsidered,
    @JsonProperty("ReimbursementPeriod")
    val reimbursementPeriod: ReimbursementPeriod,
    val lastPaymentDate: String,
    @JsonProperty("RequestedAmountForReimbursement")
    val requestedAmountForReimbursement: RequestedAmountForReimbursement
)

data class IndividualClaimStatus(
    val status: Status
)

data class Status(
    val value: List<String>
)

data class Person(
    @JsonProperty("PersonIdentification")
    val personIdentification: PersonIdentification
)

data class PersonIdentification(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: Status
)

data class InstitutionWhichCertifiedInsuranceRecord(
    @JsonProperty("TheInstitutionEXISTSInIR")
    val theInstitutionExistsInIr: TheInstitutionExistsInIr
)

data class TheInstitutionExistsInIr(
    val institutionID: String,
    val institutionName: String
)

data class WorkingPeriodsConsidered(
    @JsonProperty("WorkingPeriodConsidered")
    val workingPeriodConsidered: List<WorkingPeriodConsidered>
)

data class WorkingPeriodConsidered(
    val startDate: String,
    val endDate: String
)

data class ReimbursementPeriod(
    val startDate: String,
    val endDate: String
)

data class RequestedAmountForReimbursement(
    val amount: String,
    val currency: Status
)
