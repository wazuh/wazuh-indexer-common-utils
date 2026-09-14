package org.opensearch.commons.alerting.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.BytesStreamOutput
import org.opensearch.core.common.io.stream.StreamInput

class DataSourcesTests {
    @Test
    fun `Test DataSources construction with no comments indices`() {
        val dataSources = DataSources(
            ScheduledJob.DOC_LEVEL_QUERIES_INDEX,
            ".opensearch-alerting-finding-history-write",
            "<.opensearch-alerting-finding-history-{now/d}-1>",
            ".opendistro-alerting-alerts",
            ".opendistro-alerting-alert-history-write",
            "<.opendistro-alerting-alert-history-{now/d}-1>",
            mapOf(),
            false
        )
        Assertions.assertNotNull(dataSources)

        val out = BytesStreamOutput()
        dataSources.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newDataSources = DataSources(sin)
        Assertions.assertEquals(ScheduledJob.DOC_LEVEL_QUERIES_INDEX, newDataSources.queryIndex)
        Assertions.assertEquals(".opensearch-alerting-finding-history-write", newDataSources.findingsIndex)
        Assertions.assertEquals("<.opensearch-alerting-finding-history-{now/d}-1>", newDataSources.findingsIndexPattern)
        Assertions.assertEquals(".opendistro-alerting-alerts", newDataSources.alertsIndex)
        Assertions.assertEquals(".opendistro-alerting-alert-history-write", newDataSources.alertsHistoryIndex)
        Assertions.assertEquals("<.opendistro-alerting-alert-history-{now/d}-1>", newDataSources.alertsHistoryIndexPattern)
        Assertions.assertEquals(mapOf<String, Map<String, String>>(), newDataSources.queryIndexMappingsByType)
        Assertions.assertEquals(false, newDataSources.findingsEnabled)
    }

    /**
     * The exact map Security Analytics' `DetectorMonitorConfig.getRuleIndexMappingsByType()` sends
     * for every detector it creates.
     *
     * This shape is the reason the validation below exists at all, and it is not hypothetical: the
     * `init` block runs on construction, on `parse()` and on the `StreamInput` constructor, so a
     * type this guard does not know about does not degrade a detector - it makes `POST
     * /_plugins/_security_analytics/detectors` return HTTP 500 for every detector type, and makes an
     * already-persisted monitor fail to read back. Both times a type was added on the Security
     * Analytics side (`keyword`, then `match_only_text`) it broke detector creation outright until
     * this guard learned about it, and neither break was caught before deployment because nothing
     * here exercised the validation.
     */
    private val securityAnalyticsMappings = mapOf(
        "text" to mapOf("analyzer" to "rule_analyzer"),
        "keyword" to mapOf("normalizer" to "rule_ws_normalizer"),
        "match_only_text" to mapOf("analyzer" to "rule_analyzer")
    )

    private fun dataSources(queryIndexMappingsByType: Map<String, Map<String, String>>) = DataSources(
        queryIndex = ".opensearch-sap-test-detectors-queries",
        findingsIndex = ".opensearch-sap-test-findings",
        findingsIndexPattern = "<.opensearch-sap-test-findings-{now/d}-1>",
        alertsIndex = ".opensearch-sap-test-alerts",
        alertsHistoryIndex = ".opensearch-sap-test-alerts-history-write",
        alertsHistoryIndexPattern = "<.opensearch-sap-test-alerts-history-{now/d}-1>",
        queryIndexMappingsByType = queryIndexMappingsByType,
        findingsEnabled = false
    )

    @Test
    fun `Test DataSources accepts the query index mappings Security Analytics sends`() {
        val dataSources = dataSources(securityAnalyticsMappings)

        Assertions.assertEquals(securityAnalyticsMappings, dataSources.queryIndexMappingsByType)
    }

    @Test
    fun `Test DataSources round-trips the query index mappings Security Analytics sends`() {
        // The guard also runs when a persisted monitor is read back, so a shape that is accepted on
        // creation and rejected on deserialization would strand the monitor rather than reject it.
        val out = BytesStreamOutput()
        dataSources(securityAnalyticsMappings).writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)

        Assertions.assertEquals(securityAnalyticsMappings, DataSources(sin).queryIndexMappingsByType)
    }

    @Test
    fun `Test DataSources accepts match_only_text with an analyzer`() {
        // The WCS maps every unbounded string field (process.command_line, url.*, message) as
        // match_only_text, and rule_analyzer is what keeps a compiled `|contains` query able to
        // match one.
        val mappings = mapOf(
            "text" to mapOf("analyzer" to "rule_analyzer"),
            "match_only_text" to mapOf("analyzer" to "rule_analyzer")
        )

        Assertions.assertEquals(mappings, dataSources(mappings).queryIndexMappingsByType)
    }

    @Test
    fun `Test DataSources rejects an unknown field type`() {
        // `wildcard` stands in for any type nobody has thought about: better a rejected detector
        // than a query index whose fields no rule can reach.
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            dataSources(
                mapOf(
                    "text" to mapOf("analyzer" to "rule_analyzer"),
                    "wildcard" to mapOf("analyzer" to "rule_analyzer")
                )
            )
        }

        Assertions.assertTrue(
            exception.message!!.contains("configurable only for"),
            exception.message
        )
    }

    @Test
    fun `Test DataSources rejects the wrong parameter for a known field type`() {
        // A normalizer is meaningless on match_only_text, and an analyzer is rejected on keyword:
        // each type allows exactly the one parameter its mapping supports.
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            dataSources(
                mapOf(
                    "text" to mapOf("analyzer" to "rule_analyzer"),
                    "match_only_text" to mapOf("normalizer" to "rule_ws_normalizer")
                )
            )
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            dataSources(
                mapOf(
                    "text" to mapOf("analyzer" to "rule_analyzer"),
                    "keyword" to mapOf("analyzer" to "rule_analyzer")
                )
            )
        }
    }

    @Test
    fun `Test DataSources rejects more than one parameter on a field type`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            dataSources(
                mapOf(
                    "text" to mapOf("analyzer" to "rule_analyzer"),
                    "match_only_text" to mapOf(
                        "analyzer" to "rule_analyzer",
                        "normalizer" to "rule_ws_normalizer"
                    )
                )
            )
        }
    }

    @Test
    fun `Test DataSources still requires a text mapping`() {
        // Unchanged for existing callers: relaxing the allowlist must not make `text` optional.
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            dataSources(mapOf("match_only_text" to mapOf("analyzer" to "rule_analyzer")))
        }
    }
}
