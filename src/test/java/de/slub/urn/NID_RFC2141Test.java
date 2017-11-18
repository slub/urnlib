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

import org.junit.Test;

public class NID_RFC2141Test extends NamespaceIdentifierTest {

    @Test(expected = URNSyntaxException.class)
    public void Invalid_char_in_namespace_identifier_throw_exception() throws URNSyntaxException {
        final String badNamespaceIdentifier = "-is!bn";
        newTestInstance(badNamespaceIdentifier);
    }

    @Override
    NamespaceIdentifier newTestInstance(String nid) throws URNSyntaxException {
        return new NID_RFC2141(nid);
    }

    @Override
    NamespaceIdentifier newTestInstance(NamespaceIdentifier nid) throws URNSyntaxException {
        return new NID_RFC2141(nid);
    }
}
