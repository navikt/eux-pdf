package no.nav.eux.pdf

import no.nav.eux.pdf.service.EessiU020PdfGen
import no.nav.eux.pdf.service.LocalCaseInfo
import no.nav.eux.pdf.service.PersonIdInfo
import no.nav.eux.pdf.service.PlaceBirthInfo
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
            bicSwift = "DEUTDEFF",
            bankReference = "tuut",
            localCaseNumbers = listOf(
                LocalCaseInfo(
                    country = "NO",
                    caseNumber = "124124",
                    institutionID = "NO:NAVAT05",
                    institutionName = "NAV ACC 05"
                ),
                LocalCaseInfo(
                    country = "SE",
                    caseNumber = "SE789123",
                    institutionID = "SE:FA001",
                    institutionName = "Försäkringskassan"
                )
            )
        )
        val claims = listOf(
            U020Child(
                familyName = "Hansen",
                forename = "Kari",
                dateBirth = "2011-06-01",
                sex = "01",
                familyNameAtBirth = "Andersen",
                forenameAtBirth = "Kari Marie",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "NO",
                        personalIdentificationNumber = "12345678901",
                        sector = "03",
                        institutionID = "NO:NAVAT07",
                        institutionName = "NAV ACC 07"
                    ),
                    PersonIdInfo(
                        country = "SE",
                        personalIdentificationNumber = "19900101-1234",
                        sector = "01",
                        institutionID = "SE:FA001",
                        institutionName = "Försäkringskassan"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Oslo",
                    region = "Oslo",
                    country = "NO"
                ),
                nationality = "NO",
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
                sex = "01",
                familyNameAtBirth = "Eriksen",
                forenameAtBirth = "Erik Johan",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "SE",
                        personalIdentificationNumber = "19850315-5678",
                        sector = "02",
                        institutionID = "SE:FA001",
                        institutionName = "Försäkringskassan"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Stockholm",
                    region = "Stockholm",
                    country = "SE"
                ),
                nationality = "SE",
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
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "DK",
                        personalIdentificationNumber = "150385-1234",
                        sector = "05",
                        institutionID = "DK:BORGER001",
                        institutionName = "Borger.dk"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "København",
                    region = "Hovedstaden",
                    country = "DK"
                ),
                nationality = "DK",
                reimbursementRequestID = "111111",
                sequentialNumber = "3",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "750",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Olsen",
                forename = "Magnus",
                dateBirth = "1992-07-22",
                sex = "01",
                familyNameAtBirth = "Magnusson",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "FI",
                        personalIdentificationNumber = "220792A123B",
                        sector = "04",
                        institutionID = "FI:KELA001",
                        institutionName = "Kela"
                    ),
                    PersonIdInfo(
                        country = "NO",
                        personalIdentificationNumber = "22079298765",
                        sector = "03",
                        institutionID = "NO:NAVAT09",
                        institutionName = "NAV ACC 09"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Helsinki",
                    region = "Uusimaa",
                    country = "FI"
                ),
                nationality = "FI",
                reimbursementRequestID = "111111",
                sequentialNumber = "4",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2021-06-01",
                workingPeriodEnd = "2022-05-31",
                reimbursementPeriodStart = "2025-06-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2022-06-15",
                requestedAmount = "1200",
                requestedCurrency = "EUR"
            ),
            U020Child(
                familyName = "Larsen",
                forename = "Astrid",
                dateBirth = "1978-12-03",
                sex = "02",
                reimbursementRequestID = "111111",
                sequentialNumber = "5",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2019-01-01",
                workingPeriodEnd = "2019-12-31",
                reimbursementPeriodStart = "2024-01-01",
                reimbursementPeriodEnd = "2024-12-31",
                lastPaymentDate = "2020-01-15",
                requestedAmount = "300",
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