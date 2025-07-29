package no.nav.eux.pdf.service

import no.nav.eux.pdf.client.RinaClient
import org.springframework.stereotype.Service

@Service
class U020PdfService(
    private val rinaClient: RinaClient
) {

    fun u020Pdf(
        caseId: Int,
        documentId: String
    ): ByteArray {
        // Fetch master document (U020_Master)
        val masterDocument = rinaClient.getDocument(caseId, documentId)

        // Fetch subdocuments list to get all child documents
        val subdocumentsCollection = rinaClient.getSubdocuments(caseId, documentId)

        // Fetch each individual child document (U020_Child)
        val childDocuments = subdocumentsCollection.items.flatMap { item ->
            item.subdocuments.map { subdoc ->
                rinaClient.getSubdocument(caseId, documentId, subdoc.id)
            }
        }

        // Map domain models to PDF generator data classes
        val master = mapToU020Master(caseId.toString(), masterDocument)
        val claims = childDocuments.map { mapToU020Child(it) }

        // Generate PDF
        val pdfGen = EessiU020PdfGen()
        return pdfGen.generateU020Document(master, claims)
    }

    private fun mapToU020Master(rinasakId: String, masterDoc: no.nav.eux.pdf.model.domain.U020MasterDocument): U020Master {
        val master = masterDoc.u020Master
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

    private fun mapToU020Child(childDoc: no.nav.eux.pdf.model.domain.U020ChildDocument): U020Child {
        val child = childDoc.u020Child
        val claim = child.individualClaim
        val person = claim.person.personIdentification
        val institution = claim.institutionWhichCertifiedInsuranceRecord.theInstitutionExistsInIr
        val workingPeriods = claim.workingPeriodsConsidered.workingPeriodConsidered
        val reimbursementPeriod = claim.reimbursementPeriod
        val requestedAmount = claim.requestedAmountForReimbursement

        // Use first working period if available
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
