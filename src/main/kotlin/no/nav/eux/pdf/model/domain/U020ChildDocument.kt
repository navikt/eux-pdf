package no.nav.eux.pdf.model.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class U020ChildDocument(
    @JsonProperty("U020_Child")
    val u020Child: U020ChildContent
)

data class U020ChildContent(
    @JsonProperty("IndividualClaim")
    val individualClaim: IndividualClaim
)

data class IndividualClaim(
    @JsonProperty("Person")
    val person: Person,
    val reimbursementRequestID: String,
    val sequentialNumberIndividualClaim: String,
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

data class Person(
    @JsonProperty("PersonIdentification")
    val personIdentification: PersonIdentification
)

data class PersonIdentification(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: Sex
)

data class Sex(
    val value: List<String>
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
    val currency: Currency
)
