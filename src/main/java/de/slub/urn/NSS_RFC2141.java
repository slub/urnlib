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

import java.util.regex.Pattern;

import static de.slub.urn.RFC.RFC_2141;

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN) according to RFC 2141.
 * <p>
 * The class takes care of all RFC2141 defined encoding and decoding in order to generate valid Namespace Specific
 * Strings.
 * <p>
 * {@code NamespaceSpecificString} instances are immutable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class NSS_RFC2141 extends NamespaceSpecificString {

    private static final Pattern allowedCharacters = Pattern.compile("^([0-9a-zA-Z()+,-.:=@;$_!*']|(%[0-9a-fA-F]{2}))+$");

    /**
     * Creates a new {@code NamespaceSpecificString} instance according to RFC 2141.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxError Thrown if the given literal is not valid according to RFC 2141
     */
    public NSS_RFC2141(String nss, Encoding encoding) throws URNSyntaxError {
        super(nss, encoding);
    }

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NSS_RFC2141(NamespaceSpecificString instanceForCopying) {
        super(instanceForCopying);
    }

    /**
     * Check if a given URL encoded NSS literal is valid according to RFC 2141.
     *
     * @param encoded The URL encoded NSS literal
     * @return True, if the literal is valid according to the RFC 2141.
     */
    @Override
    protected boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
        return allowedCharacters.matcher(encoded).matches();
    }

    /**
     * @see NamespaceSpecificString#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC_2141;
    }

}
