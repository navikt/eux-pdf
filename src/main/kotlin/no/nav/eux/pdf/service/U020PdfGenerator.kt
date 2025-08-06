package no.nav.eux.pdf.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern

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
    val lineHeight = 18f

    private val log = logger {}

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
        val boldFont by lazy { loadFont("NotoSans-Bold.ttf") }
        val regularFont by lazy { loadFont("NotoSans-Regular.ttf") }
        val italicFont by lazy { loadFont("NotoSans-Italic.ttf") }

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

            contentStream.setLineWidth(1f)
            contentStream.setStrokingColor(0.7f, 0.7f, 0.7f)
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
            val keyWidth = boldFont.getStringWidth(keyText) / 1000 * 12f

            contentStream.beginText()
            contentStream.setFont(boldFont, 12f)
            contentStream.newLineAtOffset(marginLeft + indent, currentY)
            contentStream.showText(keyText)
            contentStream.endText()

            contentStream.beginText()
            contentStream.setFont(regularFont, 12f)
            contentStream.newLineAtOffset(marginLeft + indent + keyWidth + 5f, currentY)
            contentStream.showText(value)
            contentStream.endText()

            currentY -= lineHeight
        }

        fun writeCompactKeyValuePair(key: String, value: String, startX: Float) {
            val keyText = "$key:"
            val keyWidth = boldFont.getStringWidth(keyText) / 1000 * 12f

            contentStream.beginText()
            contentStream.setFont(boldFont, 12f)
            contentStream.newLineAtOffset(startX, currentY)
            contentStream.showText(keyText)
            contentStream.endText()

            contentStream.beginText()
            contentStream.setFont(regularFont, 12f)
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

            writeFooter()
            contentStream.close()
        }

        private fun startNewPageForClaims() {
            writeFooter()
            contentStream.close()
            currentPage = createNewPage()
            contentStream = PDPageContentStream(document, currentPage)
            currentY = pageHeight - marginTop
            pageNumber++
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
            return 120f + ((baseLines + additionalLines) * lineHeight)
        }

        private fun ensureSufficientSpace(requiredSpace: Float) {
            if (currentY - requiredSpace < marginBottom + 30f) {
                writeFooter()
                contentStream.close()
                currentPage = createNewPage()
                contentStream = PDPageContentStream(document, currentPage)
                currentY = pageHeight - marginTop
                pageNumber++
            }
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

        private fun writeIdNumbersHeader() {
            contentStream.beginText()
            contentStream.setFont(boldFont, 12f)
            contentStream.setNonStrokingColor(0f, 0f, 0f)
            contentStream.newLineAtOffset(marginLeft + 30f, currentY)
            contentStream.showText("ID-nummer:")
            contentStream.endText()
            currentY -= 16f
        }

        private fun writeIdNumber(pin: PersonIdInfo) {
            val maskedPnr = maskNorwegianPnr(pin.personalIdentificationNumber, pin.country)
            contentStream.beginText()
            contentStream.setFont(regularFont, 12f)
            contentStream.setNonStrokingColor(0.3f, 0.3f, 0.3f)
            contentStream.newLineAtOffset(marginLeft + 45f, currentY)
            contentStream.showText("${pin.country}: $maskedPnr (${pin.sector})")
            contentStream.endText()
            currentY -= 16f
        }

        private fun restoreDefaultTextColor() {
            contentStream.setNonStrokingColor(0f, 0f, 0f)
        }

        private fun writePersonalInformation(claim: U020Child) {
            val columnX = marginLeft + 30f

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

        private fun formatPlaceOfBirth(placeBirth: PlaceBirthInfo?): String? =
            placeBirth?.let { place ->
                listOfNotNull(place.town, place.region, place.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .takeIf { it.isNotBlank() }
            }

        private fun writeInstitutionalInformation(claim: U020Child) {
            val columnX = marginLeft + 30f

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

            val reimbursementPeriod = "${formatDate(claim.reimbursementPeriodStart)} - ${formatDate(claim.reimbursementPeriodEnd)}"
            writeCompactKeyValuePair("Refusjonsperiode", reimbursementPeriod, columnX)
            currentY -= 16f
        }

        private fun writeAmountInformation(claim: U020Child, columnX: Float) {
            writeCompactKeyValuePair("Beløp", "${claim.requestedAmount} ${claim.requestedCurrency}", columnX)
            currentY -= 17f
        }

        private fun shouldDrawSeparator(index: Int, totalClaims: Int): Boolean =
            index < totalClaims - 1

        private fun drawClaimSeparator() {
            contentStream.setLineWidth(0.5f)
            contentStream.setStrokingColor(0.9f, 0.9f, 0.9f)
            contentStream.moveTo(marginLeft + 20f, currentY)
            contentStream.lineTo(pageWidth - marginRight - 20f, currentY)
            contentStream.stroke()
            currentY -= 8f
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

        private fun loadFont(fontFileName: String): PDFont {
            val fontStream = javaClass.getResourceAsStream("/fonts/$fontFileName")
                ?: throw IllegalStateException("Font file $fontFileName not found in resources/fonts/")
            return PDType0Font.load(document, fontStream)
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

    fun maskNorwegianPnr(pnr: String, country: String) =
        when (country.uppercase()) {
            "NO" -> {
                if (pnr.length == 11 && pnr.all { it.isDigit() }) {
                    val firstSix = pnr.substring(0, 6)
                    val masked = "*".repeat(5)
                    "$firstSix$masked"
                } else {
                    maskGeneric(pnr)
                }
            }

            "DK" -> {
                val cleanPnr = pnr.replace("-", "")
                if (cleanPnr.length == 10 && cleanPnr.all { it.isDigit() }) {
                    val birthDate = cleanPnr.substring(0, 6)
                    val masked = "*".repeat(4)
                    "$birthDate-$masked"
                } else {
                    maskGeneric(pnr)
                }
            }

            "SE" -> {
                val cleanPnr = pnr.replace("-", "")
                when {
                    cleanPnr.length == 12 && cleanPnr.all { it.isDigit() } -> {
                        val birthDate = cleanPnr.substring(0, 8)
                        val masked = "*".repeat(4)
                        "$birthDate-$masked"
                    }

                    cleanPnr.length == 10 && cleanPnr.all { it.isDigit() } -> {
                        val birthDate = cleanPnr.substring(0, 6)
                        val masked = "*".repeat(4)
                        "$birthDate-$masked"
                    }

                    else -> maskGeneric(pnr)
                }
            }

            "FI" -> {
                if (pnr.length == 11) {
                    val birthDate = pnr.substring(0, 6)
                    val centuryMarker = pnr[6]
                    val validCenturyMarkers = setOf(
                        '+', '-', 'Y', 'X', 'W', 'V', 'U', 'T', 'S', 'R', 'Q',
                        'P', 'N', 'M', 'L', 'K', 'J', 'H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'
                    )

                    if (birthDate.all { it.isDigit() } && centuryMarker in validCenturyMarkers) {
                        val masked = "*".repeat(4)
                        "$birthDate$centuryMarker$masked"
                    } else {
                        maskGeneric(pnr)
                    }
                } else {
                    maskGeneric(pnr)
                }
            }

            else -> maskGeneric(pnr)
        }

    private fun maskGeneric(pnr: String): String =
        if (pnr.length > 3)
            "*".repeat(pnr.length - 3) + pnr.takeLast(3)
        else
            pnr
}
