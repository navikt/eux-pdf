package no.nav.eux.pdf.service

import org.springframework.stereotype.Service

@Service
class U020PdfService() {

    fun u020Pdf(): ByteArray {
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
                familyName = "eee",
                forename = "ffff",
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
                familyName = "Doe",
                forename = "Jane",
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
                familyName = "Doe3",
                forename = "Jane",
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
                familyName = "Doe4",
                forename = "Jane",
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
                familyName = "Doe5",
                forename = "Jane",
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
                familyName = "Doe6",
                forename = "Jane",
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
                familyName = "Doe7",
                forename = "Jane",
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
        return pdfGen.generateU020Document(master, claims)
    }
}