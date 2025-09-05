package no.nav.eux.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream
import java.io.IOException

data class U020Master(
    val rinasakId: String,
    val sedGVer: String,
    val sedPackage: String,
    val sedVer: String,
    val reimbursementRequestID: String,
    val numberIndividualClaims: String,
    val totalAmount: String,
    val currency: String,
    val iban: String,
    val bicSwift: String? = null,
    val bankReference: String,
    val localCaseNumbers: List<U020LocalCaseInfo>? = null
)

data class U020LocalCaseInfo(
    val country: String,
    val caseNumber: String,
    val institutionID: String?,
    val institutionName: String?
)

data class U020Child(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: String,
    val familyNameAtBirth: String? = null,
    val forenameAtBirth: String? = null,
    val personalIdentificationNumbers: List<PersonIdInfo>? = null,
    val placeBirth: PlaceBirthInfo? = null,
    val nationality: String? = null,
    val reimbursementRequestID: String,
    val sequentialNumber: String,
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

class EessiU020PdfGen {

    fun generateU020Document(master: U020Master, claims: List<U020Child>): ByteArray =
        try {
            val document = PDDocument()
            val writer = U020PdfWriter(document)

            writer.writeRinasakIdTopRight(master.rinasakId)
            writer.writeDocumentTitle("U020 - Forespørsel om refusjon")
            writer.writeGeneratedDate()
            writer.addBlankLine()

            writer.writeMasterInformation(master)
            writer.addBlankLine()
            writer.addBlankLine()

            writer.writeIndividualClaims(claims)

            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)
            document.close()

            outputStream.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("Failed to generate EESSI U020 PDF", e)
        }

    private class U020PdfWriter(document: PDDocument) : BasePdfWriter(document, "U020") {

        fun writeMasterInformation(master: U020Master) {
            writeSectionHeader("Dokumentinformasjon")

            writeKeyValuePair("SED-versjon", "${master.sedGVer}.${master.sedVer}")
            writeKeyValuePair("Forespørsel-ID", master.reimbursementRequestID)
            writeKeyValuePair("Antall krav", master.numberIndividualClaims)

            writeSectionHeader("Bankinformasjon")

            writeKeyValuePair("Totalbeløp", "${master.totalAmount} ${master.currency}")
            writeKeyValuePair("IBAN", master.iban)
            master.bicSwift?.let {
                writeKeyValuePair("BIC/SWIFT", it)
            }
            writeKeyValuePair("Referanse", master.bankReference)

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

        fun writeIndividualClaims(claims: List<U020Child>) {
            if (claims.isEmpty()) return

            startNewPageForClaims()
            writeSectionHeader("Enkeltkrav")

            claims.forEachIndexed { index, claim ->
                writeSingleClaim(claim, index, claims.size)
            }

            close()
        }

        private fun writeSingleClaim(claim: U020Child, index: Int, totalClaims: Int) {
            val requiredSpace = calculateRequiredSpace(claim)
            ensureSufficientSpace(requiredSpace)

            if (index > 0) addBlankLine()

            writeSubsectionHeader("Krav ${index + 1}")
            writeClaimDetails(claim)

            if (shouldDrawSeparator(index, totalClaims)) {
                drawClaimSeparator()
            }
        }

        private fun calculateRequiredSpace(claim: U020Child): Float {
            val baseLines = 6
            val additionalLines = claim.personalIdentificationNumbers
                ?.takeIf { it.isNotEmpty() }
                ?.let { it.size + 1 }
                ?: 0
            return 120f + ((baseLines + additionalLines) * 18f)
        }

        private fun writeClaimDetails(claim: U020Child) {
            writePersonalIdentificationNumbers(claim.personalIdentificationNumbers)
            writePersonalInformation(claim)
            writeInstitutionalInformation(claim)
        }

        private fun writePersonalIdentificationNumbers(pins: List<PersonIdInfo>?) {
            pins?.takeIf { it.isNotEmpty() }?.let { idNumbers ->
                writeIdNumbersHeader()
                idNumbers.forEach { pin ->
                    writeIdNumber(pin)
                }
                restoreDefaultTextColor()
                addSmallSpace()
            }
        }

        private fun writePersonalInformation(claim: U020Child) {
            val columnX = getMarginLeft() + 30f

            writeBasicPersonalInfo(claim, columnX)
            writeBirthInfo(claim, columnX)
            writeOptionalPersonalInfo(claim, columnX)
        }

        private fun writeBasicPersonalInfo(claim: U020Child, columnX: Float) {
            writeCompactKeyValuePair("Navn", "${claim.forename} ${claim.familyName}", columnX)
            currentY -= 16f

            claim.familyNameAtBirth?.let {
                writeCompactKeyValuePair("Etternavn ved fødsel", it, columnX)
                currentY -= 16f
            }
            claim.forenameAtBirth?.let {
                writeCompactKeyValuePair("Fornavn ved fødsel", it, columnX)
                currentY -= 16f
            }
        }

        private fun writeBirthInfo(claim: U020Child, columnX: Float) {
            writeCompactKeyValuePair("Fødselsdato", formatDate(claim.dateBirth), columnX)
            currentY -= 16f
            writeCompactKeyValuePair("Kjønn", getSexDescription(claim.sex), columnX)
            currentY -= 16f

            formatPlaceOfBirth(claim.placeBirth)?.let { placeText ->
                writeCompactKeyValuePair("Fødselssted", placeText, columnX)
                currentY -= 16f
            }
        }

        private fun writeOptionalPersonalInfo(claim: U020Child, columnX: Float) {
            claim.nationality?.let {
                writeCompactKeyValuePair("Nasjonalitet", it, columnX)
                currentY -= 16f
            }
        }

        private fun writeInstitutionalInformation(claim: U020Child) {
            val columnX = getMarginLeft() + 30f

            writeCompactKeyValuePair("Sekvensnr", claim.sequentialNumber, columnX)
            currentY -= 16f

            writeCompactKeyValuePair("Institusjon", "${claim.institutionName} (${claim.institutionID})", columnX)
            currentY -= 16f

            writePeriodInformation(claim, columnX)
            writeAmountInformation(claim, columnX)
        }

        private fun writePeriodInformation(claim: U020Child, columnX: Float) {
            val workingPeriod = "${formatDate(claim.workingPeriodStart)} - ${formatDate(claim.workingPeriodEnd)}"
            writeCompactKeyValuePair("Arbeidsperiode", workingPeriod, columnX)
            currentY -= 16f

            writeCompactKeyValuePair("Siste utbetaling", formatDate(claim.lastPaymentDate), columnX)
            currentY -= 16f

            val reimbursementPeriod =
                "${formatDate(claim.reimbursementPeriodStart)} - ${formatDate(claim.reimbursementPeriodEnd)}"
            writeCompactKeyValuePair("Refusjonsperiode", reimbursementPeriod, columnX)
            currentY -= 16f
        }

        private fun writeAmountInformation(claim: U020Child, columnX: Float) {
            writeCompactKeyValuePair("Beløp", "${claim.requestedAmount} ${claim.requestedCurrency}", columnX)
            currentY -= 17f
        }
    }
}
