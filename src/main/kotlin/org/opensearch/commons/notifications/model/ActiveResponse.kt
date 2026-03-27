/*
 * Copyright (C) 2026, Wazuh Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package org.opensearch.commons.notifications.model

import org.opensearch.commons.notifications.NotificationConstants
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
    val type: String,
    val statefulTimeout: Int? = null,
    val executable: String,
    val args: String,
    val location: String,
    val agentId: String? = null
) : BaseConfigData {

    init {
        require(!Strings.isNullOrEmpty(type)) { "type is null or empty" }
        require(type in listOf("stateful", "stateless")) { "type must be stateful or stateless" }
        require(!Strings.isNullOrEmpty(executable)) { "executable is null or empty" }
        require(!Strings.isNullOrEmpty(location)) { "location is null or empty" }
        require(location in listOf("all", "local", "defined-agent")) { "location must be 'all', 'defined-agent', 'local'" }
        if (location == "defined-agent") {
            require(!Strings.isNullOrEmpty(agentId)) { "agent_id is required when location is defined-agent" }
            require(agentId!!.matches(Regex("^\\d+$"))) { "agent_id must contain only numeric characters" }
        }
        if (type == "stateful") {
            require(statefulTimeout != null) { "stateful_timeout is required for stateful type" }
            require(statefulTimeout > 0) { "stateful_timeout must be greater than 0 for stateful type" }
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
            var statefulTimeout: Int? = null
            var executable: String? = null
            var args: String? = null
            var location: String? = null
            var agentId: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NotificationConstants.TYPE_TAG -> type = parser.text()
                    NotificationConstants.STATEFUL_TIMEOUT_TAG -> statefulTimeout = parser.intValue()
                    NotificationConstants.EXECUTABLE_TAG -> executable = parser.text()
                    NotificationConstants.EXTRA_ARGS_TAG -> args = parser.text()
                    NotificationConstants.LOCATION_TAG -> location = parser.text()
                    NotificationConstants.AGENT_ID_TAG -> agentId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing ActiveResponse")
                    }
                }
            }
            type ?: throw IllegalArgumentException("type field absent")
            executable ?: throw IllegalArgumentException("executable field absent")
            args ?: throw IllegalArgumentException("extra_args field absent")
            location ?: throw IllegalArgumentException("location field absent")
            return ActiveResponse(/*name*/ type, statefulTimeout, executable, args, location, agentId)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        type = input.readString(),
        statefulTimeout = input.readOptionalInt(),
        executable = input.readString(),
        args = input.readString(),
        location = input.readString(),
        agentId = input.readOptionalString()
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeOptionalInt(statefulTimeout)
        output.writeString(executable)
        output.writeString(args)
        output.writeString(location)
        output.writeOptionalString(agentId)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(NotificationConstants.EXECUTABLE_TAG, executable)
            .field(NotificationConstants.EXTRA_ARGS_TAG, args)
            .field(NotificationConstants.LOCATION_TAG, location)
            .field(NotificationConstants.TYPE_TAG, type)
        if (agentId != null) {
            builder.field(NotificationConstants.AGENT_ID_TAG, agentId)
        }
        if (statefulTimeout != null) {
            builder.field(NotificationConstants.STATEFUL_TIMEOUT_TAG, statefulTimeout)
        }
        return builder.endObject()
    }
}
