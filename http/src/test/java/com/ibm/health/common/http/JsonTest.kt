package com.ibm.health.common.http

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.client.utils.buildHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

internal class JsonTest {
    @Test
    fun `request without body json object results in empty body without content-type`() = runBlockingTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        request.body.toByteReadPacket().readText(),
                        headers = buildHeaders {
                            append("X-ContentType", request.body.contentType.toString())
                        }
                    )
                }
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
            install(FixedJsonFeature) {
                serializer = GsonSerializer {
                    disableHtmlEscaping()
                    serializeNulls()
                }
            }
        }

        val response: HttpResponse = client.get("https://test.com")
        assertThat(response.readText()).isEqualTo("")
        assertThat(response.headers["X-ContentType"]).isEqualTo("null")
    }
}
