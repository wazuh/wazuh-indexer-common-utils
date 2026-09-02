/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.alerting.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.Version
import org.opensearch.cluster.node.DiscoveryNode
import org.opensearch.core.common.transport.TransportAddress
import org.opensearch.core.rest.RestStatus
import org.opensearch.index.IndexNotFoundException
import org.opensearch.node.NodeClosedException
import org.opensearch.transport.SendRequestTransportException
import java.net.InetAddress

class AlertingExceptionTests {

    private fun node() = DiscoveryNode(
        "node-8",
        TransportAddress(InetAddress.getLoopbackAddress(), 9300),
        Version.CURRENT
    )

    @Test
    fun `test wrap is idempotent`() {
        // Every layer between the failure site and the transport boundary wraps what it caught. If
        // wrap() built a new exception each time, one failure produced one log line per layer.
        val once = AlertingException.wrap(IllegalStateException("boom"))
        val twice = AlertingException.wrap(once as Exception)

        assertSame(once, twice, "Re-wrapping an AlertingException must return the same instance")
    }

    @Test
    fun `test wrap preserves the friendly message and status of the first conversion`() {
        // Re-wrapping used to fall through to the catch-all branch, adopting the previous friendly
        // message and resetting the status to 500.
        val once = AlertingException.wrap(IndexNotFoundException("some-index")) as AlertingException
        val twice = AlertingException.wrap(once) as AlertingException

        assertTrue(
            once.message!!.startsWith("Configured indices are not found:"),
            "First conversion must produce the friendly message, not the raw one"
        )
        assertEquals(once.message, twice.message, "Friendly message must survive re-wrapping")
        assertEquals(RestStatus.NOT_FOUND, once.status)
        assertEquals(once.status, twice.status, "Status must survive re-wrapping")
    }

    @Test
    fun `test wrap still converts a genuine failure`() {
        val wrapped = AlertingException.wrap(IllegalArgumentException("bad request")) as AlertingException

        assertEquals("bad request", wrapped.message)
        assertEquals(RestStatus.BAD_REQUEST, wrapped.status)
    }

    @Test
    fun `test wrap converts a node shutdown without changing the result`() {
        // Node-unavailable causes are logged at debug rather than error, but the conversion itself
        // must be unaffected: callers still get an AlertingException carrying the cause's message.
        val direct = AlertingException.wrap(NodeClosedException(node())) as AlertingException
        assertEquals(RestStatus.INTERNAL_SERVER_ERROR, direct.status)

        // A shutdown seen through a transport hop arrives wrapped; it must convert the same way.
        val hop = SendRequestTransportException(
            node(),
            "indices:data/read/get[s]",
            NodeClosedException(node())
        )
        val throughHop = AlertingException.wrap(hop) as AlertingException
        assertEquals(hop.message, throughHop.message)
    }
}
