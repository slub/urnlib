/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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
 * <p>
 * {@code NamespaceSpecificString} instances are immutable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceSpecificString implements RFCSupport {

    private final String encoded;
    private final String raw;

    /**
     * Creates a new {@code NamespaceSpecificString} instance.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxException Thrown if the given literal is not valid according to {@code
     *                            isValidURLEncodedNamespaceSpecificString()}
     */
    public NamespaceSpecificString(String nss, Encoding encoding) throws URNSyntaxException {
        if ((nss == null) || (nss.isEmpty())) {
            throw new IllegalArgumentException("Namespace Specific String part cannot be null or empty");
        }
        if (encoding == Encoding.URL_ENCODED) {
            if (!isValidURLEncodedNamespaceSpecificString(nss)) {
                throw new URNSyntaxException(
                        String.format("Not allowed characters in Namespace Specific String '%s'", nss));
            }
            encoded = lowerCaseOctetPairs(nss);
            raw = decode(encoded);
        } else {
            encoded = encode(nss);
            raw = nss;
        }
    }

    /**
     * Check if a given URL encoded NSS literal is valid.
     * <p>
     * Implementations must validate according to the RFC they represent.
     *
     * @param encoded The URL encoded NSS literal
     * @return True, if the literal is valid according to the RFC.
     */
    protected abstract boolean isValidURLEncodedNamespaceSpecificString(String encoded);

    private static String lowerCaseOctetPairs(String s) {
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
        } catch (IOException e) {
            // IOException thrown by StringReader.read() should not happen when reading a string value.
            // If so, it's a JDK bug or change of API.
            throw new IllegalStateException(e);
        }
        return sb.toString();
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            // Both Exceptions cannot happen because:
            //  1. the character set is hard-coded and always known
            //  2. the hex encoding has been checked by pattern matching before
            //
            // Should something be wrong with the above mentioned check, throwing
            // an RuntimeException signals programmer or JDK error.
            throw new IllegalStateException("Error decoding Namespace Specific String part", e);
        }
    }

    /**
     * URL Encode all NSS reserved characters of a given string literal.
     * <p>
     * Uses {@code isReservedCharacter()} to determine whether a character is reserved according to the RFC.
     *
     * @param s A string
     * @return URL encoded string according to RFC
     * @throws URNSyntaxException Thrown if the string contains illegal character `0`.
     */
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

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NamespaceSpecificString(NamespaceSpecificString instanceForCopying) {
        this.encoded = instanceForCopying.encoded;
        this.raw = instanceForCopying.raw;
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
     * Calculates hash code based on the URL-encoded string representation of this namespace specific string.
     *
     * @return This namespace specific strings hash code
     */
    @Override
    public int hashCode() {
        return encoded.hashCode();
    }

    /**
     * Checks for equality with a given object.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code NamespaceSpecificString} instance and is lexically equivalent to
     * this instance.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceSpecificString
                && this.encoded.equals(((NamespaceSpecificString) obj).encoded);
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

    /**
     * Represents the state of encoding for a string literal.
     *
     * {@link Encoding#URL_ENCODED} means that a string is URL encoded.
     * {@link Encoding#NOT_ENCODED} means that a string is <b>not</b> URL encoded and might contain characters that
     * not allowed in URIs.
     */
    public enum Encoding {
        URL_ENCODED, NOT_ENCODED
    }

}
