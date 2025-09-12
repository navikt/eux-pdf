package no.nav.eux.pdf.service

import no.nav.eux.pdf.client.RinaClient
import no.nav.eux.pdf.model.domain.u029.U029ChildDocument
import no.nav.eux.pdf.model.domain.u029.U029MasterContent
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class U029PdfService(
    val rinaClient: RinaClient
) {

    fun u029Pdf(
        caseId: Int,
        documentId: String
    ): ByteArray {
        val masterDocument = rinaClient.u029MasterDocument(caseId, documentId)
        val masterDocumentContent = masterDocument.u029Master
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "U029 master ikke funnet")
        val subdocumentsCollection = rinaClient.u029SubdocumentsCollection(caseId, documentId)

        val childDocuments = subdocumentsCollection.items.flatMap { item ->
            item.subdocuments.map { subdocument ->
                rinaClient.u029ChildDocument(caseId, documentId, subdocument.id)
            }
        }

        val creationDate = rinaClient
            .rinasak(caseId)
            .documents
            ?.find { it.id == documentId }
            ?.creationDate
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dokument ikke funnet i rinasak")

        val master = mapToU029Master(caseId.toString(), masterDocumentContent)
        val claims = childDocuments.map { mapToU029Child(it) }
        val pdfGen = EessiU029PdfGen()
        return pdfGen.generateU029Document(master, claims, creationDate)
    }

    fun mapToU029Master(rinasakId: String, master: U029MasterContent): U029Master {
        val generalInfo = master.generalInformation
        val updatedTotalAmount = generalInfo.updatedTotalAmountRequested

        val localCaseNumbers = master.localCaseNumbers?.localCaseNumber?.map { localCase ->
            U029LocalCaseInfo(
                country = localCase.country.value.firstOrNull() ?: "",
                caseNumber = localCase.caseNumber,
                institutionID = localCase.institution?.institutionID,
                institutionName = localCase.institution?.institutionName
            )
        }

        return U029Master(
            rinasakId = rinasakId,
            sedGVer = master.sedGVer,
            sedPackage = master.sedPackage,
            sedVer = master.sedVer,
            reimbursementRequestID = generalInfo.reimbursementRequestID,
            reimbursementContestationID = generalInfo.reimbursementContestationID,
            amendedReimbursementRequestID = generalInfo.amendedReimbursementRequestID,
            updatedTotalAmount = updatedTotalAmount.amount,
            currency = updatedTotalAmount.currency.value.firstOrNull() ?: "",
            localCaseNumbers = localCaseNumbers
        )
    }

    fun mapToU029Child(childDoc: U029ChildDocument): U029Child {
        val child = childDoc.u029Child
        val claim = child.individualClaim
        val person = claim.person.personIdentification
        val fullPerson = claim.person
        val institution = claim.institutionWhichCertifiedInsuranceRecord.theInstitutionExistsInIr
        val workingPeriods = claim.workingPeriodsConsidered.workingPeriodConsidered
        val reimbursementPeriod = claim.reimbursementPeriod
        val requestedAmount = claim.requestedAmountForReimbursement
        val workingPeriod = workingPeriods.firstOrNull()
        val status = claim.individualClaimStatus.status.value.firstOrNull() ?: ""
        val reasoning = if (status == "06")
            claim.individualClaimStatus.pleaseFillInFollowingIfStatus6UnderDispute?.reasoning
        else
            null

        val personalIdNumbers = person.pinPersonInEachInstitution?.personalIdentificationNumber?.map { pin ->
            PersonIdInfo(
                country = pin.country.value.firstOrNull() ?: "",
                personalIdentificationNumber = pin.personalIdentificationNumber,
                sector = pin.sector?.value?.firstOrNull() ?: "",
                institutionID = pin.institution?.institutionID ?: "",
                institutionName = pin.institution?.institutionName ?: ""
            )
        }

        val placeBirth = person.ifPinNotProvidedForEveryInstitutionPleaseProvide?.placeBirth?.let { place ->
            PlaceBirthInfo(
                town = place.town,
                region = place.region,
                country = place.country?.value?.firstOrNull()
            )
        }

        val nationality = fullPerson.additionalInformationPerson?.nationality?.value?.firstOrNull()

        return U029Child(
            familyName = person.familyName,
            forename = person.forename,
            dateBirth = person.dateBirth,
            sex = person.sex.value.firstOrNull() ?: "",
            familyNameAtBirth = person.familyNameAtBirth,
            forenameAtBirth = person.forenameAtBirth,
            personalIdentificationNumbers = personalIdNumbers,
            placeBirth = placeBirth,
            nationality = nationality,
            reimbursementRequestID = claim.reimbursementRequestID,
            reimbursementContestationID = claim.reimbursementContestationID,
            amendedReimbursementRequestID = claim.amendedReimbursementRequestID,
            sequentialNumber = claim.sequentialNumberIndividualClaim,
            contestedIndividualClaimID = claim.contestedIndividualClaimID,
            amendedContestedIndividualClaimID = claim.amendedContestedIndividualClaimID,
            status = status,
            reasoning = reasoning,
            institutionID = institution.institutionID,
            institutionName = institution.institutionName,
            workingPeriodStart = workingPeriod?.startDate ?: "",
            workingPeriodEnd = workingPeriod?.endDate ?: "",
            reimbursementPeriodStart = reimbursementPeriod.startDate,
            reimbursementPeriodEnd = reimbursementPeriod.endDate,
            lastPaymentDate = claim.lastPaymentDate,
            requestedAmount = requestedAmount.amount,
            requestedCurrency = requestedAmount.currency.value.firstOrNull() ?: ""
        )
    }
}
