package no.nav.eux.pdf

import no.nav.eux.pdf.service.*
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime

class U029PdfGeneratorTest {

    @Test
    fun `should generate EESSI U029 PDF with PIN support and store in target`() {
        val pdfGen = EessiU029PdfGen()
        val master = U029Master(
            rinasakId = "456",
            sedGVer = "4",
            sedPackage = "Sector Components/Unemployment/U029_Master",
            sedVer = "3",
            reimbursementRequestID = "222222",
            reimbursementContestationID = "333333",
            amendedReimbursementRequestID = "444444",
            updatedTotalAmount = "2500.00",
            currency = "EUR",
            localCaseNumbers = listOf(
                U029LocalCaseInfo(
                    country = "NO",
                    caseNumber = "567890",
                    institutionID = "NO:NAVAT06",
                    institutionName = "NAV ACC 06"
                )
            )
        )
        val claims = listOf(
            U029Child(
                familyName = "Andersen",
                forename = "Lars",
                dateBirth = "1990-08-15",
                sex = "01",
                familyNameAtBirth = "Hansen",
                forenameAtBirth = "Lars Erik",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "NO",
                        personalIdentificationNumber = "15089012345",
                        sector = "03",
                        institutionID = "NO:NAVAT07",
                        institutionName = "NAV ACC 07"
                    ),
                    PersonIdInfo(
                        country = "DK",
                        personalIdentificationNumber = "150890-1234",
                        sector = "02",
                        institutionID = "DK:BORGER002",
                        institutionName = "Borger.dk"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Oslo",
                    region = "Oslo",
                    country = "NO"
                ),
                nationality = "NO",
                reimbursementRequestID = "222222",
                reimbursementContestationID = "333333",
                amendedReimbursementRequestID = "444444",
                sequentialNumber = "1",
                contestedIndividualClaimID = "555555",
                amendedContestedIndividualClaimID = "666666",
                status = "06",
                reasoning = "Uenig i beregningsgrunnlag",
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2021-01-01",
                workingPeriodEnd = "2021-12-31",
                reimbursementPeriodStart = "2026-01-01",
                reimbursementPeriodEnd = "2026-12-31",
                lastPaymentDate = "2022-01-15",
                requestedAmount = "1500",
                requestedCurrency = "EUR"
            ),
            U029Child(
                familyName = "Schmidt",
                forename = "Anna",
                dateBirth = "1985-03-22",
                sex = "02",
                familyNameAtBirth = "Müller",
                forenameAtBirth = "Anna Maria",
                personalIdentificationNumbers = listOf(
                    PersonIdInfo(
                        country = "DE",
                        personalIdentificationNumber = "22038567890",
                        sector = "01",
                        institutionID = "DE:GKV002",
                        institutionName = "Deutsche Krankenversicherung"
                    )
                ),
                placeBirth = PlaceBirthInfo(
                    town = "Hamburg",
                    region = "Hamburg",
                    country = "DE"
                ),
                nationality = "DE",
                reimbursementRequestID = "222222",
                reimbursementContestationID = "333333",
                amendedReimbursementRequestID = "444444",
                sequentialNumber = "2",
                contestedIndividualClaimID = null,
                amendedContestedIndividualClaimID = null,
                status = "01",
                reasoning = null,
                institutionID = "NO:NAVAT08",
                institutionName = "NAVAT08",
                workingPeriodStart = "2020-06-01",
                workingPeriodEnd = "2021-05-31",
                reimbursementPeriodStart = "2025-06-01",
                reimbursementPeriodEnd = "2026-05-31",
                lastPaymentDate = "2021-06-15",
                requestedAmount = "1000",
                requestedCurrency = "EUR"
            )
        )
        val pdfBytes = pdfGen.generateU029Document(master, claims, LocalDateTime.now())
        val targetDir = File("target")
        if (!targetDir.exists())
            targetDir.mkdirs()
        val outputFile = File(targetDir, "eessi-u029-claims-with-pin.pdf")
        outputFile.writeBytes(pdfBytes)
        println("EESSI U029 PDF with PIN support generated and saved to: ${outputFile.absolutePath}")
        assert(pdfBytes.isNotEmpty()) { "PDF bytes should not be empty" }
        assert(outputFile.exists()) { "PDF file should be created" }
        assert(outputFile.length() > 0) { "PDF file should not be empty" }
    }
}
