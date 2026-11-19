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

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents a URN of a particular {@link Namespace}: a URN according to RFC 8141 that also follows the rules of the
 * namespace.
 * <p>
 * Instances are obtained via {@link URN#in(Namespace)} to parse a literal, or via {@link URN#as(Namespace)} to
 * convert a URN. Both validate against RFC 8141 first and against the rules of the namespace second, so every
 * instance is valid by both. The Namespace Identifier, the Namespace Specific String and the string representation
 * are those of the underlying RFC 8141 URN, see {@link #urn()}. The namespace contributes
 * <ul>
 *     <li>the {@linkplain #name() name} it reads from the Namespace Specific String,</li>
 *     <li>its equivalence rules, which {@link #isEquivalentTo(URN)} and {@link #normalize()} apply on top of the
 *     general ones.</li>
 * </ul>
 * Like every URN, a {@code NamespaceURN} is only {@linkplain #equals(Object) equal} to URNs of its own class with
 * equal components. It is {@linkplain #isEquivalentTo(URN) equivalent} to any URN that names the same resource by the
 * rules of its namespace, including {@link URN_2141} and {@link URN_8141} instances. Those do not know the rules of
 * the namespace, so use {@link URN#areEquivalent(URN, URN)} if it is not known which of two URNs is the
 * {@code NamespaceURN}.
 * <p>
 * {@code NamespaceURN} instances are immutable, provided that the names of the namespace are.
 *
 * @param <T> The type of the name that the namespace reads from a Namespace Specific String
 * @author Ralf Claussnitzer
 * @see Namespace
 */
public final class NamespaceURN<T> extends URN {

    private final URN_8141     urn;
    private final Namespace<T> namespace;
    private final T            name;

    private NamespaceURN(URN_8141 urn, Namespace<T> namespace, T name) {
        this.urn = urn;
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * Connect a URN to a namespace by validating it against the rules of the namespace.
     *
     * @param urn       A URN according to RFC 8141
     * @param namespace The namespace
     * @return The URN of the namespace
     * @throws URNSyntaxError if the Namespace Identifier does not belong to the namespace or the Namespace Specific
     *                        String violates its rules
     */
    static <T> NamespaceURN<T> connect(URN_8141 urn, Namespace<T> namespace) throws URNSyntaxError {
        if (!namespace.accepts(urn.namespaceIdentifier())) {
            throw syntaxError(namespace.specification(), String.format(
                    "Namespace Identifier '%s' does not belong to the namespace", urn.namespaceIdentifier()));
        }
        final T name = namespace.read(urn.namespaceSpecificString());
        if (name == null) {
            throw new IllegalStateException(String.format(
                    "Namespace returned no name for Namespace Specific String '%s'", urn.namespaceSpecificString()));
        }
        return new NamespaceURN<>(urn, namespace, name);
    }

    /**
     * Return the underlying URN according to RFC 8141, which does not know the rules of the namespace.
     *
     * @return The RFC 8141 URN with the same string representation as this URN
     */
    public URN_8141 urn() {
        return urn;
    }

    /**
     * Return the namespace of this URN.
     *
     * @return The namespace
     */
    public Namespace<T> namespace() {
        return namespace;
    }

    /**
     * Return the name that the namespace has read from the Namespace Specific String.
     *
     * @return The name
     */
    public T name() {
        return name;
    }

    /**
     * Return this URNs optional RQF components.
     *
     * @return This URNs RQF components
     */
    public RQF_RFC8141 getRQFComponents() {
        return urn.getRQFComponents();
    }

    /**
     * @see URN#namespaceIdentifier()
     */
    @Override
    public NID_RFC8141 namespaceIdentifier() {
        return urn.namespaceIdentifier();
    }

    /**
     * @see URN#namespaceSpecificString()
     */
    @Override
    public NSS_RFC8141 namespaceSpecificString() {
        return urn.namespaceSpecificString();
    }

    /**
     * Return this URN in its normal form: the Namespace Identifier is lower case, the hex digits of percent-encoded
     * octets are upper case, and the rules of the namespace are applied, see {@link Namespace#normalForm(String)}.
     * The resolution, query and fragment components are kept unchanged.
     *
     * @throws IllegalStateException if the namespace violates the contract of {@link Namespace#normalForm(String)}
     *                               and the normal form is not a valid URN of the namespace
     * @see URN#normalize()
     */
    @Override
    public NamespaceURN<T> normalize() {
        final String normalForm = equivalenceForm(urn.namespaceSpecificString());
        try {
            final NSS_RFC8141 nss = new NSS_RFC8141(normalForm, URL_ENCODED);
            if (!nss.normalizedLiteral().equals(normalForm)) {
                throw syntaxError(RFC.RFC_8141, "Percent-encoded octets are not in upper case");
            }
            return connect(
                    new URN_8141(urn.namespaceIdentifier().normalize(), nss, urn.getRQFComponents()), namespace);
        } catch (URNSyntaxError | IllegalArgumentException e) {
            throw new IllegalStateException(String.format(
                    "Namespace turned the valid Namespace Specific String '%s' into the invalid normal form '%s'",
                    urn.namespaceSpecificString(), normalForm), e);
        }
    }

    /**
     * Return this URN without its resolution, query and fragment components.
     *
     * @see URN#assignedName()
     */
    @Override
    public NamespaceURN<T> assignedName() {
        final URN_8141 assignedName = urn.assignedName();
        return assignedName == urn ? this : new NamespaceURN<>(assignedName, namespace, name);
    }

    /**
     * Apply the rules of the namespace on top of the normal form of RFC 8141.
     */
    @Override
    String equivalenceForm(NamespaceSpecificString nss) {
        return namespace.normalForm(nss.normalizedLiteral());
    }

    @Override
    URN_8141 syntaxURN() {
        return urn;
    }

    /**
     * Return the RFC that defines the syntax of this URN. The rules of the namespace are named by
     * {@link Namespace#specification()}.
     *
     * @return {@link RFC#RFC_8141}
     * @see RFCSupport#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return urn.supportedRFC();
    }

    /**
     * Return a URN serialization in URN syntax.
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return urn.toString();
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two URNs of a namespace are equal if their namespaces are equal and their Namespace Identifiers, Namespace
     * Specific Strings and r-, q- and f-components are equal, exactly as given. The equivalence rules of the
     * namespace do not matter here, use {@link #isEquivalentTo(URN)} for them.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code NamespaceURN} of an equal namespace with equal components.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NamespaceURN)
                && urn.equals(((NamespaceURN<?>) obj).urn)
                && namespace.equals(((NamespaceURN<?>) obj).namespace);
    }

    /**
     * Calculates hash code based on the namespace and all components, consistent with {@link #equals(Object)}.
     *
     * @return Hash code of this URN
     */
    @Override
    public int hashCode() {
        return 31 * urn.hashCode() + namespace.hashCode();
    }

}
