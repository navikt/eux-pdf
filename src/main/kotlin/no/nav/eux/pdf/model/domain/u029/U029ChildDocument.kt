package no.nav.eux.pdf.model.domain.u029

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.eux.pdf.model.domain.Country
import no.nav.eux.pdf.model.domain.Institution

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
    val status: Status,
    @JsonProperty("PleaseFillInFollowingIfStatus6UnderDispute")
    val pleaseFillInFollowingIfStatus6UnderDispute: PleaseFillInFollowingIfStatus6UnderDispute? = null
)

data class PleaseFillInFollowingIfStatus6UnderDispute(
    val reasoning: String
)

data class Status(
    val value: List<String>
)

data class Person(
    @JsonProperty("PersonIdentification")
    val personIdentification: PersonIdentification,
    @JsonProperty("AdditionalInformationPerson")
    val additionalInformationPerson: AdditionalInformationPerson? = null
)

data class PersonIdentification(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: Status,
    val familyNameAtBirth: String? = null,
    val forenameAtBirth: String? = null,
    @JsonProperty("PINPersonInEachInstitution")
    val pinPersonInEachInstitution: PinPersonInEachInstitution? = null,
    @JsonProperty("IfPINNotProvidedForEveryInstitutionPleaseProvide")
    val ifPinNotProvidedForEveryInstitutionPleaseProvide: IfPinNotProvidedForEveryInstitutionPleaseProvide? = null
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

data class PinPersonInEachInstitution(
    @JsonProperty("PersonalIdentificationNumber")
    val personalIdentificationNumber: List<PersonalIdentificationNumber>? = null
)

data class PersonalIdentificationNumber(
    val country: Country,
    val personalIdentificationNumber: String,
    val sector: Sector?,
    @JsonProperty("Institution")
    val institution: Institution?
)

data class Sector(
    val value: List<String>
)

data class IfPinNotProvidedForEveryInstitutionPleaseProvide(
    @JsonProperty("PlaceBirth")
    val placeBirth: PlaceBirth? = null
)

data class PlaceBirth(
    val town: String? = null,
    val region: String? = null,
    val country: Country? = null
)

data class AdditionalInformationPerson(
    val nationality: Country? = null
)
