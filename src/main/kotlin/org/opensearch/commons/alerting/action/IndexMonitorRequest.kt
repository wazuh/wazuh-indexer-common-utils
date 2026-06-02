package org.opensearch.commons.alerting.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.support.WriteRequest
import org.opensearch.commons.alerting.model.DocLevelMonitorInput
import org.opensearch.commons.alerting.model.Monitor
import org.opensearch.commons.alerting.util.IndexPatternUtils
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.rest.RestRequest
import java.io.IOException
import java.util.Locale

class IndexMonitorRequest : ActionRequest {
    val monitorId: String
    val seqNo: Long
    val primaryTerm: Long
    val refreshPolicy: WriteRequest.RefreshPolicy
    val method: RestRequest.Method
    var monitor: Monitor
    val rbacRoles: List<String>?

    /** When true the request originates from an internal plugin (e.g. SAP) and should bypass the max-monitors limit. */
    val internalCaller: Boolean

    constructor(
        monitorId: String,
        seqNo: Long,
        primaryTerm: Long,
        refreshPolicy: WriteRequest.RefreshPolicy,
        method: RestRequest.Method,
        monitor: Monitor,
        rbacRoles: List<String>? = null,
        internalCaller: Boolean = false
    ) : super() {
        this.monitorId = monitorId
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.refreshPolicy = refreshPolicy
        this.method = method
        this.monitor = monitor
        this.rbacRoles = rbacRoles
        this.internalCaller = internalCaller
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : this(
        monitorId = sin.readString(),
        seqNo = sin.readLong(),
        primaryTerm = sin.readLong(),
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin),
        method = sin.readEnum(RestRequest.Method::class.java),
        monitor = Monitor.readFrom(sin) as Monitor,
        rbacRoles = sin.readOptionalStringList(),
        internalCaller = sin.readBoolean()
    )

    override fun validate(): ActionRequestValidationException? {
        if (rejectsIndexPatterns() && hasDocLeveMonitorInput()) {
            val docLevelMonitorInput = monitor.inputs[0] as DocLevelMonitorInput
            if (docLevelMonitorInput.indices.stream().anyMatch { IndexPatternUtils.containsPatternSyntax(it) }) {
                val actionValidationException = ActionRequestValidationException()
                actionValidationException.addValidationError("Index patterns are not supported for doc level monitors.")
                return actionValidationException
            }
        }
        return null
    }

    private fun hasDocLeveMonitorInput() = monitor.inputs.isNotEmpty() && monitor.inputs[0] is DocLevelMonitorInput

    private fun rejectsIndexPatterns(): Boolean {
        if (monitor.monitorType.isBlank() || !isMonitorOfStandardType(monitor.monitorType)) return false
        val type = Monitor.MonitorType.valueOf(this.monitor.monitorType.uppercase(Locale.ROOT))
        return type == Monitor.MonitorType.DOC_LEVEL_MONITOR || type == Monitor.MonitorType.ACTIVE_RESPONSE_MONITOR
    }

    private fun isMonitorOfStandardType(monitorType: String): Boolean {
        val standardMonitorTypes = Monitor.MonitorType.values().map { it.value.uppercase(Locale.ROOT) }.toSet()
        return standardMonitorTypes.contains(monitorType.uppercase(Locale.ROOT))
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        refreshPolicy.writeTo(out)
        out.writeEnum(method)
        monitor.writeTo(out)
        out.writeOptionalStringCollection(rbacRoles)
        out.writeBoolean(internalCaller)
    }
}
