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

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN) according to RFC 8141.
 * <p>
 * A valid URL encoded string (RFC 8141, section 2) is not empty, does not start with a slash and consists of
 * <ul>
 *     <li>ASCII letters and digits,</li>
 *     <li>the characters <code>- . _ ~ ! $ &amp; ' ( ) * + , ; = : @ /</code>,</li>
 *     <li>percent-encoded octets like {@code %2F}.</li>
 * </ul>
 * Compared to RFC 2141 (see {@link NSS_RFC2141}), the characters <code>/ ~ &amp;</code> may appear literally and
 * {@code %00} is not rejected.
 * <p>
 * A string that is given as {@link NamespaceSpecificString.Encoding#NOT_ENCODED NOT_ENCODED} is percent-encoded by
 * the constructor, as UTF-8. This encodes every character that RFC 2141 does not allow literally, so
 * <code>/ ~ &amp;</code> get encoded as well. To keep them literal, percent-encode the string yourself and pass it
 * as {@link NamespaceSpecificString.Encoding#URL_ENCODED URL_ENCODED}: {@code a/b} and {@code a%2Fb} are different
 * names.
 * <p>
 * Instances are immutable. See {@link NamespaceSpecificString} for how equality differs from equivalence.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class NSS_RFC8141 extends NamespaceSpecificString {

    /**
     * Creates a new {@code NamespaceSpecificString} instance according to RFC 8141.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxError Thrown if the given literal is not valid according to RFC 8141
     */
    public NSS_RFC8141(String nss, Encoding encoding) throws URNSyntaxError {
        super(nss, encoding);
    }

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     * @throws IllegalArgumentException if the parameter is null or not a {@code NSS_RFC8141}
     * @see NamespaceSpecificString#NamespaceSpecificString(NamespaceSpecificString)
     */
    public NSS_RFC8141(NamespaceSpecificString instanceForCopying) {
        super(instanceForCopying);
    }

    /**
     * Check if a given URL encoded NSS literal is valid according to RFC 8141.
     *
     * @param encoded The URL encoded NSS literal
     * @return True, if the literal is valid according to the RFC 8141.
     */
    @Override
    boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
        // NSS = pchar *(pchar / "/")
        return !encoded.startsWith("/")
                && PercentEncoding.isValid(encoded, c -> c == '/' || Parsing.isPchar(c), true);
    }

    /**
     * @see NamespaceSpecificString#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC.RFC_8141;
    }

    /**
     * @see NamespaceSpecificString#normalize()
     */
    @Override
    public NSS_RFC8141 normalize() {
        try {
            return new NSS_RFC8141(normalizedLiteral(), Encoding.URL_ENCODED);
        } catch (URNSyntaxError e) {
            throw new IllegalStateException("Normal form of a valid Namespace Specific String is not valid", e);
        }
    }

}
