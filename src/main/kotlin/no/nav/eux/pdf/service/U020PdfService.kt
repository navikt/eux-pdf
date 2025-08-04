package no.nav.eux.pdf.service

import no.nav.eux.pdf.client.RinaClient
import no.nav.eux.pdf.model.domain.U020ChildDocument
import no.nav.eux.pdf.model.domain.U020MasterContent
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class U020PdfService(
    val rinaClient: RinaClient
) {

    fun u020Pdf(
        caseId: Int,
        documentId: String
    ): ByteArray {
        val masterDocument = rinaClient.getDocument(caseId, documentId)
        rinaClient
            .getDocumentStringTest(caseId, documentId)
            .also { println(it) }
        val masterDocumentContent = masterDocument.u020Master
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "U020 master ikke funnet")
        val subdocumentsCollection = rinaClient.getSubdocuments(caseId, documentId)
        val childDocuments = subdocumentsCollection.items.flatMap { item ->
            item.subdocuments.map { subdocument ->
                rinaClient
                    .getSubdocumentStringTest(caseId, documentId, subdocument.id)
                    .also { println(it) }
                rinaClient.getSubdocument(caseId, documentId, subdocument.id)
            }
        }
        val master = mapToU020Master(caseId.toString(), masterDocumentContent)
        val claims = childDocuments.map { mapToU020Child(it) }
        val pdfGen = EessiU020PdfGen()
        return pdfGen.generateU020Document(master, claims)
    }

     fun mapToU020Master(rinasakId: String, master: U020MasterContent): U020Master {
        val generalInfo = master.generalInformation
        val totalAmount = generalInfo.totalAmountRequested
        val bankInfo = generalInfo.bankInformation
        return U020Master(
            rinasakId = rinasakId,
            sedGVer = master.sedGVer,
            sedPackage = master.sedPackage,
            sedVer = master.sedVer,
            reimbursementRequestID = generalInfo.reimbursementRequestID,
            numberIndividualClaims = generalInfo.numberIndividualClaims,
            totalAmount = totalAmount.amount,
            currency = totalAmount.currency.value.firstOrNull() ?: "",
            iban = bankInfo.sepaBankDetails.iban,
            bankReference = bankInfo.bankTransferSubjectOrTransactionReference
        )
    }

     fun mapToU020Child(childDoc: U020ChildDocument): U020Child {
        val child = childDoc.u020Child
        val claim = child.individualClaim
        val person = claim.person.personIdentification
        val institution = claim.institutionWhichCertifiedInsuranceRecord.theInstitutionExistsInIr
        val workingPeriods = claim.workingPeriodsConsidered.workingPeriodConsidered
        val reimbursementPeriod = claim.reimbursementPeriod
        val requestedAmount = claim.requestedAmountForReimbursement
        val workingPeriod = workingPeriods.firstOrNull()
        return U020Child(
            familyName = person.familyName,
            forename = person.forename,
            dateBirth = person.dateBirth,
            sex = person.sex.value.firstOrNull() ?: "",
            reimbursementRequestID = claim.reimbursementRequestID,
            sequentialNumber = claim.sequentialNumberIndividualClaim,
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
