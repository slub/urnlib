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

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents a Federated Content URN ({@code urn:fdc:}) according to RFC 4198.
 * <p>
 * RFC 4198 registers the Namespace Identifier {@code fdc} and defines structure and equivalence rules for its
 * Namespace Specific String, see {@link NSS_RFC4198}. Apart from that, a Federated Content URN follows the URN
 * syntax of RFC 8141. It may have resolution, query and fragment components, even though RFC 4198 predates them.
 * <p>
 * For creating URNs from string literals or URIs, use the {@link URNParser} which can be obtained via
 * {@code URN.rfc4198()}. Use {@link #from(URN)} to convert a URN of another type and
 * {@link #of(String, String, String)} to build a new one from its fields.
 * <p>
 * Like every URN, a Federated Content URN is only {@linkplain #equals(Object) equal} to URNs of its own class with
 * equal components. It is {@linkplain #isEquivalentTo(URN) equivalent} to any URN that names the same resource by the
 * rules of RFC 4198, which ignore the case of the ProviderId on top of the general rules. This includes
 * {@link URN_2141} and {@link URN_8141} instances in the {@code fdc} namespace. Those do not know the rule for the
 * ProviderId, so use {@link URN#areEquivalent(URN, URN)} if it is not known which of two URNs is the Federated
 * Content URN.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc4198">A Uniform Resource Name (URN) Namespace for Federated Content</a>
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 */
public final class URN_4198 extends URN {

    /**
     * The Namespace Identifier registered by RFC 4198.
     */
    public static final String NID = "fdc";

    private final NID_RFC8141 namespaceIdentifier;
    private final NSS_RFC4198 namespaceSpecificString;
    private final RQF_RFC8141 rqfComponents;

    /**
     * Create a new URN from its parts.
     *
     * @param namespaceIdentifier     The Namespace Identifier, which has to be {@code fdc} in any case
     * @param namespaceSpecificString The Namespace Specific String
     * @param rqfComponents           The RQF components, use {@link RQF_RFC8141#NULL} if absent
     * @throws IllegalArgumentException if any of the parameters is null or the Namespace Identifier is not
     *                                  {@code fdc}
     */
    public URN_4198(NID_RFC8141 namespaceIdentifier, NSS_RFC4198 namespaceSpecificString, RQF_RFC8141 rqfComponents) {
        assertNotNull(namespaceIdentifier, "Namespace identifier cannot be null");
        assertNotNull(namespaceSpecificString, "Namespace specific string cannot be null");
        assertNotNull(rqfComponents, "Resolution-Query-Fragment component cannot be null");
        if (!isFederatedContent(namespaceIdentifier)) {
            throw new IllegalArgumentException(wrongNamespaceIdentifier(namespaceIdentifier));
        }
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
        this.rqfComponents = rqfComponents;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isFederatedContent(NamespaceIdentifier nid) {
        return NID.equalsIgnoreCase(nid.toString());
    }

    private static String wrongNamespaceIdentifier(NamespaceIdentifier nid) {
        return String.format("Namespace Identifier must be '%s', but is '%s'", NID, nid);
    }

    /**
     * Create a Federated Content URN from the parts of a parsed RFC 8141 URN.
     *
     * @param nid           The Namespace Identifier
     * @param encodedNss    The URL encoded Namespace Specific String literal
     * @param rqfComponents The RQF components
     * @return The Federated Content URN
     * @throws URNSyntaxError if the Namespace Identifier is not {@code fdc} or the Namespace Specific String does not
     *                        follow RFC 4198
     */
    static URN_4198 create(NID_RFC8141 nid, String encodedNss, RQF_RFC8141 rqfComponents) throws URNSyntaxError {
        if (!isFederatedContent(nid)) {
            throw syntaxError(RFC.RFC_4198, wrongNamespaceIdentifier(nid));
        }
        return new URN_4198(
                nid, new NSS_RFC4198(encodedNss, NamespaceSpecificString.Encoding.URL_ENCODED), rqfComponents);
    }

    /**
     * Convert a URN into a Federated Content URN by validating it against RFC 4198.
     * <p>
     * The URN literal is kept as it is. This includes resolution, query and fragment components.
     *
     * @param urn A URN of any type
     * @return The Federated Content URN
     * @throws URNSyntaxError           if the Namespace Identifier is not {@code fdc} or the Namespace Specific String
     *                                  does not follow RFC 4198
     * @throws IllegalArgumentException if {@code urn} is null
     */
    public static URN_4198 from(URN urn) throws URNSyntaxError {
        if (urn == null) {
            throw new IllegalArgumentException("URN cannot be null");
        }
        if (urn instanceof URN_4198) {
            return (URN_4198) urn;
        }
        return URN.rfc4198().parse(urn.toString());
    }

    /**
     * Build a new Federated Content URN from its fields.
     * <p>
     * The ResourceId is taken as not encoded: characters that are not allowed literally get percent-encoded.
     *
     * @param providerId The ProviderId, a domain name
     * @param dateId     The DateId in ISO 8601 basic format ({@code CCYY}, {@code CCYYMM} or {@code CCYYMMDD})
     * @param resourceId The not encoded ResourceId
     * @return The new Federated Content URN, without resolution, query or fragment components
     * @throws URNSyntaxError           if any of the fields is not valid according to RFC 4198
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public static URN_4198 of(String providerId, String dateId, String resourceId) throws URNSyntaxError {
        return new URN_4198(
                new NID_RFC8141(NID), NSS_RFC4198.of(providerId, dateId, resourceId), RQF_RFC8141.NULL);
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
     * Return a URN serialization in URN syntax.
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
     * Return the Namespace Specific String, which gives access to ProviderId, DateId and ResourceId.
     *
     * @see URN#namespaceSpecificString()
     */
    @Override
    public NSS_RFC4198 namespaceSpecificString() {
        return namespaceSpecificString;
    }

    /**
     * Return this URN in its normal form: the Namespace Identifier and the ProviderId are lower case, the hex digits
     * of percent-encoded octets are upper case. The resolution, query and fragment components are kept unchanged.
     *
     * @see URN#normalize()
     */
    @Override
    public URN_4198 normalize() {
        return new URN_4198(namespaceIdentifier.normalize(), namespaceSpecificString.normalize(), rqfComponents);
    }

    /**
     * Return this URN without its resolution, query and fragment components.
     *
     * @see URN#assignedName()
     */
    @Override
    public URN_4198 assignedName() {
        return rqfComponents.equals(RQF_RFC8141.NULL)
                ? this
                : new URN_4198(namespaceIdentifier, namespaceSpecificString, RQF_RFC8141.NULL);
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two Federated Content URNs are equal if their Namespace Identifiers, Namespace Specific Strings and r-, q- and
     * f-components are equal, exactly as given. The case of the ProviderId matters here, use
     * {@link #isEquivalentTo(URN)} for the comparison defined by RFC 4198.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code URN_4198} with equal components.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN_4198)
                && super.equals(obj)
                && rqfComponents.equals(((URN_4198) obj).rqfComponents);
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
     * @see RFCSupport#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC.RFC_4198;
    }

}
