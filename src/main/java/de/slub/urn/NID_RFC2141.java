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

import java.util.regex.Pattern;

import static de.slub.urn.RFC.RFC_2141;

/**
 * Represents a Namespace Identifier (NID) part of a Uniform Resource Identifier (URN).
 * <p>
 * It takes care of the validity according to RFC2141.
 * <p>
 * {@code NamespaceIdentifier} instances are immutable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class NID_RFC2141 extends NamespaceIdentifier {

    private static final Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]?[0-9a-zA-Z-]{0,31}$");

    /**
     * Creates a new {@link NID_RFC2141} instance.
     *
     * @param nid The Namespace Identifier literal
     * @throws URNSyntaxException if the given value is <pre>null</pre>, empty or invalid according to RFC2141.
     */
    public NID_RFC2141(String nid) throws URNSyntaxException {
        super(nid);
    }

    /**
     * @see NamespaceIdentifier#NamespaceIdentifier(NamespaceIdentifier)
     */
    public NID_RFC2141(NamespaceIdentifier instanceForCopying) {
        super(instanceForCopying);
    }

    /**
     * Check if a given literal is a valid namespace identifier according to RFC 2141
     *
     * @param nid Namespace identifier literal
     * @return True, if the given string complies to the rules for valid namespace identifiers. False, if not.
     */
    @Override
    public boolean isValidNamespaceIdentifier(String nid) {
        return allowedNID.matcher(nid).matches();
    }

    /**
     * @see NamespaceIdentifier#supportedRFC()
     */
    @Override
    protected RFC supportedRFC() {
        return RFC_2141;
    }

}
