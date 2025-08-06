package no.nav.eux.pdf.integration.mock

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import jakarta.annotation.PreDestroy
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Configuration
class MockWebServerConfiguration(
    val requestBodies: RequestBodies
) {

    val log = logger {}

    private final val server = MockWebServer()

    init {
        server.start(9500)
        server.dispatcher = dispatcher()
    }

    fun mockResponse(request: RecordedRequest, body: String) =
        when (request.method) {
            POST.name() -> mockResponsePost(request, body)
            GET.name() -> mockResponseGet(request)
            else -> defaultResponse()
        }

    fun mockResponsePost(request: RecordedRequest, body: String) =
        when {
            request.path?.contains("/eessiCas/v1/tickets") == true -> casTicketResponse()
            request.path?.contains("?service=") == true -> serviceTicketResponse()
            else -> defaultResponse()
        }

    fun mockResponseGet(request: RecordedRequest) =
        when {
            request.path?.contains("/eessiRest/login/cas") == true -> casLoginResponse()

            request.path?.matches(Regex(".*/eessiRest/Cases/123456$")) == true -> rinaCaseResponse()

            request.path?.matches(Regex(".*/eessiRest/Cases/999999.*")) == true ->
                MockResponse().apply {
                    setResponseCode(404)
                    setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    setBody("""{"error": "Case not found"}""")
                }

            request.path?.contains("/eessiRest/Cases/123456/Documents/f0293bae3c494391851a76d0f6f82f46") == true &&
            request.path?.contains("/Subdocuments") == false ->
                masterDocumentResponse()

            request.path?.contains("/eessiRest/Cases/123456/Documents/non-existent-document-id") == true &&
            request.path?.contains("/Subdocuments") == false ->
                MockResponse().apply {
                    setResponseCode(404)
                    setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    setBody("""{"error": "Document not found"}""")
                }

            request.path?.contains(
                "/eessiRest/Cases/123456/Documents/f0293bae3c494391851a76d0f6f82f46/Subdocuments"
            ) == true && request.path?.contains("subdoc_") == false -> subdocumentsCollectionResponse()

            request.path?.contains(
                "/eessiRest/Cases/123456/Documents/f0293bae3c494391851a76d0f6f82f46/Subdocuments/subdoc_001"
            ) == true -> childDocumentResponse("u020-child-document-001.json")

            request.path?.contains(
                "/eessiRest/Cases/123456/Documents/f0293bae3c494391851a76d0f6f82f46/Subdocuments/subdoc_002"
            ) == true -> childDocumentResponse("u020-child-document-002.json")

            request.path?.contains(
                "/eessiRest/Cases/123456/Documents/f0293bae3c494391851a76d0f6f82f46/Subdocuments/subdoc_003"
            ) == true -> childDocumentResponse("u020-child-document-003.json")

            else -> defaultResponse()
        }

    fun casTicketResponse() =
        MockResponse().apply {
            setResponseCode(201)
            setHeader("Location", "http://localhost:9500/eessiCas/v1/tickets/TGT-123-casTicket")
        }

    fun serviceTicketResponse() =
        MockResponse().apply {
            setResponseCode(200)
            setBody("ST-456-serviceTicket")
        }

    fun casLoginResponse() =
        MockResponse().apply {
            setResponseCode(200)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setHeader(SET_COOKIE, "JSESSIONID=ABCD1234; Path=/; HttpOnly")
            setHeader("X-Auth-Cookie", "xauth-token-123")
            setBody("""{"status": "success"}""")
        }

    fun rinaCaseResponse() =
        MockResponse().apply {
            setResponseCode(200)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setBody(loadMockFile("rinacase-123456.json"))
        }

    fun masterDocumentResponse() =
        MockResponse().apply {
            setResponseCode(200)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setBody(loadMockFile("u020-master-document.json"))
        }

    fun subdocumentsCollectionResponse() =
        MockResponse().apply {
            setResponseCode(200)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setBody(loadMockFile("u020-subdocuments-collection.json"))
        }

    fun childDocumentResponse(fileName: String) =
        MockResponse().apply {
            setResponseCode(200)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setBody(loadMockFile(fileName))
        }

    fun defaultResponse() =
        MockResponse().apply {
            setResponseCode(404)
            setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            setBody("""{"error": "Mock endpoint not found"}""")
        }

    private fun loadMockFile(fileName: String): String =
        try {
            ClassPathResource("mocks/$fileName").inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            log.error(e) { "Failed to load mock file: $fileName" }
            """{"error": "Mock file not found: $fileName"}"""
        }

    @PreDestroy
    fun shutdown() {
        server.shutdown()
    }

    private fun dispatcher() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            log.info { "received ${request.method} ${request.path} with headers=${request.headers}" }
            val body = request.body.readUtf8()
            requestBodies[request.path ?: "unknown"] = body
            return mockResponse(request, body)
        }
    }
}
