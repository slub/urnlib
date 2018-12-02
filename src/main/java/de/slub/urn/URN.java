/*
 * Copyright (C) 2018 Saxon State and University Library Dresden (SLUB)
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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents the base for Uniform Resource Name (URN) implementations.
 * <p>
 * Every implementation has has a Namespace Identifier and Namespace Specific String component and supports a certain
 * {@link RFC}. New instances can be created using an appropriate {@link URNParser} which can be obtained via the
 * {@code rfc2141()} or {@code rfc8141()} methods respectively.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax (superseeding RFC 2141)</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public abstract class URN implements RFCSupport {

    /**
     * The URI scheme for URNs.
     */
    public static final String SCHEME = "urn";

    /**
     * Returns a parser to produce and parse URNs according to RFC 2141
     *
     * @return Parser instance
     */
    public static URNParser<URN_2141> rfc2141() {
        return new RFC2141Parser();
    }

    /**
     * Returns a parser to produce and parse URNs according to RFC 8141
     *
     * @return Parser instance
     */
    public static URNParser<URN_8141> rfc8141() {
        return new RFC8141Parser();
    }

    /**
     * Return the URNs Namespace Identifier part.
     *
     * @return The Namespace Identifier
     */
    public abstract NamespaceIdentifier namespaceIdentifier();

    /**
     * Return the URNs Namespace Specific String part.
     *
     * @return The URL decoded Namespace Specific String
     */
    public abstract NamespaceSpecificString namespaceSpecificString();

    /**
     * Return a URI instance for this URN.
     *
     * @return URI instance
     * @throws URISyntaxException If a URI couldn't be constructed
     */
    public URI toURI() throws URISyntaxException {
        return new URI(this.toString());
    }

    /**
     * Calculates hash code based on the string representation of this RQF instance.
     *
     * @return This namespace specific strings hash code
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Checks for equality with a given object.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code URN} instance and is lexically equivalent to
     * this instance.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && namespaceIdentifier().equals(((URN) obj).namespaceIdentifier())
                && namespaceSpecificString().equals(((URN) obj).namespaceSpecificString())
                && supportedRFC().equals(((URN) obj).supportedRFC());
    }

}
