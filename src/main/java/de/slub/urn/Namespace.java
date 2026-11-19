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
 * Defines the rules of a URN namespace on top of the URN syntax.
 * <p>
 * The URN syntax leaves it to the definition of a namespace to constrain the Namespace Specific String (RFC 8141,
 * section 2.2) and to declare more URNs equivalent than the general rules do (RFC 8141, section 3.1). An
 * implementation of this interface expresses both for one namespace. It is the way to extend this library: the URN
 * types themselves are not open for extension.
 * <p>
 * A namespace is connected to a URN by {@link URN#in(Namespace)} or {@link URN#as(Namespace)}, which yield a
 * {@link NamespaceURN}. The library parses and validates the URN according to RFC 8141 before it asks the namespace.
 * So a namespace can reject a URN and interpret it, but it never produces the URN literal and cannot make a URN valid
 * that violates the syntax.
 * <p>
 * Implementations should be immutable and are expected to be used as constants. Two {@link NamespaceURN}s are only
 * {@linkplain NamespaceURN#equals(Object) equal} if their namespaces are equal, so a namespace that is not a
 * singleton has to implement {@code equals()} and {@code hashCode()}.
 *
 * @param <T> The type of the name that the namespace reads from a Namespace Specific String, typically an immutable
 *            value that gives access to the fields of the name
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 */
public interface Namespace<T> {

    /**
     * Tell whether a Namespace Identifier belongs to this namespace.
     * <p>
     * Namespace Identifiers are case-insensitive, so the answer must not depend on case.
     *
     * @param nid A valid Namespace Identifier, never null
     * @return True, if URNs with this Namespace Identifier are subject to the rules of this namespace
     */
    boolean accepts(NamespaceIdentifier nid);

    /**
     * Validate a Namespace Specific String against the rules of this namespace and read the name it denotes.
     * <p>
     * The given Namespace Specific String is valid according to RFC 8141. Its URL encoded literal is available via
     * {@code toString()}.
     *
     * @param nss A valid Namespace Specific String, never null
     * @return The name, never null
     * @throws URNSyntaxError if the Namespace Specific String violates the rules of this namespace. It should refer
     *                        to {@link #specification()}.
     */
    T read(NamespaceSpecificString nss) throws URNSyntaxError;

    /**
     * Apply the equivalence rules of this namespace to a Namespace Specific String literal.
     * <p>
     * Two URNs of this namespace are equivalent if this method returns the same string for both. The argument
     * already is in the normal form of RFC 8141, section 3: the hex digits of percent-encoded octets are upper case.
     * As this method is applied on top of that, a namespace can only add equivalences, as demanded by RFC 8141,
     * section 3.1. The default implementation adds none.
     * <p>
     * The method is also applied to literals of URNs that are not valid in this namespace, in order to compare them.
     * So it must
     * <ul>
     *     <li>depend on its argument only,</li>
     *     <li>return the same result if applied to its own result,</li>
     *     <li>accept any Namespace Specific String literal without throwing,</li>
     *     <li>turn a literal that is valid in this namespace into a literal that is valid in this namespace, and
     *     leave the case of the hex digits of percent-encoded octets as it is.</li>
     * </ul>
     *
     * @param rfcNormalForm A URL encoded Namespace Specific String literal in the normal form of RFC 8141
     * @return The normal form according to this namespace
     */
    default String normalForm(String rfcNormalForm) {
        return rfcNormalForm;
    }

    /**
     * Return the specification that defines this namespace. Syntax errors raised for this namespace refer to it.
     *
     * @return The specification, never null
     */
    Specification specification();

}
