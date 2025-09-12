package no.nav.eux.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime

data class U029Master(
    val rinasakId: String,
    val sedGVer: String,
    val sedPackage: String,
    val sedVer: String,
    val reimbursementRequestID: String,
    val reimbursementContestationID: String,
    val amendedReimbursementRequestID: String,
    val updatedTotalAmount: String,
    val currency: String,
    val localCaseNumbers: List<U029LocalCaseInfo>? = null
)

data class U029LocalCaseInfo(
    val country: String,
    val caseNumber: String,
    val institutionID: String?,
    val institutionName: String?
)

data class U029Child(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: String,
    val reimbursementRequestID: String,
    val reimbursementContestationID: String,
    val amendedReimbursementRequestID: String,
    val sequentialNumber: String,
    val contestedIndividualClaimID: String?,
    val amendedContestedIndividualClaimID: String?,
    val status: String,
    val reasoning: String?,
    val institutionID: String,
    val institutionName: String,
    val workingPeriodStart: String,
    val workingPeriodEnd: String,
    val reimbursementPeriodStart: String,
    val reimbursementPeriodEnd: String,
    val lastPaymentDate: String,
    val requestedAmount: String,
    val requestedCurrency: String
)

class EessiU029PdfGen {

    fun generateU029Document(
        master: U029Master,
        claims: List<U029Child>,
        creationDate: LocalDateTime
    ): ByteArray =
        try {
            val document = PDDocument()
            val writer = U029PdfWriter(document)

            writer.writeDocumentTitle("U029 - Endret anmodning om refusjon etter bestridelse")
            writer.writeGeneratedDateWithRinasakId(master.rinasakId)
            writer.addBlankLine()

            writer.writeMasterInformation(master, creationDate)
            writer.addBlankLine()
            writer.addBlankLine()

            writer.writeIndividualClaims(claims)

            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)
            document.close()

            outputStream.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("Failed to generate EESSI U029 PDF", e)
        }

    private class U029PdfWriter(document: PDDocument) : BasePdfWriter(document, "U029") {

        fun writeMasterInformation(master: U029Master, creationDate: LocalDateTime) {
            writeSectionHeader("Dokumentinformasjon")

            writeKeyValuePair("Opprettet dato",
                "${creationDate.year}-${creationDate.monthValue}-${creationDate.dayOfMonth}")
            writeKeyValuePair("SED-versjon", "${master.sedGVer}.${master.sedVer}")
            writeKeyValuePair("ID-nummer for krav om refusjon", master.reimbursementRequestID)
            writeKeyValuePair("ID bestridelse av refusjon", master.reimbursementContestationID)
            writeKeyValuePair("ID for endret anmodning om refusjon", master.amendedReimbursementRequestID)
            writeKeyValuePair("Oppdatert totalbeløp", "${master.updatedTotalAmount} ${master.currency}")

            master.localCaseNumbers?.let { cases ->
                if (cases.isNotEmpty()) {
                    writeSectionHeader("Lokale saksnummer")
                    cases.forEach { case ->
                        writeSubsectionHeader("${case.country} - ${case.caseNumber}")
                        writeKeyValuePair("Institusjon", case.institutionName ?: "", 30f)
                        writeKeyValuePair("Institusjon-ID", case.institutionID ?: "", 30f)
                        addSmallSpace()
                    }
                }
            }
        }

        fun writeIndividualClaims(claims: List<U029Child>) {
            if (claims.isEmpty()) return

            startNewPageForClaims()
            writeSectionHeader("Individuelle krav")

            claims.forEachIndexed { index, claim ->
                println("Rendering claim index: $index, sequentialNumber: ${claim.sequentialNumber}")
                writeSingleClaim(claim, index, claims.size)
            }

            close()
        }

        private fun writeSingleClaim(claim: U029Child, index: Int, totalClaims: Int) {
            val requiredSpace = calculateRequiredSpace()
            ensureSufficientSpace(requiredSpace)

            if (index > 0) addBlankLine()

            writeSubsectionHeader("Krav ${index + 1}")
            writeClaimDetails(claim)

            if (shouldDrawSeparator(index, totalClaims)) {
                drawClaimSeparator()
            }
        }

        private fun calculateRequiredSpace(): Float {
            val baseLines = 10
            return 120f + (baseLines * 18f)
        }

        private fun writeClaimDetails(claim: U029Child) {
            writePersonalInformation(claim)
            writeClaimInformation(claim)
            writeInstitutionalInformation(claim)
        }

        private fun writePersonalInformation(claim: U029Child) {
            val columnX = getMarginLeft() + 30f

            writeCompactKeyValuePair("Navn", "${claim.forename} ${claim.familyName}", columnX)
            currentY -= 16f
            writeCompactKeyValuePair("Fødselsdato", formatDate(claim.dateBirth), columnX)
            currentY -= 16f
            writeCompactKeyValuePair("Kjønn", getSexDescription(claim.sex), columnX)
            currentY -= 16f
        }

        private fun writeClaimInformation(claim: U029Child) {
            val columnX = getMarginLeft() + 30f

            writeCompactKeyValuePair("ID-nummer for krav om refusjon", claim.reimbursementRequestID, columnX)
            currentY -= 16f
            writeCompactKeyValuePair("ID bestridelse av refusjon", claim.reimbursementContestationID, columnX)
            currentY -= 16f
            writeCompactKeyValuePair(
                "ID for endret anmodning om refusjon", claim.amendedReimbursementRequestID, columnX
            )
            currentY -= 16f
            writeCompactKeyValuePair("Løpenummer for enkeltkrav", claim.sequentialNumber, columnX)
            currentY -= 16f
            writeCompactKeyValuePair("Status", getStatusDescription(claim.status), columnX)
            currentY -= 16f

            claim.contestedIndividualClaimID?.let {
            if (claim.status == "06" && claim.reasoning != null) {
                writeCompactKeyValuePair("Omtvistet begrunnelse", claim.reasoning, columnX)
                currentY -= 16f
            }

                writeCompactKeyValuePair("ID bestridelse av enkeltkrav", it, columnX)
                currentY -= 16f
            }
            claim.amendedContestedIndividualClaimID?.let {
                writeCompactKeyValuePair("ID for endret bestridelse av enkeltkrav", it, columnX)
                currentY -= 16f
            }
        }

        private fun writeInstitutionalInformation(claim: U029Child) {
            val columnX = getMarginLeft() + 30f

            writeCompactKeyValuePair("Institusjon", "${claim.institutionName} (${claim.institutionID})", columnX)
            currentY -= 16f

            val workingPeriod = "${formatDate(claim.workingPeriodStart)} - ${formatDate(claim.workingPeriodEnd)}"
            writeCompactKeyValuePair("Arbeidsperiode", workingPeriod, columnX)
            currentY -= 16f

            val reimbursementPeriod =
                "${formatDate(claim.reimbursementPeriodStart)} - ${formatDate(claim.reimbursementPeriodEnd)}"
            writeCompactKeyValuePair("Refusjonsperiode", reimbursementPeriod, columnX)
            currentY -= 16f

            writeCompactKeyValuePair("Siste utbetaling", formatDate(claim.lastPaymentDate), columnX)
            currentY -= 16f

            writeCompactKeyValuePair("Beløp", "${claim.requestedAmount} ${claim.requestedCurrency}", columnX)
            currentY -= 17f
        }

        private fun getStatusDescription(statusCode: String): String =
            when (statusCode) {
                "01" -> "Akseptert"
                "02" -> "Meldt betalt i U024"
                "03" -> "Beløp tilpasset nasjonalt tak"
                "04" -> "Endret enkeltkrav"
                "05" -> "Enkeltkrav tatt ut av anmodning om refusjon"
                "06" -> "Omtvistet"
                else -> "Ukjent ($statusCode)"
            }
    }
}
