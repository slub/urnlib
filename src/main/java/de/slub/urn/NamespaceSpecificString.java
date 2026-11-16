/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import java.nio.charset.CharacterCodingException;

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN).
 * <p>
 * {@code NamespaceSpecificString} instances are immutable and comparable by using {@code equals()}.
 * <p>
 * A URL encoded literal is kept as given, so that {@code toString()} returns it unchanged. Comparison follows
 * RFC 8141, section 3: percent-encoded octets are compared case-insensitively and are never decoded. Percent-encoded
 * octets do not have to form valid UTF-8, e.g. when a namespace carries binary data. Such a string can't be decoded,
 * see {@link #unencoded()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceSpecificString implements RFCSupport {

    private final String encoded;
    private final String normalized;
    // Null if the percent-encoded octets are not valid UTF-8
    private final String raw;

    /**
     * Creates a new {@code NamespaceSpecificString} instance.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxError Thrown if the given literal is not valid according to {@code
     *                        isValidURLEncodedNamespaceSpecificString()}
     */
    public NamespaceSpecificString(String nss, Encoding encoding) throws URNSyntaxError {
        if ((nss == null) || (nss.isEmpty())) {
            throw new IllegalArgumentException("Namespace Specific String part cannot be null or empty");
        }
        if (encoding == Encoding.URL_ENCODED) {
            if (!isValidURLEncodedNamespaceSpecificString(nss)) {
                throw syntaxError(supportedRFC(),
                        String.format("Not allowed characters in Namespace Specific String '%s'", nss));
            }
            encoded = nss;
            raw = decodeOrNull(nss);
        } else {
            encoded = encode(nss);
            raw = nss;
        }
        normalized = PercentEncoding.upperCaseOctets(encoded);
    }

    private static String decodeOrNull(String encoded) {
        try {
            return PercentEncoding.decode(encoded);
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NamespaceSpecificString(NamespaceSpecificString instanceForCopying) {
        this.encoded = instanceForCopying.encoded;
        this.normalized = instanceForCopying.normalized;
        this.raw = instanceForCopying.raw;
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

    /**
     * URL Encode all NSS reserved characters of a given string literal.
     * <p>
     * Uses {@code isReservedCharacter()} to determine whether a character is reserved according to the RFC.
     *
     * @param s A string
     * @return URL encoded string according to RFC
     * @throws URNSyntaxError Thrown if the string contains illegal character `0`.
     */
    private String encode(String s) throws URNSyntaxError {
        if (s.indexOf(0) >= 0) {
            throw syntaxError(supportedRFC(), "Illegal character `0` found");
        }
        try {
            return PercentEncoding.encode(s, NamespaceSpecificString::isReservedCharacter);
        } catch (CharacterCodingException e) {
            throw syntaxError(supportedRFC(),
                    "Namespace Specific String contains an unpaired surrogate character that cannot be encoded as UTF-8");
        }
    }

    // Encode reserved character set (RFC 2141, 2.3) and explicitly excluded characters (RFC 2141, 2.4),
    // as well as all non-ASCII characters (RFC 2141, 2.2; RFC 8141, 2.2)
    private static boolean isReservedCharacter(int c) {
        return (c <= 0x20) || (c >= 0x7F)
                || c == '%' || c == '/' || c == '?' || c == '#' || c == '<' || c == '"' || c == '&' || c == '\\'
                || c == '>' || c == '[' || c == ']' || c == '^' || c == '`' || c == '{' || c == '|'
                || c == '}' || c == '~';
    }

    /**
     * Return the decoded Namespace Specific String literal.
     * <p>
     * Use {@link NamespaceSpecificString#toString()} to get an encoded string representation. Use
     * {@link #isDecodable()} to check beforehand whether decoding is possible.
     *
     * @return Decoded Namespace Specific String literal
     * @throws IllegalStateException if the percent-encoded octets are not valid UTF-8 and therefore cannot be decoded
     *                               without loss
     */
    public String unencoded() {
        if (raw == null) {
            throw new IllegalStateException(String.format(
                    "Percent-encoded octets in Namespace Specific String '%s' are not valid UTF-8", encoded));
        }
        return this.raw;
    }

    /**
     * Tell whether the percent-encoded octets of this Namespace Specific String are valid UTF-8, so that
     * {@link #unencoded()} can decode it.
     *
     * @return True, if {@link #unencoded()} will not throw
     */
    public boolean isDecodable() {
        return raw != null;
    }

    /**
     * Calculates hash code based on the case-normalized URL-encoded representation of this namespace specific string.
     *
     * @return This namespace specific strings hash code
     */
    @Override
    public int hashCode() {
        return normalized.hashCode();
    }

    /**
     * Checks for equality with a given object.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code NamespaceSpecificString} instance and is lexically equivalent to
     * this instance, i.e. both are equal after upper-casing the hex digits of percent-encoded octets.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceSpecificString
                && this.normalized.equals(((NamespaceSpecificString) obj).normalized);
    }

    /**
     * Return the encoded Namespace Specific String literal.
     * <p>
     * A URL encoded literal is returned exactly as it was given, including the case of hex digits in percent-encoded
     * octets.
     *
     * @return Encoded Namespace Specific String literal
     */
    @Override
    public String toString() {
        return this.encoded;
    }

    /**
     * @see RFCSupport#supports(RFC)
     */
    @Override
    public boolean supports(RFC rfc) {
        return supportedRFC().equals(rfc);
    }

    /**
     * Represents the state of encoding for a string literal.
     * <p>
     * {@link Encoding#URL_ENCODED} means that a string is URL encoded.
     * {@link Encoding#NOT_ENCODED} means that a string is <b>not</b> URL encoded and might contain characters that are
     * not allowed in URIs.
     */
    public enum Encoding {
        /**
         * The string is URL encoded (percent-encoded).
         */
        URL_ENCODED,
        /**
         * The string is not URL encoded and may contain characters that need encoding.
         */
        NOT_ENCODED
    }

}
