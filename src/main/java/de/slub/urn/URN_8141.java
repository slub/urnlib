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
 * Represents a Uniform Resource Name (URN) according to RFC 8141.
 * <p>
 * A new URN instance can be created via the constructor if provided with proper {@link NamespaceIdentifier},
 * {@link NamespaceSpecificString} and {@link RQF_RFC8141} instances. For creating URNs from string literals or URIs,
 * use a {@link URNParser} for RFC 8141 which can be obtained via the {@code URN.rfc8141()} method.
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

    /**
     * Create a new URN from its parts.
     *
     * @param namespaceIdentifier     The Namespace Identifier
     * @param namespaceSpecificString The Namespace Specific String
     * @param rqfComponents           The RQF components, use {@link RQF_RFC8141#NULL} if absent
     * @throws IllegalArgumentException if any of the parameters is null
     */
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
     * Checks for equality with a given object.
     * <p>
     * Two RFC 8141 URNs are equal if their Namespace Identifiers, Namespace Specific Strings and r-, q- and
     * f-components are equal. Note that this is stricter than URN-equivalence as defined in RFC 8141, section 3,
     * which ignores the r-, q- and f-components. Use {@link #isEquivalentTo(URN)} for that.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code URN_8141} with equal components.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN_8141)
                && super.equals(obj)
                && rqfComponents.equals(((URN_8141) obj).rqfComponents);
    }

    /**
     * Calculates hash code based on all components, consistent with {@link #equals(Object)}.
     *
     * @return Hash code of this URN
     */
    @Override
    public int hashCode() {
        return 31 * super.hashCode() + rqfComponents.hashCode();
    }

    /**
     * @see URN#namespaceIdentifier()
     */
    @Override
    public NID_RFC8141 namespaceIdentifier() {
        return namespaceIdentifier;
    }

    /**
     * @see URN#namespaceSpecificString()
     */
    @Override
    public NSS_RFC8141 namespaceSpecificString() {
        return namespaceSpecificString;
    }

    /**
     * @see RFCSupport#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return supportedRfc;
    }

    /**
     * Return this URN in its normal form according to the URN-equivalence rules of RFC 8141, section 3.
     * <p>
     * The resolution, query and fragment components are not part of URN-equivalence. They are kept unchanged.
     *
     * @see URN#normalize()
     */
    @Override
    public URN_8141 normalize() {
        return new URN_8141(namespaceIdentifier.normalize(), namespaceSpecificString.normalize(), rqfComponents);
    }

    @Override
    URN_8141 syntaxURN() {
        return this;
    }

    /**
     * Return this URN without its resolution, query and fragment components.
     *
     * @see URN#assignedName()
     */
    @Override
    public URN_8141 assignedName() {
        return rqfComponents.equals(RQF_RFC8141.NULL)
                ? this
                : new URN_8141(namespaceIdentifier, namespaceSpecificString, RQF_RFC8141.NULL);
    }

}
