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

import java.net.URI;

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Shared lexical and grammar helpers for URN parsing, used by the RFC specific parsers and by the classes
 * representing URN components.
 */
final class Parsing {

    // RFC 2141, 2.2 and 2.3: <trans> characters other than letters and digits, without the reserved set
    private static final String RFC2141_NSS_SYMBOLS = "()+,-.:=@;$_!*'";

    // RFC 3986: sub-delims plus ":" and "@", which together with unreserved make up pchar
    private static final String PCHAR_SYMBOLS = "-._~!$&'()*+,;=:@";

    private Parsing() {
    }

    /**
     * Make sure a URN literal is neither null nor empty.
     *
     * @param urnLiteral URN literal
     * @return The given URN literal
     * @throws IllegalArgumentException if the literal is null or empty
     */
    static String requireURNLiteral(String urnLiteral) {
        if ((urnLiteral == null) || (urnLiteral.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }
        return urnLiteral;
    }

    /**
     * Return the URN literal of a URI.
     *
     * @param uri URI to take the literal from
     * @return The ASCII string representation of the URI
     * @throws IllegalArgumentException if the URI is null
     */
    static String literalOf(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return uri.toASCIIString();
    }

    /**
     * Split a URN literal into its Namespace Identifier and the remaining scheme specific part.
     * <p>
     * The remaining part starts right after the second colon. It contains the Namespace Specific String
     * and, depending on the RFC, optional further components.
     *
     * @param urnLiteral URN literal, must not be null or empty
     * @param rfc        RFC to report in case of syntax errors
     * @return Array with two elements: the NID literal and the non-empty remainder
     * @throws URNSyntaxError if the literal does not start with "urn:", the NID is empty, or nothing follows the NID
     */
    static String[] splitNamespaceIdentifier(String urnLiteral, RFC rfc) throws URNSyntaxError {
        final String[] parts = urnLiteral.split(":", 3);
        if (parts.length < 3 || !URN.SCHEME.equalsIgnoreCase(parts[0])) {
            throw syntaxError(rfc, String.format("Invalid format `%s` is probably not a URN", urnLiteral));
        }
        if (parts[1].isEmpty()) {
            throw syntaxError(rfc, String.format("Empty Namespace Identifier in `%s`", urnLiteral));
        }
        if (parts[2].isEmpty()) {
            throw syntaxError(rfc, String.format("Empty Namespace Specific String in `%s`", urnLiteral));
        }
        return new String[]{parts[1], parts[2]};
    }

    /**
     * Split a scheme specific part into the Namespace Specific String and the RQF components.
     * <p>
     * The NSS ({@code pchar *(pchar / "/")}) contains neither {@code ?} nor {@code #}, so the first of these
     * characters starts the RQF components.
     *
     * @param schemeSpecificPart Everything following the Namespace Identifier and its colon
     * @param urnLiteral         Complete URN literal to report in case of syntax errors
     * @param rfc                RFC to report in case of syntax errors
     * @return Array with two elements: the non-empty NSS literal and the possibly empty RQF part
     * @throws URNSyntaxError if the NSS is empty
     */
    static String[] splitNamespaceSpecificString(String schemeSpecificPart, String urnLiteral, RFC rfc)
            throws URNSyntaxError {
        int nssEnd = schemeSpecificPart.length();
        for (int i = 0; i < schemeSpecificPart.length(); i++) {
            final char c = schemeSpecificPart.charAt(i);
            if (c == '?' || c == '#') {
                nssEnd = i;
                break;
            }
        }
        if (nssEnd == 0) {
            throw syntaxError(rfc, String.format("Empty Namespace Specific String in `%s`", urnLiteral));
        }
        return new String[]{schemeSpecificPart.substring(0, nssEnd), schemeSpecificPart.substring(nssEnd)};
    }

    /**
     * Check whether a character may appear literally in an RFC 2141 Namespace Specific String, i.e. it is a
     * {@code <trans>} character that is not reserved. Percent-encoded octets are not covered by this check.
     *
     * @param c The character
     * @return True if the character may appear literally
     */
    static boolean isRfc2141NssLiteral(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || RFC2141_NSS_SYMBOLS.indexOf(c) >= 0;
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
     * Check whether a string only contains characters allowed in RFC 8141 r-, q- and f-components, that is
     * {@code pchar}, {@code "/"} and {@code "?"}, with well-formed percent-encoding.
     *
     * @param s The string to check
     * @return True, if the string is valid
     */
    static boolean isValidRqfComponentContent(String s) {
        return PercentEncoding.isValid(s, c -> c == '/' || c == '?' || isPchar(c), true);
    }
}
