/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.apache.logging.log4j.LogManager
import org.opensearch.OpenSearchException
import org.opensearch.OpenSearchSecurityException
import org.opensearch.OpenSearchStatusException
import org.opensearch.core.common.Strings
import org.opensearch.core.rest.RestStatus
import org.opensearch.index.IndexNotFoundException
import org.opensearch.index.engine.VersionConflictEngineException
import org.opensearch.indices.InvalidIndexNameException
import org.opensearch.node.NodeClosedException
import org.opensearch.transport.NodeNotConnectedException

private val log = LogManager.getLogger(AlertingException::class.java)

/**
 * Class names recorded by [AlertingException.wrap] when it flattens a cause into
 * `Exception("<class name>: <message>")`; the original exception type does not survive that.
 */
private val NODE_UNAVAILABLE_EXCEPTION_NAMES = setOf(
    NodeClosedException::class.java.name,
    NodeNotConnectedException::class.java.name
)

/** Guards against a self-referencing or pathologically deep cause chain. */
private const val MAX_CAUSE_CHAIN_DEPTH = 10

/**
 * Converts into a user friendly message.
 */
class AlertingException(message: String, val status: RestStatus, val ex: Exception) : OpenSearchException(message, ex) {

    override fun status(): RestStatus {
        return status
    }

    companion object {
        @JvmStatic
        fun wrap(ex: Exception): OpenSearchException {
            // Already converted, so hand it back untouched. Every caller between the failure site
            // and the transport boundary wraps whatever it caught, and re-wrapping an
            // AlertingException used to (a) log the same failure once per layer of the unwinding
            // call stack and (b) fall through to the `else` branch below, which would adopt the
            // previous friendly message -- by then often just a transport address -- and discard
            // the status resolved by the first conversion.
            if (ex is AlertingException) {
                return ex
            }

            if (isNodeUnavailable(ex)) {
                // A node leaving the cluster interrupts in-flight work. The run is abandoned and
                // retried on the next schedule, so this is not a failure of the operation itself.
                log.debug("Alerting error: $ex", ex)
            } else {
                log.error("Alerting error: $ex")
            }

            var friendlyMsg = "Unknown error"
            var status = RestStatus.INTERNAL_SERVER_ERROR
            when (ex) {
                is IndexNotFoundException -> {
                    status = ex.status()
                    friendlyMsg = "Configured indices are not found: ${ex.index}"
                }
                is OpenSearchSecurityException -> {
                    status = ex.status()
                    friendlyMsg = "User doesn't have permissions to execute this action. Contact administrator."
                }
                is OpenSearchStatusException -> {
                    status = ex.status()
                    friendlyMsg = ex.message as String
                }
                is IllegalArgumentException -> {
                    status = RestStatus.BAD_REQUEST
                    friendlyMsg = ex.message as String
                }
                is VersionConflictEngineException -> {
                    status = ex.status()
                    friendlyMsg = ex.message as String
                }
                is InvalidIndexNameException -> {
                    status = RestStatus.BAD_REQUEST
                    friendlyMsg = ex.message as String
                }
                else -> {
                    if (!Strings.isNullOrEmpty(ex.message)) {
                        friendlyMsg = ex.message as String
                    }
                }
            }
            // Wrapping the origin exception as runtime to avoid it being formatted.
            // Currently, alerting-kibana is using `error.root_cause.reason` as text in the toast message.
            // Below logic is to set friendly message to error.root_cause.reason.
            return AlertingException(friendlyMsg, status, Exception("${ex.javaClass.name}: ${ex.message}"))
        }

        /**
         * Returns true when [ex], or any cause it wraps, means the local node is shutting down or
         * a peer has gone away, rather than a genuine failure of the operation being attempted.
         *
         * The whole chain is walked, and a cause this function has already flattened is matched by
         * the class name it recorded, so a shutdown stays recognisable once it has been converted.
         *
         * Deliberately a local copy of `isNodeUnavailableFailure()` in wazuh-indexer-alerting
         * rather than a shared helper: alerting consumes this library as a version-pinned
         * published artifact, so sharing it would force the two repositories to be released in
         * lockstep for a logging change.
         */
        private fun isNodeUnavailable(ex: Exception): Boolean {
            var cause: Throwable? = ex
            var depth = 0
            while (cause != null && depth++ < MAX_CAUSE_CHAIN_DEPTH) {
                if (cause is NodeClosedException || cause is NodeNotConnectedException) {
                    return true
                }
                val message = cause.message
                if (message != null && NODE_UNAVAILABLE_EXCEPTION_NAMES.any { message.startsWith("$it:") }) {
                    return true
                }
                val next = cause.cause
                cause = if (next === cause) null else next
            }
            return false
        }

        @JvmStatic
        fun merge(vararg ex: AlertingException): AlertingException {
            var friendlyMsg = ""
            var unwrappedExceptionMsg = ""
            ex.forEach {
                if (friendlyMsg != "") {
                    friendlyMsg += ", ${it.message}"
                    unwrappedExceptionMsg += ", ${it.ex.message}"
                } else {
                    friendlyMsg = it.message.orEmpty()
                    unwrappedExceptionMsg = "${it.ex.message}"
                }
            }
            return AlertingException(friendlyMsg, ex.first().status, Exception(unwrappedExceptionMsg))
        }
    }
}
