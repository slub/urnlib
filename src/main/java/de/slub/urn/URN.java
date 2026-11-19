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
 * Represents a Uniform Resource Name (URN).
 * <p>
 * Every URN has a Namespace Identifier and Namespace Specific String component and follows the syntax of a certain
 * {@link RFC}. Like with {@link java.net.URI}, new instances are created by static methods:
 * <ul>
 *     <li>{@link #parse(String)} and {@link #create(String)} read a URN literal according to RFC 8141, the current
 *     URN syntax. {@link #of(String, String)} builds a URN from its parts.</li>
 *     <li>{@link #rfc2141()} and {@link #rfc8141()} return a {@link URNParser} for an explicitly chosen syntax.</li>
 *     <li>{@link #in(Namespace)} returns a parser for the URNs of a particular {@link Namespace}, and
 *     {@link #as(Namespace)} converts a URN into one. Both yield a {@link NamespaceURN}, which knows the rules of the
 *     namespace on top of the syntax.</li>
 * </ul>
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
 * <p>
 * This class is not open for extension: it has no accessible constructor, so all of its subclasses are part of this
 * library and conform to the RFC they report by {@link #supportedRFC()}. To add the rules of a namespace, implement
 * {@link Namespace}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax (superseding RFC 2141)</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public abstract class URN implements RFCSupport {

    /**
     * Constructor for the URN types of this library. It is not accessible from outside, so that every URN conforms
     * to an RFC.
     */
    URN() {
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
     * Create a URN by parsing a URN string literal according to RFC 8141.
     *
     * @param urnLiteral String to be parsed into a URN
     * @return The URN
     * @throws IllegalArgumentException if the given string is null
     * @throws URNSyntaxError           if the given string is not a URN according to RFC 8141
     * @see #create(String)
     */
    public static URN_8141 parse(String urnLiteral) throws URNSyntaxError {
        return rfc8141().parse(urnLiteral);
    }

    /**
     * Create a URN from a URI according to RFC 8141.
     *
     * @param uri The URI to be parsed into a URN
     * @return The URN
     * @throws IllegalArgumentException if the given URI is null
     * @throws URNSyntaxError           if the given URI is not a URN according to RFC 8141
     */
    public static URN_8141 parse(URI uri) throws URNSyntaxError {
        return rfc8141().parse(uri);
    }

    /**
     * Create a URN by parsing a URN string literal according to RFC 8141, without a checked exception.
     * <p>
     * Like {@link URI#create(String)}, this method is meant for literals that are known to be valid, such as
     * constants in a program. A syntax error is then a programming error. Use {@link #parse(String)} for input that
     * may be invalid.
     *
     * @param urnLiteral String to be parsed into a URN
     * @return The URN
     * @throws IllegalArgumentException if the given string is null or not a URN according to RFC 8141. In the latter
     *                                  case the cause is a {@link URNSyntaxError}.
     */
    public static URN_8141 create(String urnLiteral) {
        try {
            return parse(urnLiteral);
        } catch (URNSyntaxError e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Build a URN according to RFC 8141 from its parts.
     *
     * @param nid        The Namespace Identifier literal
     * @param encodedNss The URL encoded Namespace Specific String literal
     * @return The URN, without resolution, query or fragment components
     * @throws IllegalArgumentException if any of the parameters is null or empty
     * @throws URNSyntaxError           if any of the parts is not valid according to RFC 8141
     */
    public static URN_8141 of(String nid, String encodedNss) throws URNSyntaxError {
        return of(new NID_RFC8141(nid), new NSS_RFC8141(encodedNss, NamespaceSpecificString.Encoding.URL_ENCODED));
    }

    /**
     * Build a URN according to RFC 8141 from its parts.
     *
     * @param namespaceIdentifier     The Namespace Identifier
     * @param namespaceSpecificString The Namespace Specific String
     * @return The URN, without resolution, query or fragment components
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public static URN_8141 of(NID_RFC8141 namespaceIdentifier, NSS_RFC8141 namespaceSpecificString) {
        return new URN_8141(namespaceIdentifier, namespaceSpecificString, RQF_RFC8141.NULL);
    }

    /**
     * Returns a parser for the URNs of a particular namespace.
     * <p>
     * A literal is parsed according to RFC 8141 and then validated against the rules of the namespace.
     *
     * @param namespace The namespace
     * @param <T>       The type of the name that the namespace reads from a Namespace Specific String
     * @return Parser instance
     * @throws IllegalArgumentException if the namespace is null
     * @see #as(Namespace)
     */
    public static <T> URNParser<NamespaceURN<T>> in(Namespace<T> namespace) {
        return new NamespaceURNParser<>(namespace);
    }

    /**
     * Convert this URN into a URN of a particular namespace, by validating it against the rules of the namespace.
     * <p>
     * The URN literal is kept as it is. This includes resolution, query and fragment components. A URN that does not
     * follow RFC 8141 yet is parsed again according to RFC 8141.
     *
     * @param namespace The namespace
     * @param <T>       The type of the name that the namespace reads from a Namespace Specific String
     * @return The URN of the namespace. This instance, if it already is a URN of the namespace.
     * @throws URNSyntaxError           if the Namespace Identifier does not belong to the namespace, the Namespace
     *                                  Specific String violates its rules, or this URN is not valid according to RFC
     *                                  8141
     * @throws IllegalArgumentException if the namespace is null
     * @see #in(Namespace)
     */
    public final <T> NamespaceURN<T> as(Namespace<T> namespace) throws URNSyntaxError {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        if (this instanceof NamespaceURN && ((NamespaceURN<?>) this).namespace().equals(namespace)) {
            @SuppressWarnings("unchecked") final NamespaceURN<T> same = (NamespaceURN<T>) this;
            return same;
        }
        return NamespaceURN.connect(syntaxURN(), namespace);
    }

    /**
     * Return this URN as a URN according to RFC 8141, without the rules of a namespace.
     */
    URN_8141 syntaxURN() throws URNSyntaxError {
        return rfc8141().parse(toString());
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
     * to its type, see {@link NamespaceSpecificString#normalize()}: the hex digits of percent-encoded octets are upper
     * case. A {@link NamespaceURN} applies the rules of its namespace on top, see {@link Namespace#normalForm(String)}.
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
     *     {@link NamespaceSpecificString#isEquivalentTo(NamespaceSpecificString)}. If this URN is a
     *     {@link NamespaceURN}, the rules of its namespace are applied on top, see
     *     {@link Namespace#normalForm(String)}.</li>
     * </ul>
     * All further components, like the r-, q- and f-components of RFC 8141, are ignored.
     * <p>
     * The other URN may be of any type. The rules of <em>this</em> URN are applied to it, so the answer does not
     * depend on how the other URN was parsed, and a URN is always equivalent to a URN of another type with the same
     * string representation. If the two URNs are of different types the relation is not symmetric: a
     * {@link NamespaceURN} knows equivalence rules that a generic URN does not. Use {@link #areEquivalent(URN, URN)}
     * to ask both.
     *
     * @param other The URN to compare to
     * @return True, if the other URN is not null and equivalent by the rules of this URN
     */
    public final boolean isEquivalentTo(URN other) {
        return other != null
                && namespaceIdentifier().isEquivalentTo(other.namespaceIdentifier())
                && equivalenceForm(namespaceSpecificString()).equals(equivalenceForm(other.namespaceSpecificString()));
    }

    /**
     * Return the literal that a Namespace Specific String is compared by when this URN checks for equivalence. Two
     * Namespace Specific Strings are equivalent by the rules of this URN if their literals are identical.
     */
    String equivalenceForm(NamespaceSpecificString nss) {
        return nss.normalizedLiteral();
    }

    /**
     * Tell whether two URNs are equivalent by the rules of at least one of them.
     * <p>
     * This is the symmetric form of {@link #isEquivalentTo(URN)}. According to RFC 8141, section 3, the rules of a
     * namespace may only add equivalences to the general rules, never remove any. So if one URN is of a generic type
     * and the other is a {@link NamespaceURN}, the answer is the one of the URN that knows more.
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
     * URN types with further components override this method together with {@link #equals(Object)}.
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
     * This implementation compares the Namespace Identifier and the Namespace Specific String. URN types with
     * further components override this method and {@link #hashCode()} to include them.
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
