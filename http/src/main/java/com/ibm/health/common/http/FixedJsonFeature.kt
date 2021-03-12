package com.ibm.health.common.http

import com.ibm.health.common.http.FixedJsonFeature.Config
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonSerializer
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.accept
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.withContext

/**
 * [HttpClient] feature that serializes/de-serializes as JSON custom objects
 * to request and from response bodies using a [Config.serializer].
 *
 * The default [Config.serializer] is [GsonSerializer].
 *
 * The default [Config.acceptContentTypes] is a list which contains [ContentType.Application.Json]
 *
 * The default [Config.shouldHandle] accepts any `application/<...>+json` pattern.
 *
 * Note: It will de-serialize the body response if the specified type is a public accessible class
 *       and the Content-Type is one of [acceptContentTypes] list (`application/json` by default).
 */
public class FixedJsonFeature(private val config: Config) {
    /**
     * [FixedJsonFeature] configuration that is used during installation
     */
    public class Config {
        /**
         * Serializer that will be used for serializing requests and deserializing response bodies.
         *
         * Default value for [serializer] is [defaultSerializer].
         */
        public var serializer: JsonSerializer
            get() = _serializer ?: { val result = defaultSerializer(); _serializer = result; result }()
            set(value) {
                _serializer = value
            }
        private var _serializer: JsonSerializer? = null

        /**
         * Backing field with mutable list of content types that are handled by this feature.
         */
        private val _acceptContentTypes: MutableList<ContentType> = mutableListOf(ContentType.Application.Json)

        /**
         * List of content types that are handled by this feature.
         * It also affects `Accept` request header value.
         * Please note that wildcard content types are supported but no quality specification provided.
         */
        public var acceptContentTypes: List<ContentType>
            set(value) {
                require(value.isNotEmpty()) { "At least one content type should be provided to acceptContentTypes" }
                _acceptContentTypes.clear()
                _acceptContentTypes.addAll(value)
            }
            get() = _acceptContentTypes

        /**
         * Adds accepted content types.
         */
        public fun accept(vararg contentTypes: ContentType) {
            _acceptContentTypes += contentTypes
        }

        /**
         * Defines when to handle a given [ContentType] (used in addition to [accept]).
         *
         * By default accepts any `application/<...>+json` pattern.
         */
        public var shouldHandle: (ContentType) -> Boolean = {
            val value = it.toString()
            value.startsWith("application/") && value.endsWith("+json")
        }
    }

    /**
     * Companion object for feature installation
     */
    public companion object Feature : HttpClientFeature<Config, FixedJsonFeature> {
        override val key: AttributeKey<FixedJsonFeature> = AttributeKey("Json")

        override fun prepare(block: Config.() -> Unit): FixedJsonFeature =
            FixedJsonFeature(Config().apply(block))

        override fun install(feature: FixedJsonFeature, scope: HttpClient) {
            val config = feature.config
            scope.requestPipeline.intercept(HttpRequestPipeline.Transform) { payload ->
                val contentType = context.contentType() ?: return@intercept
                if (config.acceptContentTypes.none { contentType.match(it) } && !config.shouldHandle(contentType))
                    return@intercept

                context.headers.remove(HttpHeaders.ContentType)

                val serializedContent = withContext(scope.engine.dispatcher) {
                    when (payload) {
                        Unit -> EmptyContent
                        is EmptyContent -> EmptyContent
                        else -> config.serializer.write(payload, contentType)
                    }
                }

                proceedWith(serializedContent)
            }

            scope.responsePipeline.intercept(HttpResponsePipeline.Transform) { (info, body) ->
                if (body !is ByteReadChannel) return@intercept

                val contentType = context.response.contentType()
                if (contentType == null ||
                    config.acceptContentTypes.none { contentType.match(it) } &&
                    !config.shouldHandle(contentType)
                ) {
                    return@intercept
                }

                val parsedBody = withContext(scope.engine.dispatcher) {
                    config.serializer.read(info, body.readRemaining())
                }
                val response = HttpResponseContainer(info, parsedBody)
                proceedWith(response)
            }
        }
    }
}
