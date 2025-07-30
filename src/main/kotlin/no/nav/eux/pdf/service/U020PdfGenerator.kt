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
import java.time.format.DateTimeFormatter

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
    val bankReference: String
)

data class U020Child(
    val familyName: String,
    val forename: String,
    val dateBirth: String,
    val sex: String,
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
    
    private val pageWidth = PDRectangle.A4.width
    private val pageHeight = PDRectangle.A4.height
    private val marginLeft = 50f
    private val marginRight = 50f
    private val marginTop = 50f
    private val marginBottom = 50f
    private val lineHeight = 20f
    
    private val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
    private val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    
    fun generateU020Document(master: U020Master, claims: List<U020Child>): ByteArray {
        return try {
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
    }
    
    private inner class PdfWriter(private val document: PDDocument) {
        private var currentPage: PDPage = createNewPage()
        private var contentStream: PDPageContentStream = PDPageContentStream(document, currentPage)
        private var currentY: Float = pageHeight - marginTop
        
        private fun createNewPage(): PDPage {
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            return page
        }
        
        private fun checkPageSpace(requiredSpace: Float = lineHeight) {
            if (currentY - requiredSpace < marginBottom) {
                contentStream.close()
                currentPage = createNewPage()
                contentStream = PDPageContentStream(document, currentPage)
                currentY = pageHeight - marginTop
            }
        }

        fun writeDocumentTitle() {
            checkPageSpace(30f)
            contentStream.beginText()
            contentStream.setFont(boldFont, 16f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText("U020 - Forespørsel om refusjon")
            contentStream.endText()
            currentY -= 30f
        }

        fun writeGeneratedDate() {
            checkPageSpace()
            val dateString = "PDF Generert: ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)}"
            contentStream.beginText()
            contentStream.setFont(regularFont, 8f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText(dateString)
            contentStream.endText()
            currentY -= lineHeight
        }

        fun writeSectionHeader(title: String) {
            checkPageSpace(25f)
            contentStream.beginText()
            contentStream.setFont(boldFont, 14f)
            contentStream.newLineAtOffset(marginLeft, currentY)
            contentStream.showText(title)
            contentStream.endText()
            currentY -= 25f
        }

        fun writeSubsectionHeader(title: String) {
            checkPageSpace()
            contentStream.beginText()
            contentStream.setFont(boldFont, 12f)
            contentStream.newLineAtOffset(marginLeft + 20f, currentY)
            contentStream.showText(title)
            contentStream.endText()
            currentY -= lineHeight
        }

        fun writeKeyValuePair(key: String, value: String) {
            checkPageSpace()
            val keyText = "$key "
            val keyWidth = boldFont.getStringWidth(keyText) / 1000 * 10f

            contentStream.beginText()
            contentStream.setFont(boldFont, 10f)
            contentStream.newLineAtOffset(marginLeft + 40f, currentY)
            contentStream.showText(keyText)
            contentStream.endText()

            contentStream.beginText()
            contentStream.setFont(regularFont, 10f)
            contentStream.newLineAtOffset(marginLeft + 40f + keyWidth, currentY)
            contentStream.showText(value)
            contentStream.endText()

            currentY -= lineHeight
        }

        fun addBlankLine() {
            currentY -= lineHeight / 2
        }

        fun writeMasterInformation(master: U020Master) {
            writeSectionHeader("Generell Informasjon")

            writeKeyValuePair("SED Versjon:", "${master.sedGVer}.${master.sedVer}")
            writeKeyValuePair("ID-nummer for krav om refusjon:", master.reimbursementRequestID)
            writeKeyValuePair("Antall enkeltkrav:", master.numberIndividualClaims)

            addBlankLine()
            writeSubsectionHeader("Bankinformasjon")

            writeKeyValuePair("Anmodet Totalbeløp:", "${master.totalAmount} ${master.currency}")
            writeKeyValuePair("IBAN:", master.iban)
            writeKeyValuePair("Bank Reference:", master.bankReference)
        }

        fun writeIndividualClaims(claims: List<U020Child>) {
            writeSectionHeader("Enkeltkrav")

            claims.forEachIndexed { index, claim ->
                val requiredSpace = lineHeight + (9 * lineHeight) + if (index < claims.size - 1) lineHeight / 2 else 0f

                if (currentY - requiredSpace < marginBottom) {
                    contentStream.close()
                    currentPage = createNewPage()
                    contentStream = PDPageContentStream(document, currentPage)
                    currentY = pageHeight - marginTop
                }

                writeSubsectionHeader("Krav #${index + 1}")

                writeKeyValuePair("Navn:", "${claim.forename} ${claim.familyName}")
                writeKeyValuePair("Fødselsdato:", formatDate(claim.dateBirth))
                writeKeyValuePair("Kjønn:", getSexDescription(claim.sex))
                writeKeyValuePair("Sekvensnummer:", claim.sequentialNumber)
                writeKeyValuePair("Institusjon:", "${claim.institutionName} (${claim.institutionID})")
                writeKeyValuePair("Arbeidsperiode:",
                    "${formatDate(claim.workingPeriodStart)} - ${formatDate(claim.workingPeriodEnd)}")
                writeKeyValuePair("Refusjonsperiode:",
                    "${formatDate(claim.reimbursementPeriodStart)} - ${formatDate(claim.reimbursementPeriodEnd)}")
                writeKeyValuePair("Siste utbetalingsdato:", formatDate(claim.lastPaymentDate))
                writeKeyValuePair("Anmodet refusjonsbeløp:", "${claim.requestedAmount} ${claim.requestedCurrency}")

                if (index < claims.size - 1)
                    addBlankLine()
            }

            contentStream.close()
        }

        fun writeRinasakIdTopRight(rinasakId: String) {
            val text = "Saksnummer: $rinasakId"
            val textWidth = regularFont.getStringWidth(text) / 1000 * 8f

            contentStream.beginText()
            contentStream.setFont(regularFont, 9f)
            contentStream.newLineAtOffset(pageWidth - marginRight - textWidth, pageHeight - marginTop)
            contentStream.showText(text)
            contentStream.endText()
        }
    }

    private fun formatDate(dateString: String): String =
        try {
            val date = LocalDate.parse(dateString)
            date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            dateString
        }

    private fun getSexDescription(sexCode: String): String =
        when (sexCode) {
            "01" -> "Mann"
            "02" -> "Kvinne"
            else -> "Ukjent ($sexCode)"
        }
}