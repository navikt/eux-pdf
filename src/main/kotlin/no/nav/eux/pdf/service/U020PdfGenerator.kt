package no.nav.eux.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.*

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
    val localCaseNumbers: List<LocalCaseInfo>? = null
)

data class LocalCaseInfo(
    val country: String,
    val caseNumber: String,
    val institutionID: String,
    val institutionName: String
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

data class PersonIdInfo(
    val country: String,
    val personalIdentificationNumber: String,
    val sector: String,
    val institutionID: String,
    val institutionName: String
)

data class PlaceBirthInfo(
    val town: String? = null,
    val region: String? = null,
    val country: String? = null
)

class EessiU020PdfGen {

    val pageWidth = PDRectangle.A4.width
    val pageHeight = PDRectangle.A4.height
    val marginLeft = 50f
    val marginRight = 50f
    val marginTop = 40f
    val marginBottom = 50f
    val lineHeight = 16f
    val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    val italicFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE)

    fun generateU020Document(master: U020Master, claims: List<U020Child>): ByteArray =
        try {
            val document = PDDocument()
            val writer = PdfWriter(document)

            writer.writeRinasakIdTopRight(master.rinasakId)
            writer.writeDocumentTitle()
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

    private inner class PdfWriter(val document: PDDocument) {
        var currentPage: PDPage = createNewPage()
        var contentStream: PDPageContentStream = PDPageContentStream(document, currentPage)
        var currentY: Float = pageHeight - marginTop
        var pageNumber: Int = 1

        private fun createNewPage(): PDPage {
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            return page
        }

        private fun writeFooter() {
            val footerY = marginBottom - 15f
            val footerLineY = marginBottom - 5f

            contentStream.beginText()
            contentStream.setFont(regularFont, 9f)
            contentStream.newLineAtOffset(marginLeft, footerY)
            contentStream.showText("U020")
            contentStream.endText()

            val pageText = "Side $pageNumber"
            val pageTextWidth = regularFont.getStringWidth(pageText) / 1000 * 9f
            contentStream.beginText()
            contentStream.setFont(regularFont, 9f)
            contentStream.newLineAtOffset(pageWidth - marginRight - pageTextWidth, footerY)
            contentStream.showText(pageText)
            contentStream.endText()

            contentStream.setLineWidth(0.5f)
            contentStream.moveTo(marginLeft, footerLineY)
            contentStream.lineTo(pageWidth - marginRight, footerLineY)
            contentStream.stroke()
        }

        private fun checkPageSpace(requiredSpace: Float = lineHeight) {
            if (currentY - requiredSpace < marginBottom + 30f) {
                writeFooter()
                contentStream.close()
                currentPage = createNewPage()
                contentStream = PDPageContentStream(document, currentPage)
                currentY = pageHeight - marginTop
                pageNumber++
            }
        }

        fun writeDocumentTitle() {
            checkPageSpace(40f)

            contentStream.beginText()
            contentStream.setFont(boldFont, 18f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText("U020 - Forespørsel om refusjon")
            contentStream.endText()

            currentY -= 15f

            contentStream.setLineWidth(2f)
            contentStream.setStrokingColor(0.7f, 0.7f, 0.7f) // Make line lighter grey
            contentStream.moveTo(marginLeft, currentY)
            contentStream.lineTo(pageWidth - marginRight, currentY)
            contentStream.stroke()

            currentY -= 10f
        }

        fun writeGeneratedDate() {
            checkPageSpace()
            val dateString = "Generert: ${now().format(ofPattern("dd.MM.yyyy 'kl.' HH:mm"))}"
            contentStream.beginText()
            contentStream.setFont(italicFont, 8f)
            contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText(dateString)
            contentStream.endText()
            contentStream.setNonStrokingColor(0f, 0f, 0f)
            currentY -= lineHeight
        }

        fun writeSectionHeader(title: String) {
            checkPageSpace(30f)
            addBlankLine()

            contentStream.beginText()
            contentStream.setFont(boldFont, 12f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText(title)
            contentStream.endText()
            currentY -= 25f
        }

        fun writeSubsectionHeader(title: String) {
            checkPageSpace(20f)
            contentStream.beginText()
            contentStream.setFont(boldFont, 10f)
            contentStream.setNonStrokingColor(0.2f, 0.2f, 0.2f)
            contentStream.newLineAtOffset(marginLeft + 10f, currentY)
            contentStream.showText(title)
            contentStream.endText()
            contentStream.setNonStrokingColor(0f, 0f, 0f)
            currentY -= 18f
        }

        fun writeKeyValuePair(key: String, value: String, indent: Float = 20f) {
            checkPageSpace()
            val keyText = "$key:"
            val keyWidth = boldFont.getStringWidth(keyText) / 1000 * 9f

            contentStream.beginText()
            contentStream.setFont(boldFont, 9f)
            contentStream.newLineAtOffset(marginLeft + indent, currentY)
            contentStream.showText(keyText)
            contentStream.endText()

            contentStream.beginText()
            contentStream.setFont(regularFont, 9f)
            contentStream.newLineAtOffset(marginLeft + indent + keyWidth + 5f, currentY)
            contentStream.showText(value)
            contentStream.endText()

            currentY -= lineHeight
        }

        fun writeCompactKeyValuePair(key: String, value: String, startX: Float) {
            val keyText = "$key:"
            val keyWidth = boldFont.getStringWidth(keyText) / 1000 * 8f

            contentStream.beginText()
            contentStream.setFont(boldFont, 8f)
            contentStream.newLineAtOffset(startX, currentY)
            contentStream.showText(keyText)
            contentStream.endText()

            contentStream.beginText()
            contentStream.setFont(regularFont, 8f)
            contentStream.newLineAtOffset(startX + keyWidth + 3f, currentY)
            contentStream.showText(value)
            contentStream.endText()
        }

        fun addBlankLine() {
            currentY -= lineHeight * 0.8f
        }

        fun addSmallSpace() {
            currentY -= lineHeight * 0.3f
        }

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
                        writeKeyValuePair("Institusjon", case.institutionName, 30f)
                        writeKeyValuePair("Institusjon-ID", case.institutionID, 30f)
                        addSmallSpace()
                    }
                }
            }
        }

        fun writeIndividualClaims(claims: List<U020Child>) {
            if (claims.isEmpty()) return

            writeFooter()
            contentStream.close()
            currentPage = createNewPage()
            contentStream = PDPageContentStream(document, currentPage)
            currentY = pageHeight - marginTop
            pageNumber++

            writeSectionHeader("Enkeltkrav")

            claims.forEachIndexed { index, claim ->
                var additionalLines = 6
                if (claim.personalIdentificationNumbers?.isNotEmpty() == true)
                    additionalLines += claim.personalIdentificationNumbers.size + 1

                val requiredSpace = 120f + (additionalLines * lineHeight)

                if (currentY - requiredSpace < marginBottom + 30f) {
                    writeFooter()
                    contentStream.close()
                    currentPage = createNewPage()
                    contentStream = PDPageContentStream(document, currentPage)
                    currentY = pageHeight - marginTop
                    pageNumber++
                }

                if (index > 0) {
                    addBlankLine()
                }

                writeSubsectionHeader("Krav ${index + 1}")

                claim.personalIdentificationNumbers?.let { pins ->
                    if (pins.isNotEmpty()) {
                        contentStream.beginText()
                        contentStream.setFont(italicFont, 8f)
                        contentStream.setNonStrokingColor(0.4f, 0.4f, 0.4f)
                        contentStream.newLineAtOffset(marginLeft + 30f, currentY)
                        contentStream.showText("ID-nummer:")
                        contentStream.endText()
                        currentY -= 12f

                        pins.forEach { pin ->
                            contentStream.beginText()
                            contentStream.setFont(regularFont, 8f)
                            contentStream.setNonStrokingColor(0.3f, 0.3f, 0.3f)
                            contentStream.newLineAtOffset(marginLeft + 45f, currentY)
                            contentStream.showText("${pin.country}: ${pin.personalIdentificationNumber} (${pin.sector})")
                            contentStream.endText()
                            currentY -= 11f
                        }
                        contentStream.setNonStrokingColor(0f, 0f, 0f)
                        addSmallSpace()
                    }
                }

                val columnX = marginLeft + 30f

                // Single column layout
                writeCompactKeyValuePair("Navn", "${claim.forename} ${claim.familyName}", columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Etternavn ved fødsel", claim.familyNameAtBirth ?: "-", columnX)
                currentY -= 12f
                writeCompactKeyValuePair("Fornavn ved fødsel", claim.forenameAtBirth ?: "-", columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Fødselsdato", formatDate(claim.dateBirth), columnX)
                currentY -= 12f
                writeCompactKeyValuePair("Kjønn", getSexDescription(claim.sex), columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Nasjonalitet", claim.nationality ?: "-", columnX)
                currentY -= 12f

                val placeText = claim.placeBirth?.let { place ->
                    listOfNotNull(place.town, place.region, place.country)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .takeIf { it.isNotBlank() }
                } ?: "-"
                writeCompactKeyValuePair("Fødselssted", placeText, columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Sekvensnr", claim.sequentialNumber, columnX)
                currentY -= 12f
                writeCompactKeyValuePair("Institusjon", "${claim.institutionName} (${claim.institutionID})", columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Arbeidsperiode",
                    "${formatDate(claim.workingPeriodStart)} - ${formatDate(claim.workingPeriodEnd)}", columnX)
                currentY -= 12f
                writeCompactKeyValuePair("Siste utbetaling", formatDate(claim.lastPaymentDate), columnX)
                currentY -= 12f

                writeCompactKeyValuePair("Refusjonsperiode",
                    "${formatDate(claim.reimbursementPeriodStart)} - ${formatDate(claim.reimbursementPeriodEnd)}", columnX)
                currentY -= 12f
                writeCompactKeyValuePair("Beløp", "${claim.requestedAmount} ${claim.requestedCurrency}", columnX)
                currentY -= 15f

                if (index < claims.size - 1) {
                    // Draw line closer to next heading
                    contentStream.setLineWidth(0.5f)
                    contentStream.setStrokingColor(0.9f, 0.9f, 0.9f)
                    contentStream.moveTo(marginLeft + 20f, currentY)
                    contentStream.lineTo(pageWidth - marginRight - 20f, currentY)
                    contentStream.stroke()

                    currentY -= 8f  // Small space after line before next heading
                }
            }

            writeFooter()
            contentStream.close()
        }

        fun writeRinasakIdTopRight(rinasakId: String) {
            val text = "Saksnr: $rinasakId"
            val textWidth = boldFont.getStringWidth(text) / 1000 * 8f

            contentStream.beginText()
            contentStream.setFont(boldFont, 8f)
            contentStream.newLineAtOffset(pageWidth - marginRight - textWidth, pageHeight - marginTop - 10f)
            contentStream.showText(text)
            contentStream.endText()
        }
    }

    fun formatDate(dateString: String): String =
        try {
            val date = LocalDate.parse(dateString)
            date.format(ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            dateString
        }

    fun getSexDescription(sexCode: String): String =
        when (sexCode) {
            "01" -> "Mann"
            "02" -> "Kvinne"
            else -> "Ukjent ($sexCode)"
        }
}
