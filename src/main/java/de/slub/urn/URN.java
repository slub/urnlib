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
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Represents the base for Uniform Resource Name (URN) implementations.
 * <p>
 * Every implementation has a Namespace Identifier and Namespace Specific String component and supports a certain
 * {@link RFC}. New instances can be created using an appropriate {@link URNParser} which can be obtained via the
 * {@code rfc2141()} or {@code rfc8141()} methods respectively.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax (superseding RFC 2141)</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public abstract class URN implements RFCSupport {

    /**
     * Constructor for subclasses.
     */
    protected URN() {
    }

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
     * Calculates hash code based on the Namespace Identifier and the Namespace Specific String, consistent with
     * {@link #equals(Object)}.
     * <p>
     * Subclasses with further components have to override this method together with {@link #equals(Object)}.
     *
     * @return Hash code of this URN
     */
    @Override
    public int hashCode() {
        return Objects.hash(namespaceIdentifier(), namespaceSpecificString());
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two URNs are equal if they are of the same class and all their components are equal. Components are compared
     * exactly as given, so {@code urn:ISBN:1} and {@code urn:isbn:1} are different objects, and a URN is never equal
     * to a URN of another class, even if both have the same string representation.
     * <p>
     * This implementation compares the Namespace Identifier and the Namespace Specific String. Subclasses with
     * further components have to override this method and {@link #hashCode()} to include them.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is of the same class and has equal components.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && getClass() == obj.getClass()
                && namespaceIdentifier().equals(((URN) obj).namespaceIdentifier())
                && namespaceSpecificString().equals(((URN) obj).namespaceSpecificString());
    }

    /**
     * @see RFCSupport#supports(RFC)
     */
    @Override
    public boolean supports(RFC rfc) {
        return supportedRFC().equals(rfc);
    }

}
