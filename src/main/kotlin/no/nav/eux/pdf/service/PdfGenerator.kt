package no.nav.eux.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern

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

open class BasePdfWriter(
    private val document: PDDocument,
    private val documentType: String,
    private val pageWidth: Float = PDRectangle.A4.width,
    private val pageHeight: Float = PDRectangle.A4.height,
    private val marginLeft: Float = 50f,
    private val marginRight: Float = 50f,
    private val marginTop: Float = 40f,
    private val marginBottom: Float = 50f,
    private val lineHeight: Float = 18f
) {
    var currentPage: PDPage = createNewPage()
    var contentStream: PDPageContentStream = PDPageContentStream(document, currentPage)
    var currentY: Float = pageHeight - marginTop
    var pageNumber: Int = 1

    val boldFont by lazy { loadFont("NotoSans-Bold.ttf") }
    val regularFont by lazy { loadFont("NotoSans-Regular.ttf") }
    val italicFont by lazy { loadFont("NotoSans-Italic.ttf") }

    fun createNewPage(): PDPage {
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)
        return page
    }

    fun writeFooter() {
        val footerY = marginBottom - 15f
        val footerLineY = marginBottom - 5f

        contentStream.beginText()
        contentStream.setFont(regularFont, 9f)
        contentStream.newLineAtOffset(marginLeft, footerY)
        contentStream.showText(documentType)
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

    fun checkPageSpace(requiredSpace: Float = lineHeight) {
        if (currentY - requiredSpace < marginBottom + 30f) {
            writeFooter()
            contentStream.close()
            currentPage = createNewPage()
            contentStream = PDPageContentStream(document, currentPage)
            currentY = pageHeight - marginTop
            pageNumber++
        }
    }

    fun writeDocumentTitle(title: String) {
        checkPageSpace(40f)

        contentStream.beginText()
        contentStream.setFont(boldFont, 18f)
        contentStream.newLineAtOffset(marginLeft, currentY)
        contentStream.showText(title)
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

    fun writeRinasakIdTopRight(rinasakId: String) {
        val text = "Saksnr: $rinasakId"
        val textWidth = boldFont.getStringWidth(text) / 1000 * 8f

        contentStream.beginText()
        contentStream.setFont(boldFont, 8f)
        contentStream.newLineAtOffset(pageWidth - marginRight - textWidth, pageHeight - marginTop - 10f)
        contentStream.showText(text)
        contentStream.endText()
    }

    fun startNewPageForClaims() {
        writeFooter()
        contentStream.close()
        currentPage = createNewPage()
        contentStream = PDPageContentStream(document, currentPage)
        currentY = pageHeight - marginTop
        pageNumber++
    }

    fun ensureSufficientSpace(requiredSpace: Float) {
        if (currentY - requiredSpace < marginBottom + 30f) {
            writeFooter()
            contentStream.close()
            currentPage = createNewPage()
            contentStream = PDPageContentStream(document, currentPage)
            currentY = pageHeight - marginTop
            pageNumber++
        }
    }

    fun shouldDrawSeparator(index: Int, totalClaims: Int): Boolean = index < totalClaims - 1

    fun drawClaimSeparator() {
        contentStream.setLineWidth(0.5f)
        contentStream.setStrokingColor(0.9f, 0.9f, 0.9f)
        contentStream.moveTo(marginLeft + 20f, currentY)
        contentStream.lineTo(pageWidth - marginRight - 20f, currentY)
        contentStream.stroke()
        currentY -= 8f
    }

    fun writeIdNumbersHeader() {
        contentStream.beginText()
        contentStream.setFont(boldFont, 12f)
        contentStream.setNonStrokingColor(0f, 0f, 0f)
        contentStream.newLineAtOffset(marginLeft + 30f, currentY)
        contentStream.showText("ID-nummer:")
        contentStream.endText()
        currentY -= 16f
    }

    fun writeIdNumber(pin: PersonIdInfo) {
        val maskedPnr = maskPersonalNumber(pin.personalIdentificationNumber, pin.country)
        contentStream.beginText()
        contentStream.setFont(regularFont, 12f)
        contentStream.setNonStrokingColor(0.3f, 0.3f, 0.3f)
        contentStream.newLineAtOffset(marginLeft + 45f, currentY)
        contentStream.showText("${pin.country}: $maskedPnr (${pin.sector})")
        contentStream.endText()
        currentY -= 16f
    }

    fun restoreDefaultTextColor() {
        contentStream.setNonStrokingColor(0f, 0f, 0f)
    }

    fun getMarginLeft() = marginLeft

    fun close() {
        writeFooter()
        contentStream.close()
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

fun formatPlaceOfBirth(placeBirth: PlaceBirthInfo?): String? =
    placeBirth?.let { place ->
        listOfNotNull(place.town, place.region, place.country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
    }

fun maskPersonalNumber(pnr: String, country: String): String =
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
