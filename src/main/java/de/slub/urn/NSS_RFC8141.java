/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
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

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN) according to RFC 8141.
 * <p>
 * The class takes care of all RFC8141 defined encoding and decoding in order to generate valid Namespace Specific
 * Strings.
 * <p>
 * {@code NamespaceSpecificString} instances are immutable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class NSS_RFC8141 extends NamespaceSpecificString {

    // RFC 3986: sub-delims plus ":" and "@", which together with unreserved make up pchar
    private static final String PCHAR_SYMBOLS = "-._~!$&'()*+,;=:@";

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
    protected boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
        // NSS = pchar *(pchar / "/")
        return !encoded.startsWith("/")
                && PercentEncoding.isValid(encoded, c -> c == '/' || isPchar(c), true);
    }

    /**
     * Check whether a character is a literal {@code pchar} (RFC 3986, section 3.3), i.e. unreserved, a sub-delim,
     * colon or at sign. Percent-encoded octets are not covered by this check.
     *
     * @param c The character
     * @return True if the character may appear literally as {@code pchar}
     */
    static boolean isPchar(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || PCHAR_SYMBOLS.indexOf(c) >= 0;
    }

    /**
     * @see NamespaceSpecificString#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC.RFC_8141;
    }

}
