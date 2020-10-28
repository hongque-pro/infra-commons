package com.labijie.infra.utils

import java.util.*

class HostAndPort private constructor(
        /** Hostname, IPv4/IPv6 literal, or unvalidated nonsense.  */
        /**
         * Returns the portion of this `HostAndPort` instance that should represent the hostname or
         * IPv4/IPv6 literal.
         *
         *
         * A successful parse does not imply any degree of sanity in this field. For additional
         * validation, see the [HostSpecifier] class.
         *
         * @since 20.0 (since 10.0 as `getHostText`)
         */
        val host: String,
        /** Validated port number in the range [0..65535], or NO_PORT  */
        private val portValue: Int,
        /** True if the parsed host has colons, but no surrounding brackets.  */
        private val hasBracketlessColons: Boolean) {


    /** Return true if this instance has a defined port.  */
    fun hasPort(): Boolean {
        return portValue >= 0
    }

    fun tryFromString(str: String): Optional<HostAndPort> {
        try {
             val hp = HostAndPort.fromString(str)
            return Optional.of(hp)
        } catch (ex: IllegalArgumentException) {
            return Optional.empty()
        }
    }

    /**
     * Get the current port number, failing if no port is defined.
     *
     * @return a validated port number, in the range [0..65535]
     * @throws IllegalStateException if no port is defined. You can use [.withDefaultPort]
     * to prevent this from occurring.
     */
    val port: Int
        get() =
            if (!hasPort())
                throw IllegalStateException("validated port number")
            else portValue


    /**
     * Returns the current port number, with a default if no port is defined.
     */
    fun getPortOrDefault(defaultPort: Int): Int {
        return if (hasPort()) portValue else defaultPort
    }

    /**
     * Provide a default port if the parsed string contained only a host.
     *
     * You can chain this after [.fromString] to include a port in case the port was
     * omitted from the input string. If a port was already provided, then this method is a no-op.
     *
     * @param defaultPort a port number, from [0..65535]
     * @return a HostAndPort instance, guaranteed to have a defined port.
     */
    fun withDefaultPort(defaultPort: Int): HostAndPort {
        checkArgument(isValidPort(defaultPort))
        return if (hasPort() || portValue == defaultPort) {
            this
        } else HostAndPort(host, defaultPort, hasBracketlessColons)
    }

    /**
     * Generate an error if the host might be a non-bracketed IPv6 literal.
     *
     *
     * URI formatting requires that IPv6 literals be surrounded by brackets, like "[2001:db8::1]".
     * Chain this call after [.fromString] to increase the strictness of the parser, and
     * disallow IPv6 literals that don't contain these brackets.
     *
     *
     * Note that this parser identifies IPv6 literals solely based on the presence of a colon. To
     * perform actual validation of IP addresses, see the [InetAddresses.forString]
     * method.
     *
     * @return `this`, to enable chaining of calls.
     * @throws IllegalArgumentException if bracketless IPv6 is detected.
     */
    fun requireBracketsForIPv6(): HostAndPort {
        checkArgument(!hasBracketlessColons, "Possible bracketless IPv6 literal: %s", host)
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        val that = (other as? HostAndPort) ?: return false
        return (this.host == that.host
                && this.portValue == that.portValue
                && this.hasBracketlessColons == that.hasBracketlessColons)
    }


    /** Rebuild the host:port string, including brackets if necessary.  */
    override fun toString(): String {
        // "[]:12345" requires 8 extra bytes.
        val builder = StringBuilder(host.length + 8)
        if (host.indexOf(':') >= 0) {
            builder.append('[').append(host).append(']')
        } else {
            builder.append(host)
        }
        if (hasPort()) {
            builder.append(':').append(portValue)
        }
        return builder.toString()
    }

    companion object {
        /** Magic value indicating the absence of a port number.  */
        private const val NO_PORT = -1

        /**
         * Build a HostAndPort instance from separate host and port values.
         *
         *
         * Note: Non-bracketed IPv6 literals are allowed. Use [.requireBracketsForIPv6] to
         * prohibit these.
         *
         * @param host the host string to parse. Must not contain a port number.
         * @param port a port number from [0..65535]
         * @return if parsing was successful, a populated HostAndPort object.
         * @throws IllegalArgumentException if `host` contains a port number, or `port` is out
         * of range.
         */
        fun fromParts(host: String, port: Int): HostAndPort {
            checkArgument(isValidPort(port), "Port out of range: %s", port)
            val parsedHost = fromString(host)
            checkArgument(!parsedHost.hasPort(), "Host has a port: %s", host)
            return HostAndPort(parsedHost.host, port, parsedHost.hasBracketlessColons)
        }

        private fun checkArgument(condition: Boolean, formatString: String? = "", vararg argValue: Any?) {
            if (!condition) {
                throw IllegalArgumentException(if (formatString.isNullOrBlank()) "invalid argument" else String.format(formatString, argValue))
            }
        }

        /**
         * Build a HostAndPort instance from a host only.
         *
         *
         * Note: Non-bracketed IPv6 literals are allowed. Use [.requireBracketsForIPv6] to
         * prohibit these.
         *
         * @param host the host-only string to parse. Must not contain a port number.
         * @return if parsing was successful, a populated HostAndPort object.
         * @throws IllegalArgumentException if `host` contains a port number.
         * @since 17.0
         */
        fun fromHost(host: String): HostAndPort {
            val parsedHost = fromString(host)
            checkArgument(!parsedHost.hasPort(), "Host has a port: %s", host)
            return parsedHost
        }

        /**
         * Split a freeform string into a host and port, without strict validation.
         *
         * Note that the host-only formats will leave the port field undefined. You can use
         * [.withDefaultPort] to patch in a default value.
         *
         * @param hostPortString the input string to parse.
         * @return if parsing was successful, a populated HostAndPort object.
         * @throws IllegalArgumentException if nothing meaningful could be parsed.
         */
        fun fromString(hostPortString: String?): HostAndPort {
            checkNotNull(hostPortString)
            val host: String
            var portString: String? = null
            var hasBracketlessColons = false

            if (hostPortString.startsWith("[")) {
                val hostAndPort = getHostAndPortFromBracketedHost(hostPortString)
                host = hostAndPort[0]
                portString = hostAndPort[1]
            } else {
                val colonPos = hostPortString.indexOf(':')
                if (colonPos >= 0 && hostPortString.indexOf(':', colonPos + 1) == -1) {
                    // Exactly 1 colon. Split into host:port.
                    host = hostPortString.substring(0, colonPos)
                    portString = hostPortString.substring(colonPos + 1)
                } else {
                    // 0 or 2+ colons. Bare hostname or IPv6 literal.
                    host = hostPortString
                    hasBracketlessColons = colonPos >= 0
                }
            }

            var port = NO_PORT
            if (!(portString.isNullOrBlank())) {
                // Try to parse the whole port string as a number.
                // JDK7 accepts leading plus signs. We don't want to.
                checkArgument(!portString.startsWith("+"), "Unparseable port number: %s", hostPortString)
                try {
                    port = Integer.parseInt(portString)
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Unparseable port number: $hostPortString")
                }

                checkArgument(isValidPort(port), "Port number out of range: %s", hostPortString)
            }

            return HostAndPort(host, port, hasBracketlessColons)
        }

        /**
         * Parses a bracketed host-port string, throwing IllegalArgumentException if parsing fails.
         *
         * @param hostPortString the full bracketed host-port specification. Post might not be specified.
         * @return an array with 2 strings: host and port, in that order.
         * @throws IllegalArgumentException if parsing the bracketed host-port string fails.
         */
        private fun getHostAndPortFromBracketedHost(hostPortString: String): Array<String> {
            checkArgument(
                    hostPortString[0] == '[',
                    "Bracketed host-port string must start with a bracket: %s",
                    hostPortString)
            val colonIndex = hostPortString.indexOf(':')
            val closeBracketIndex = hostPortString.lastIndexOf(']')
            checkArgument(
                    colonIndex > -1 && closeBracketIndex > colonIndex,
                    "Invalid bracketed host/port: %s",
                    hostPortString)

            val host = hostPortString.substring(1, closeBracketIndex)
            if (closeBracketIndex + 1 == hostPortString.length) {
                return arrayOf(host, "")
            } else {
                checkArgument(
                        hostPortString[closeBracketIndex + 1] == ':',
                        "Only a colon may follow a close bracket: %s",
                        hostPortString)
                for (i in closeBracketIndex + 2 until hostPortString.length) {
                    checkArgument(
                            Character.isDigit(hostPortString[i]),
                            "Port must be numeric: %s",
                            hostPortString)
                }
                return arrayOf(host, hostPortString.substring(closeBracketIndex + 2))
            }
        }

        /** Return true for valid port numbers.  */
        private fun isValidPort(port: Int): Boolean {
            return port in 0..65535
        }
    }
}