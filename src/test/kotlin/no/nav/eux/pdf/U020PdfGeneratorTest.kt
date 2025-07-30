package no.nav.eux.pdf

import no.nav.eux.pdf.service.EessiU020PdfGen
import no.nav.eux.pdf.service.U020Child
import no.nav.eux.pdf.service.U020Master
import org.junit.jupiter.api.Test
import java.io.File

class U020PdfGeneratorTest {

    @Test
    fun `should generate EESSI U020 PDF and store in target`() {
        val pdfGen = EessiU020PdfGen()
        val master = U020Master(
            rinasakId = "123",
            sedGVer = "4",
            sedPackage = "Sector Components/Unemployment/U020_Master",
            sedVer = "3",
            reimbursementRequestID = "111111",
            numberIndividualClaims = "2",
            totalAmount = "1500.00",
            currency = "EUR",
            iban = "NO156465465NO132156",
            bankReference = "tuut"
        )
        val claims = listOf(
            U020Child(
                familyName = "Hansen",
                forename = "Kari",
                dateBirth = "2011-06-01",
                sex = "01",
                reimbursementRequestID = "111111",
                sequentialNumber = "1",
                institutionID = "NO:NAVAT07",
                institutionName = "NAVAT07",
                workingPeriodStart = "2020-11-15",
                workingPeriodEnd = "2021-02-02",
                reimbursementPeriodStart = "2025-06-11",
                reimbursementPeriodEnd = "2025-06-04",
                lastPaymentDate = "2021-02-03",
                requestedAmount = "1000",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Johansen",
                forename = "Erik",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Andersen",
                forename = "Ingrid",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Olsen",
                forename = "Magnus",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Larsen",
                forename = "Astrid",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Svendsen",
                forename = "Bjørn",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Kristiansen",
                forename = "Lena",
                dateBirth = "1985-03-15",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "2",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "500",
                requestedCurrency = "EUR"
            )
        )
        val pdfBytes = pdfGen.generateU020Document(master, claims)
        val targetDir = File("target")
        if (!targetDir.exists())
            targetDir.mkdirs()
        val outputFile = File(targetDir, "eessi-u020-claims.pdf")
        outputFile.writeBytes(pdfBytes)
        println("EESSI U020 PDF generated and saved to: ${outputFile.absolutePath}")
        assert(pdfBytes.isNotEmpty()) { "PDF bytes should not be empty" }
        assert(outputFile.exists()) { "PDF file should be created" }
        assert(outputFile.length() > 0) { "PDF file should not be empty" }
    }
}