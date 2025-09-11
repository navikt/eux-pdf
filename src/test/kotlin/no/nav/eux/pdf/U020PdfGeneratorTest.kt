package no.nav.eux.pdf

import no.nav.eux.pdf.service.*
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

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
                U020LocalCaseInfo(
                    country = "NO",
                    caseNumber = "124124",
                    institutionID = "NO:NAVAT05",
                    institutionName = "NAV ACC 05"
                ),
                U020LocalCaseInfo(
                    country = "SE",
                    caseNumber = "SE789123",
                    institutionID = "SE:FA001",
                    institutionName = "Försäkringskassan"
                )
            )
        )
        val claims = listOf(
            U020Child(
                familyName = "Januškevičius",
                forename = "Petras",
                dateBirth = "2011-06-01",
                sex = "01",
                familyNameAtBirth = "Žemaitis",
                forenameAtBirth = "Petras Antanas",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "LT",
                        personalIdentificationNumber = "39906012345",
                        sector = "03",
                        institutionID = "LT:SODRA001",
                        institutionName = "Valstybinio socialinio draudimo fondo valdyba"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Vilnius",
                    region = "Vilniaus apskritis",
                    country = "LT"
                ),
                nationality = "LT",
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
                familyName = "Åström",
                forename = "Erik Göran",
                dateBirth = "1985-03-15",
                sex = "01",
                familyNameAtBirth = "Eriksson",
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
                    town = "Göteborg",
                    region = "Västra Götalands län",
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
                familyName = "Jørgensen",
                forename = "Åse",
                dateBirth = "1985-03-15",
                sex = "02",
                familyNameAtBirth = "Lærdal",
                forenameAtBirth = "Åse Margrethe",
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
                familyName = "Müller",
                forename = "Jürgen",
                dateBirth = "1992-07-22",
                sex = "01",
                familyNameAtBirth = "Weiß",
                forenameAtBirth = "Jürgen François",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "DE",
                        personalIdentificationNumber = "22079298765",
                        sector = "03",
                        institutionID = "DE:GKV001",
                        institutionName = "Deutsche Gesetzliche Krankenversicherung"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "München",
                    region = "Bayern",
                    country = "DE"
                ),
                nationality = "DE",
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
                familyName = "Čechová",
                forename = "Božena",
                dateBirth = "1978-12-03",
                sex = "02",
                familyNameAtBirth = "Dvořáková",
                forenameAtBirth = "Božena Růžena",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "CZ",
                        personalIdentificationNumber = "7812036789",
                        sector = "01",
                        institutionID = "CZ:CSSZ001",
                        institutionName = "Česká správa sociálního zabezpečení"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Brno",
                    region = "Jihomoravský kraj",
                    country = "CZ"
                ),
                nationality = "CZ",
                reimbursementRequestID = "111111",
                sequentialNumber = "5",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2019-01-01",
                workingPeriodEnd = "2020-12-31",
                reimbursementPeriodStart = "2025-01-01",
                reimbursementPeriodEnd = "2025-12-31",
                lastPaymentDate = "2021-01-15",
                requestedAmount = "850",
                requestedCurrency = "EUR"
            )
        )
        val pdfBytes = pdfGen.generateU020Document(master, claims, LocalDateTime.now())
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