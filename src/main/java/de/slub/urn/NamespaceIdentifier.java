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

import java.util.regex.Pattern;

class NamespaceIdentifier {

    private static final Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]+[0-9a-zA-Z-]{0,31}$");
    private static final String URN_SCHEME = "urn";
    private final String nid;

    public NamespaceIdentifier(String nid) throws URNSyntaxException {
        assertNotNullNotEmpty(nid);
        validateNamespaceIdentifier(nid);
        this.nid = nid;
    }

    private NamespaceIdentifier(NamespaceIdentifier instanceForCloning) {
        this.nid = instanceForCloning.nid;
    }

    @Override
    public String toString() {
        return nid;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceIdentifier
                && this.nid.equalsIgnoreCase(((NamespaceIdentifier) obj).nid);
    }

    @Override
    public int hashCode() {
        return nid.hashCode();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new NamespaceIdentifier(this);
    }

    private void assertNotNullNotEmpty(String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException("Namespace Identifier part cannot be null or empty");
        }
    }

    private void validateNamespaceIdentifier(String nid) throws URNSyntaxException {
        if (URN_SCHEME.equalsIgnoreCase(nid)) {
            throw new URNSyntaxException(
                    String.format("Namespace identifier can not be '%s'", URN_SCHEME));
        }

        if (!allowedNID.matcher(nid).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Identifier '%s'", nid));
        }
    }

}
