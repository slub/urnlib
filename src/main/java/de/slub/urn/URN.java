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

import java.net.URI;
import java.net.URISyntaxException;

import static de.slub.urn.NamespaceIdentifier.URN_SCHEME;
import static de.slub.urn.RFC.RFC_2141;

/**
 * Represents a Uniform Resource Name (URN).
 *
 * Creating a URN instance is done by using one of static factory methods, e.g.:
 * <pre>
 *     {@code
 *      URN urn = URN.create("isbn", "0451450523");
 *      // or...
 *      URN urn = URN.create("urn:isbn:0451450523");
 * }
 * </pre>
 *
 * One can also use URN instances or URIs to create new a URN instance, e.g.:
 * <pre>
 *     {@code
 *      URN urn = URN.create(new URI("urn:isbn:0451450523"));
 *      // or
 *      URN urn = URN.create(URN.create("isbn", "0451450523"));
 * }
 * </pre>
 *
 * Note that when using the factory methods, the passed namespace specific string has to be properly
 * encoded in respect to the rules described in RFC2141.
 *
 * The URN class itself is not designed to be extended. To represent URNs from other URN namespaces a specific class
 * should utilize this class to parse, serialize and validate proper Namespace Specific String encoding. You should use
 * the specific URN part classes {@link NamespaceIdentifier} and {@link NamespaceSpecificString}.
 *
 * URN instances are immutable, cloneable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
final public class URN {

    private final NamespaceIdentifier namespaceIdentifier;
    private final NamespaceSpecificString namespaceSpecificString;

    /**
     *
     * @param namespaceIdentifier
     * @param namespaceSpecificString
     * @throws IllegalArgumentException
     */
    public URN(NamespaceIdentifier namespaceIdentifier, NamespaceSpecificString namespaceSpecificString) {
        assertNotNull(namespaceIdentifier, "Namespace identifier cannot be null");
        assertNotNull(namespaceSpecificString, "Namespace specific string cannot be null");

        if (!namespaceIdentifier.supportedRFC().equals(namespaceSpecificString.supportedRFC())) {
            throw new IllegalArgumentException("RFCs of namespace identifier and namespace specific string must match");
        }

        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static URNFactory rfc2141() {
        return new URNFactory(RFC_2141);
    }

    /**
     * Create a new URN instance using another URN instance.
     *
     * @param urn URN to duplicate
     * @return New URN instance equal to the given URN instance
     * @throws IllegalArgumentException
     */
    public static URN create(URN urn) {
        assertNotNull(urn, "URN parameter cannot be null");
        return new URN(urn.namespaceIdentifier, urn.namespaceSpecificString);
    }

    /**
     * Return the URNs Namespace Identifier part.
     *
     * @return The Namespace Identifier
     */
    public String getNamespaceIdentifier() {
        return namespaceIdentifier.toString();
    }

    /**
     * Return the URNs Namespace Specific String part.
     *
     * @return The URL decoded Namespace Specific String
     */
    public String getNamespaceSpecificString() {
        return namespaceSpecificString.toString();
    }

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
     * Return a URN serialization in URN syntax: <pre>&lt;URN&gt; ::= &quot;urn:&quot; &lt;NID&gt; &quot;:&quot; &lt;NSS&gt;</pre>
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return String.format("%s:%s:%s", URN_SCHEME, namespaceIdentifier, namespaceSpecificString);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && namespaceIdentifier.equals(((URN) obj).namespaceIdentifier)
                && namespaceSpecificString.equals(((URN) obj).namespaceSpecificString);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
