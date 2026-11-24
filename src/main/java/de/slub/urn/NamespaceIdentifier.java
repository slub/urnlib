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

import java.util.Locale;

import static de.slub.urn.URN.SCHEME;
import static de.slub.urn.URNSyntaxError.*;

/**
 * Represents a Namespace Identifier (NID) part of a Uniform Resource Identifier (URN).
 * <p>
 * {@code NamespaceIdentifier} instances are immutable. The literal is kept as given, so that {@code toString()}
 * returns it unchanged, and {@code equals()} compares it exactly. Use {@link #isEquivalentTo(NamespaceIdentifier)}
 * for the case-insensitive comparison of the RFCs and {@link #normalize()} for the lower case form.
 * <p>
 * This class is not open for extension: it has no accessible constructor, so all of its subclasses are part of this
 * library and conform to the RFC they report by {@link #supportedRFC()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceIdentifier implements RFCSupport {

    private final String nid;

    /**
     * Creates a new {@code NamespaceIdentifier} instance.
     * <p>
     * The checks that both RFCs share are done here: the literal is not {@code urn} and has at most 32 characters.
     * Everything else is left to {@link #validateNamespaceIdentifier(String)}.
     * <p>
     * Note for subclasses: this constructor calls {@link #validateNamespaceIdentifier(String)} and
     * {@link #supportedRFC()} before the constructor of the subclass has run. Both must not use instance fields of
     * the subclass, which are not initialized yet. Static fields are fine.
     *
     * @param nid The Namespace Identifier literal
     * @throws URNSyntaxError           if the given value is invalid according to
     *                                  {@link #validateNamespaceIdentifier(String)}, reserved or too long
     * @throws IllegalArgumentException if the parameter is null or empty
     */
    NamespaceIdentifier(String nid) throws URNSyntaxError {
        if ((nid == null) || (nid.isEmpty())) {
            throw new IllegalArgumentException("Namespace identifier part cannot be null or empty");
        }
        if (SCHEME.equalsIgnoreCase(nid)) {
            throw reservedIdentifier(supportedRFC(), nid);
        }
        if (nid.length() > 32) {
            throw lengthError(supportedRFC(), nid);
        }
        final String validationError = validateNamespaceIdentifier(nid);
        if (validationError != null) {
            throw syntaxError(supportedRFC(), validationError);
        }
        this.nid = nid;
    }

    /**
     * Create a new {@code NamespaceIdentifier} instance that is an exact copy of the given instance.
     * <p>
     * The literal is taken over without validating it again. So the given instance has to be of the same class as
     * the new one, otherwise the copy could hold a literal that its own class does not accept.
     *
     * @param instanceForCopying Base instance for copying
     * @throws IllegalArgumentException if the parameter is null or of another class than the new instance
     */
    NamespaceIdentifier(NamespaceIdentifier instanceForCopying) {
        if (instanceForCopying == null) {
            throw new IllegalArgumentException("Namespace identifier cannot be null");
        }
        if (instanceForCopying.getClass() != getClass()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot copy a Namespace Identifier of type %s into a %s",
                    instanceForCopying.getClass().getSimpleName(), getClass().getSimpleName()));
        }
        nid = instanceForCopying.nid;
    }

    /**
     * Check if a given literal is a valid namespace identifier and return an error message if not.
     *
     * @param nid Namespace identifier literal
     * @return Error message, if the given string violates the rules for valid namespace identifiers. Null, if not.
     */
    abstract String validateNamespaceIdentifier(String nid);

    /**
     * Calculates hash code based on the Namespace Identifier literal, consistent with {@link #equals(Object)}.
     *
     * @return The hash code for this namespace identifier instance.
     */
    @Override
    public int hashCode() {
        return nid.hashCode();
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two Namespace Identifiers are equal if they are of the same class and have the same literal. The comparison is
     * case-sensitive, so {@code ISBN} and {@code isbn} are different objects although they identify the same
     * namespace.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is of the same class and has the same literal as this instance.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NamespaceIdentifier)
                && getClass() == obj.getClass()
                && this.nid.equals(((NamespaceIdentifier) obj).nid);
    }

    /**
     * Returns the Namespace Identifier literal
     *
     * @return Namespace Identifier literal
     */
    @Override
    public String toString() {
        return nid;
    }

    /**
     * @see RFCSupport#supports(RFC)
     */
    @Override
    public boolean supports(RFC rfc) {
        return supportedRFC().equals(rfc);
    }

    /**
     * Return this Namespace Identifier in its normal form, which is the lower case form.
     * <p>
     * A Namespace Identifier is {@linkplain #isEquivalentTo(NamespaceIdentifier) equivalent} to its normal form, but
     * only {@linkplain #equals(Object) equal} to it if it already is in lower case.
     *
     * @return A Namespace Identifier of the same type in normal form
     */
    public abstract NamespaceIdentifier normalize();

    /**
     * Return the literal of the normal form, for use by implementations of {@link #normalize()}.
     *
     * @return The lower-cased Namespace Identifier literal
     */
    final String normalizedLiteral() {
        return nid.toLowerCase(Locale.ROOT);
    }

    /**
     * Tell whether another Namespace Identifier identifies the same namespace as this one.
     * <p>
     * Namespace Identifiers are case-insensitive (RFC 2141, section 5 and RFC 8141, section 3). Unlike
     * {@link #equals(Object)}, this method ignores case and works across types.
     *
     * @param other The Namespace Identifier to compare to
     * @return True, if the other Namespace Identifier is not null and differs in case at most
     */
    public final boolean isEquivalentTo(NamespaceIdentifier other) {
        return other != null && nid.equalsIgnoreCase(other.nid);
    }

}
