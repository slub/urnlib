/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
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

import org.junit.jupiter.api.Test;

import static de.slub.urn.RFC.RFC_2141;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NID_RFC2141Test extends NamespaceIdentifierTest<NID_RFC2141> {

    @Test
    public void Invalid_char_in_namespace_identifier_throw_exception() {
        final String badNamespaceIdentifier = "-is!bn";
        assertThrows(URNSyntaxError.class, () -> newTestInstance(badNamespaceIdentifier));
    }

    @Override
    NID_RFC2141 newTestInstance(String nid) throws URNSyntaxError {
        return new NID_RFC2141(nid);
    }

    @Override
    NID_RFC2141 newTestInstance(NamespaceIdentifier nid) {
        return new NID_RFC2141(nid);
    }

    @Test
    public void Supports_RFC_2141() throws URNSyntaxError {
        NamespaceIdentifier nid = newTestInstance("foo");
        assertEquals(RFC_2141, nid.supportedRFC());
    }

    @Test
    public void Allows_single_letter_namespace_identifier() throws URNSyntaxError {
        newTestInstance("A");
    }

}
