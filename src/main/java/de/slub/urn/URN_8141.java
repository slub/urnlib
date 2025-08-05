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
 * Represents a Uniform Resource Name (URN) according to RFC 8141.
 * <p>
 * A new URN instance can be created via the constructor if provided with proper {@link NamespaceIdentifier},
 * {@link NamespaceSpecificString} and {@link RQF_RFC8141} instances. For creating URNs from string literals or URIs,
 * use a {@link URNParser} for RFC 8141 which can be obtained via the {@code URN.rfc8141()} method.
 * <p>
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class URN_8141 extends URN {

    private final RFC supportedRfc = RFC.RFC_8141;

    private final NID_RFC8141 namespaceIdentifier;
    private final NSS_RFC8141 namespaceSpecificString;

    private final RQF_RFC8141 rqfComponents;

    public URN_8141(NID_RFC8141 namespaceIdentifier, NSS_RFC8141 namespaceSpecificString, RQF_RFC8141 rqfComponents) {
        assertNotNull(namespaceIdentifier, "Namespace identifier cannot be null");
        assertNotNull(namespaceSpecificString, "Namespace specific string cannot be null");
        assertNotNull(rqfComponents, "Resolution-Query-Fragment component cannot be null");
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
        this.rqfComponents = rqfComponents;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Return this URNs optional RQF components.
     *
     * @return This URNs RQF components
     */
    public RQF_RFC8141 getRQFComponents() {
        return rqfComponents;
    }

    /**
     * Return a URN serialization in URN syntax:
     * <pre>&lt;URN&gt; ::= &quot;urn:&quot; &lt;NID&gt; &quot;:&quot; &lt;NSS&gt;</pre>
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return String.format("%s:%s:%s%s", SCHEME, namespaceIdentifier, namespaceSpecificString, rqfComponents);
    }

    /**
     * @see URN#namespaceIdentifier()
     */
    @Override
    public NamespaceIdentifier namespaceIdentifier() {
        return namespaceIdentifier;
    }

    /**
     * @see URN#namespaceSpecificString()
     */
    @Override
    public NamespaceSpecificString namespaceSpecificString() {
        return namespaceSpecificString;
    }

    /**
     * @see RFCSupport#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return supportedRfc;
    }

}
