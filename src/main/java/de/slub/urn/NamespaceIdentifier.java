/*
 * Copyright (C) 2016 Saxon State and University Library Dresden (SLUB)
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
 * Represents a Namespace Identifier (NID) part of a Uniform Resource Identifier (URN).
 *
 * It takes care of the validity according to RFC2141.
 *
 * {@code NamespaceIdentifier} instances are immutable, cloneable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceIdentifier {

    private static final String URN_SCHEME = "urn";
    private final String nid;

    /**
     * Creates a new {@code NamespaceIdentifier} instance.
     *
     * @param nid The Namespace Identifier literal
     * @throws URNSyntaxException if the given value is <pre>null</pre>, empty or invalid according to RFC2141.
     */
    public NamespaceIdentifier(String nid) throws URNSyntaxException {
        assertNotNullNotEmpty(nid);
        if (URN_SCHEME.equalsIgnoreCase(nid)) {
            throw new URNSyntaxException(
                    String.format("Namespace identifier can not be '%s'", URN_SCHEME));
        }
        if (!isValidNamespaceIdentifier(nid)) {
            throw new URNSyntaxException(String.format("Not allowed characters in Namespace Identifier '%s'", nid));
        }
        this.nid = nid;
    }

    /**
     * Create a new {@code NamespaceIdentifier} instance that is an exact copy of the given instance.
     *
     * @param instanceForCopying Base instance for copying
     */
    public NamespaceIdentifier(NamespaceIdentifier instanceForCopying) {
        nid = instanceForCopying.nid;
    }

    /**
     * Check if a given literal is a valid namespace identifier
     *
     * @param nid Namespace identifier literal
     * @return True, if the given string complies to the rules for valid namespace identifiers. False, if not.
     */
    abstract protected boolean isValidNamespaceIdentifier(String nid);

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

    @Override
    public int hashCode() {
        return nid.hashCode();
    }

    private void assertNotNullNotEmpty(String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException("Namespace Identifier part cannot be null or empty");
        }
    }

}
