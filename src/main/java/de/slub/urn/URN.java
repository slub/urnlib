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
 * {@code rfc2141()} or {@code rfc8141()} methods respectively. URNs of a particular namespace can have a type of
 * their own, e.g. {@link URN_4198} for Federated Content URNs.
 * <p>
 * URNs are immutable and keep their components as given. There are two ways to compare them:
 * <ul>
 *     <li>{@link #equals(Object)} and {@link #hashCode()} follow plain Java value semantics: same class and equal
 *     components, compared exactly.</li>
 *     <li>{@link #isEquivalentTo(URN)} and {@link #areEquivalent(URN, URN)} implement the equivalence defined by the
 *     RFCs, which ignores the case of the Namespace Identifier and of percent-encoded octets as well as r-, q- and
 *     f-components, and may include rules of a particular namespace.</li>
 * </ul>
 * {@link #normalize()} and {@link #assignedName()} produce the normal form that equivalence is based on.
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
     * Returns a parser to produce and parse Federated Content URNs ({@code urn:fdc:}) according to RFC 4198
     *
     * @return Parser instance
     */
    public static URNParser<URN_4198> rfc4198() {
        return new RFC4198Parser();
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
     * Return this URN in its normal form according to the RFC it supports.
     * <p>
     * The scheme and the Namespace Identifier are lower case. The Namespace Specific String is normalized according
     * to its type, see {@link NamespaceSpecificString#normalize()}: for RFC 2141 and RFC 8141 the hex digits of
     * percent-encoded octets are upper case, URN types for a particular namespace may define more.
     * Percent-encoded octets are never decoded.
     * <p>
     * Further components, like the r-, q- and f-components of RFC 8141, are kept unchanged. Use
     * {@link #assignedName()} to remove them.
     * <p>
     * A URN is {@linkplain #isEquivalentTo(URN) equivalent} to its normal form, but only
     * {@linkplain #equals(Object) equal} to it if it already is in normal form. For two URNs of the same type,
     * {@code a.isEquivalentTo(b)} is the same as {@code a.normalize().assignedName().equals(b.normalize().assignedName())}.
     * This makes {@code urn.normalize().assignedName()} a suitable key for collections that are to treat equivalent
     * URNs as the same.
     *
     * @return A URN of the same type in normal form
     */
    public abstract URN normalize();

    /**
     * Return the assigned-name of this URN, i.e. this URN reduced to its Namespace Identifier and Namespace Specific
     * String.
     * <p>
     * RFC 8141, section 2 calls {@code "urn:" NID ":" NSS} the assigned-name. It is the part of a URN that
     * URN-equivalence is defined on. All further components, like the r-, q- and f-components of RFC 8141, are
     * removed. Nothing is normalized.
     *
     * @return A URN of the same type without any components beyond Namespace Identifier and Namespace Specific String
     */
    public abstract URN assignedName();

    /**
     * Tell whether another URN is equivalent to this one, by the rules of this URN.
     * <p>
     * Equivalence answers whether two URNs name the same thing, e.g. for caching, while {@link #equals(Object)}
     * tells whether two objects have the same value. Following RFC 2141, section 5 and RFC 8141, section 3, two URNs
     * are equivalent if
     * <ul>
     *     <li>their Namespace Identifiers differ in case at most, see
     *     {@link NamespaceIdentifier#isEquivalentTo(NamespaceIdentifier)},</li>
     *     <li>their Namespace Specific Strings have the same normal form, see
     *     {@link NamespaceSpecificString#isEquivalentTo(NamespaceSpecificString)}.</li>
     * </ul>
     * All further components, like the r-, q- and f-components of RFC 8141, are ignored.
     * <p>
     * The other URN may be of any type. The rules of <em>this</em> URN are applied to it, so the answer does not
     * depend on how the other URN was parsed, and a URN is always equivalent to a URN of another type with the same
     * string representation. If the two URNs are of different types the relation is not symmetric: a URN type for a
     * particular namespace knows equivalence rules that a generic type does not. Use {@link #areEquivalent(URN, URN)}
     * to ask both.
     *
     * @param other The URN to compare to
     * @return True, if the other URN is not null and equivalent by the rules of this URN
     */
    public final boolean isEquivalentTo(URN other) {
        return other != null
                && namespaceIdentifier().isEquivalentTo(other.namespaceIdentifier())
                && namespaceSpecificString().isEquivalentTo(other.namespaceSpecificString());
    }

    /**
     * Tell whether two URNs are equivalent by the rules of at least one of them.
     * <p>
     * This is the symmetric form of {@link #isEquivalentTo(URN)}. According to RFC 8141, section 3, the rules of a
     * namespace may only add equivalences to the general rules, never remove any. So if one URN is of a generic type
     * and the other is of a type for a particular namespace, the answer is the one of the type that knows more.
     * <p>
     * Across more than two types this relation is not transitive, which is why it is not what
     * {@link #equals(Object)} implements. To collect equivalent URNs, convert them to one type and use
     * {@code urn.normalize().assignedName()} as the key.
     *
     * @param a A URN
     * @param b Another URN
     * @return True, if neither URN is null and at least one is equivalent to the other by its own rules
     */
    public static boolean areEquivalent(URN a, URN b) {
        return a != null && b != null && (a.isEquivalentTo(b) || b.isEquivalentTo(a));
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
