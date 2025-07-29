package no.nav.eux.pdf.model.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class U020SubdocumentsCollection(
    val totalCount: Int,
    val items: List<SubdocumentItem>
)

data class SubdocumentItem(
    val no: String,
    val subdocuments: List<Subdocument>,
    val id: String
)

data class Subdocument(
    val id: String,
    val mimeType: String,
    val lastUpdate: String,
    val creationDate: String,
    val creator: Creator,
    val hasMultipleVersions: Boolean,
    val versions: List<Version>,
    val parentDocumentId: String,
    val type: String,
    val attachments: List<Any>, // Empty array in the example
    val traits: Traits,
    val validation: Validation
)

data class Creator(
    val id: String,
    val type: String,
    val name: String,
    val organisation: Organisation
)

data class Organisation(
    val id: String,
    val registryNumber: String,
    val name: String,
    val acronym: String,
    val countryCode: String,
    val activeSince: String
)

data class Version(
    val id: String,
    val date: String,
    val user: User
)

data class User(
    val id: String,
    val type: String,
    val name: String,
    val organisation: Organisation
)

data class Traits(
    val fam: String,
    val `for`: String, // Using backticks because 'for' is a Kotlin keyword
    val cid: String
)

data class Validation(
    val status: String
)
