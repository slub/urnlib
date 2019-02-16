/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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

import static de.slub.urn.URN.SCHEME;
import static de.slub.urn.URNSyntaxError.syntaxError;
import static de.slub.urn.URNSyntaxError.lengthError;
import static de.slub.urn.URNSyntaxError.reservedIdentifier;

/**
 * Represents a Namespace Identifier (NID) part of a Uniform Resource Identifier (URN).
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceIdentifier implements RFCSupport {

    private final String nid;

    /**
     * Creates a new {@code NamespaceIdentifier} instance.
     *
     * @param nid The Namespace Identifier literal
     * @throws URNSyntaxError       if the given value is <pre>null</pre>, empty or invalid according to the
     *                                  {@code isValidNamespaceIdentifier()} method.
     * @throws IllegalArgumentException if the parameter is null or empty
     */
    public NamespaceIdentifier(String nid) throws URNSyntaxError {
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
     * Check if a given literal is a valid namespace identifier and return an error message if not.
     *
     * @param nid Namespace identifier literal
     * @return Error message, if the given string violates the rules for valid namespace identifiers. Null, if not.
     */
    abstract protected String validateNamespaceIdentifier(String nid);

    /**
     * Create a new {@code NamespaceIdentifier} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     * @throws IllegalArgumentException if parameter is null or empty
     */
    public NamespaceIdentifier(NamespaceIdentifier instanceForCopying) {
        if (instanceForCopying == null) {
            throw new IllegalArgumentException("Namespace identifier cannot be null");
        }
        nid = instanceForCopying.nid;
    }

    /**
     * Calculates hash code based on the string representation of this namespace identifier.
     *
     * @return The hash code for this namespace identifier instance.
     */
    @Override
    public int hashCode() {
        return nid.hashCode();
    }

    /**
     * Checks for equality with a given object.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code NamespaceIdentifier} instance and is lexically equivalent to this instance.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NamespaceIdentifier)
                && this.nid.equalsIgnoreCase(((NamespaceIdentifier) obj).nid);
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

}
