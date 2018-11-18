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

/**
 * Represents a Uniform Resource Name (URN) according to RFC 2141.
 * <p>
 * A new URN instance can be created via the constructor if provided with proper {@link NamespaceIdentifier} and
 * {@link NamespaceSpecificString} instances. For creating URNs from string literals or URIs, use a {@link URNParser}
 * for RFC 2141 which can be obtained via the {@code URN.rfc2141()} method.
 * <p>
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class URN_2141 extends URN {

    protected final RFC supportedRfc = RFC.RFC_2141;

    protected final NID_RFC2141 namespaceIdentifier;
    protected final NSS_RFC2141 namespaceSpecificString;

    /**
     * Creates a new {@code URN} instance from a {@code NamespaceIdentifier} (NID) and
     * a {@code NamespaceSpecificString} (NSS).
     *
     * @param namespaceIdentifier     The namespace identifier for this URN
     * @param namespaceSpecificString The namespace specific string for this URN
     * @throws IllegalArgumentException Thrown if any of the arguments are null
     */
    public URN_2141(NID_RFC2141 namespaceIdentifier, NSS_RFC2141 namespaceSpecificString) {
        assertNotNull(namespaceIdentifier, "Namespace identifier cannot be null");
        assertNotNull(namespaceSpecificString, "Namespace specific string cannot be null");
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN_2141)
                && namespaceIdentifier.equals(((URN_2141) obj).namespaceIdentifier)
                && namespaceSpecificString.equals(((URN_2141) obj).namespaceSpecificString)
                && supportedRfc.equals(((URN_2141) obj).supportedRfc);
    }

    /**
     * Return a URN serialization in URN syntax:
     * <pre>&lt;URN&gt; ::= &quot;urn:&quot; &lt;NID&gt; &quot;:&quot; &lt;NSS&gt;</pre>
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return String.format("%s:%s:%s", SCHEME, namespaceIdentifier, namespaceSpecificString);
    }

    @Override
    public String namespaceIdentifier() {
        return namespaceIdentifier.toString();
    }

    @Override
    public String namespaceSpecificString() {
        return namespaceSpecificString.toString();
    }

    @Override
    public RFC supportedRfc() {
        return supportedRfc;
    }
}
