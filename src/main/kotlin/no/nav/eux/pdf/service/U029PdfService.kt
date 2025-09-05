package no.nav.eux.pdf.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
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

    val log = logger {}

    fun u029Pdf(
        caseId: Int,
        documentId: String
    ): ByteArray {
        val masterDocument = rinaClient.u029MasterDocument(caseId, documentId)
        val masterDocumentContent = masterDocument.u029Master
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "U029 master ikke funnet")
        val subdocumentsCollection = rinaClient.u029SubdocumentsCollection(caseId, documentId)

        println("=== U029 Subdocuments Collection Debug ===")
        println("subdocumentsCollection.items.size: ${subdocumentsCollection.items.size}")
        subdocumentsCollection.items.forEachIndexed { index, item ->
            println("Item $index: subdocuments.size = ${item.subdocuments.size}")
        }

        val childDocuments = subdocumentsCollection.items.flatMap { item ->
            item.subdocuments.map { subdocument ->
                rinaClient.u029ChildDocument(caseId, documentId, subdocument.id)
            }
        }
        println("Total childDocuments.size: ${childDocuments.size}")
        println("=== End Debug ===")

        val master = mapToU029Master(caseId.toString(), masterDocumentContent)
        val claims = childDocuments.map { mapToU029Child(it) }
        val pdfGen = EessiU029PdfGen()
        return pdfGen.generateU029Document(master, claims)
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
        val institution = claim.institutionWhichCertifiedInsuranceRecord.theInstitutionExistsInIr
        val workingPeriods = claim.workingPeriodsConsidered.workingPeriodConsidered
        val reimbursementPeriod = claim.reimbursementPeriod
        val requestedAmount = claim.requestedAmountForReimbursement
        val workingPeriod = workingPeriods.firstOrNull()

        return U029Child(
            familyName = person.familyName,
            forename = person.forename,
            dateBirth = person.dateBirth,
            sex = person.sex.value.firstOrNull() ?: "",
            reimbursementRequestID = claim.reimbursementRequestID,
            reimbursementContestationID = claim.reimbursementContestationID,
            amendedReimbursementRequestID = claim.amendedReimbursementRequestID,
            sequentialNumber = claim.sequentialNumberIndividualClaim,
            contestedIndividualClaimID = claim.contestedIndividualClaimID,
            amendedContestedIndividualClaimID = claim.amendedContestedIndividualClaimID,
            status = claim.individualClaimStatus.status.value.firstOrNull() ?: "",
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
