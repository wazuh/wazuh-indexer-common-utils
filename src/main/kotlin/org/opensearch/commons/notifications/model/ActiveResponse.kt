/*
 * Copyright Wazuh Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.commons.notifications.model

import org.opensearch.commons.notifications.NotificationConstants
import org.opensearch.commons.notifications.model.BaseConfigData
import org.opensearch.commons.notifications.model.XParser
import org.opensearch.commons.utils.logger
import org.opensearch.core.common.Strings
import org.opensearch.core.common.io.stream.StreamInput
import org.opensearch.core.common.io.stream.StreamOutput
import org.opensearch.core.common.io.stream.Writeable
import org.opensearch.core.xcontent.ToXContent
import org.opensearch.core.xcontent.XContentBuilder
import org.opensearch.core.xcontent.XContentParser
import org.opensearch.core.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Data class representing ActiveResponse configuration.
 */
data class ActiveResponse(
    // val name: String,
    val type: String,
    val stateful_timeout: Int? = null,
    val executable: String,
    val extra_args: String,
    val location: String,
    val agent_id: String? = null
) : BaseConfigData {

    init {
        // require(!Strings.isNullOrEmpty(name)) { "name is null or empty" }
        require(!Strings.isNullOrEmpty(type)) { "type is null or empty" }
        require(type in listOf("stateful", "stateless")) { "type must be stateful or stateless" }
        require(!Strings.isNullOrEmpty(executable)) { "executable is null or empty" }
        require(!Strings.isNullOrEmpty(location)) { "location is null or empty" }
        require(location in listOf("all", "local", "defined-agent")) { "location must be 'all', 'defined-agent', 'local'" }
        if (location == "defined-agent") {
            require(!Strings.isNullOrEmpty(agent_id)) { "agent_id is required when location is defined-agent" }
            require(agent_id!!.matches(Regex("^\\d+$"))) { "agent_id must contain only numeric characters" }
        }
        if (type == "stateful") {
            require(stateful_timeout != null) { "stateful_timeout is required for stateful type" }
            require(stateful_timeout > 0) { "stateful_timeout must be greater than 0 for stateful type" }
        }
    }

    companion object {
        private val log by logger(ActiveResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { ActiveResponse(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): ActiveResponse {
            var type: String? = null
            var stateful_timeout: Int? = null
            var executable: String? = null
            var extra_args: String? = null
            var location: String? = null
            var agent_id: String? = null
            // var name: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    // NotificationConstants.NAME_TAG -> name = parser.text()
                    NotificationConstants.TYPE_TAG -> type = parser.text()
                    NotificationConstants.STATEFUL_TIMEOUT_TAG -> stateful_timeout = parser.intValue()
                    NotificationConstants.EXECUTABLE_TAG -> executable = parser.text()
                    NotificationConstants.EXTRA_ARGS_TAG -> extra_args = parser.text()
                    NotificationConstants.LOCATION_TAG -> location = parser.text()
                    NotificationConstants.AGENT_ID_TAG -> agent_id = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing ActiveResponse")
                    }
                }
            }
            // name ?: throw IllegalArgumentException("name field absent")
            type ?: throw IllegalArgumentException("type field absent")
            executable ?: throw IllegalArgumentException("executable field absent")
            extra_args ?: throw IllegalArgumentException("extra_args field absent")
            location ?: throw IllegalArgumentException("location field absent")
            return ActiveResponse(/*name*/ type, stateful_timeout, executable, extra_args, location, agent_id)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        type = input.readString(),
        stateful_timeout = input.readOptionalInt(),
        executable = input.readString(),
        extra_args = input.readString(),
        location = input.readString(),
        agent_id = input.readOptionalString()
        // name = input.readString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeOptionalInt(stateful_timeout)
        output.writeString(executable)
        output.writeString(extra_args)
        output.writeString(location)
        output.writeOptionalString(agent_id)
        // output.writeString(name)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(NotificationConstants.EXECUTABLE_TAG, executable)
            .field(NotificationConstants.EXTRA_ARGS_TAG, extra_args)
            .field(NotificationConstants.LOCATION_TAG, location)
            .field(NotificationConstants.TYPE_TAG, type)
            // .field(NotificationConstants.NAME_TAG, name)
        if (agent_id != null) builder.field(NotificationConstants.AGENT_ID_TAG, agent_id)
        if (stateful_timeout != null) builder.field(NotificationConstants.STATEFUL_TIMEOUT_TAG, stateful_timeout)
        return builder.endObject()
    }
}
