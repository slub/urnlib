/*
 * Copyright (C) 2016 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static java.lang.Character.forDigit;
import static java.lang.Character.toLowerCase;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN).
 *
 * The class takes care of all RFC2141 defined encoding and decoding in order to generate valid Namespace Specific
 * Strings.
 *
 * Creating a URN instance is done by using one of static factory methods, e.g.:
 * <pre>
 *     {@code
 *      URN urn = NamespaceSpecificString.fromRaw("a123,456");
 *      // or...
 *      URN urn = NamespaceSpecificString.fromEncoded("a123%2C456");
 * }
 * </pre>
 *
 * The difference between {@code fromRaw()} and {@code fromEncoded()} is different validation and further encoding of
 * the given value. When using {@code fromRaw()} the value gets properly encoded and not validated. When using
 * {@code fromEncoded()} the given value must be a valid and properly encoded Namespace Specific String. The encoded
 * value can be obtained via {@code toString()}, while the raw, unencoded value is returned by {@code raw()}.
 *
 * {@code NamespaceSpecificString} instances are immutable, cloneable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceSpecificString {

    public enum NssEncoding {
        URL_ENCODED, NOT_ENCODED
    }

    private final String encoded;
    private final String raw;

    public NamespaceSpecificString(String nss, NssEncoding nssEncoding) throws URNSyntaxException {
        assertNotNullNotEmpty(nss);
        if (nssEncoding == NssEncoding.URL_ENCODED) {
            if (!isValidURLEncodedNamespaceSpecificString(nss)) {
                throw new URNSyntaxException(
                        String.format("Not allowed characters in Namespace Specific String '%s'", nss));
            }
            encoded = lowerCaseOctedPairs(nss);
            raw = decode(encoded);
        } else {
            encoded = encode(nss);
            raw = nss;
        }
    }

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NamespaceSpecificString(NamespaceSpecificString instanceForCopying) {
        this.encoded = instanceForCopying.encoded;
        this.raw = instanceForCopying.raw;
    }

    protected abstract boolean isValidURLEncodedNamespaceSpecificString(String encoded);

    /**
     * Return RFC supported by this namespace specific string instance
     *
     * @return The supported RFC
     */
    protected abstract RFC supportedRFC();

    protected static String encode(String s) throws URNSyntaxException {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == 0) {
                throw new URNSyntaxException("Illegal character `0` found");
            }
            if (isReservedCharacter(c)) {
                appendEncoded(sb, c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Encode reserved character set (RFC 2141, 2.3) and explicitly excluded characters (RFC 2141, 2.4)
    private static boolean isReservedCharacter(char c) {
        return (c > 0x80)
                || ((c >= 0x01) && (c <= 0x20) || ((c >= 0x7F)) && (c <= 0xFF))
                || c == '%' || c == '/' || c == '?' || c == '#' || c == '<' || c == '"' || c == '&' || c == '\\'
                || c == '>' || c == '[' || c == ']' || c == '^' || c == '`' || c == '{' || c == '|'
                || c == '}' || c == '~';
    }

    // http://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/21178195#21178195
    // http://www.utf8-chartable.de/
    // http://www.ascii-code.com/
    private static void appendEncoded(StringBuilder sb, char c) {
        for (byte b : String.valueOf(c).getBytes(UTF_8)) {
            sb.append('%');
            sb.append(toLowerCase(forDigit((b >> 4) & 0xF, 16)));
            sb.append(toLowerCase(forDigit((b & 0xF), 16)));
        }
    }

    protected static String lowerCaseOctedPairs(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        try (StringReader sr = new StringReader(s)) {
            int i;
            while ((i = sr.read()) != -1) {
                char c = (char) i;
                if (c == '%') {
                    sb.append('%')
                            .append(toLowerCase((char) sr.read()))
                            .append(toLowerCase((char) sr.read()));
                } else {
                    sb.append(c);
                }
            }
        } catch (IOException ignored) {
        }
        return sb.toString();
    }

    protected static String decode(String s) throws URNSyntaxException {
        try {
            return URLDecoder.decode(s, UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            // Both Exceptions cannot happen because:
            //  1. the character set is hard-coded and always known
            //  2. the hex encoding has been checked by pattern matching before
            //
            // Should something be wrong with the above mentioned check, throwing
            // a URNSyntaxException is in order.
            throw new URNSyntaxException("Error decoding Namespace Specific String part", e);
        }
    }

    protected static void assertNotNullNotEmpty(String s) throws IllegalArgumentException {
        if ((s == null) || (s.isEmpty())) {
            throw new IllegalArgumentException("Namespace Specific String part cannot be null or empty");
        }
    }

    /**
     * Return the decoded Namespace Specific String literal.
     *
     * @return Decoded Namespace Specific String literal
     */
    public String raw() {
        return this.raw;
    }

    /**
     * Return the encoded Namespace Specific String literal.
     *
     * @return Encoded Namespace Specific String literal
     */
    @Override
    public String toString() {
        return this.encoded;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceSpecificString
                && this.encoded.equals(((NamespaceSpecificString) obj).encoded);
    }

    @Override
    public int hashCode() {
        return encoded.hashCode();
    }

}
