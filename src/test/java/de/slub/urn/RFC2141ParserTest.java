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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RFC2141ParserTest extends URNParserTest {

    @Test
    public void Invalid_NID_part_in_URI_throws_exception() {
        assertThrows(URNSyntaxError.class, () -> getURNParser().parse(new URI("urn:!?:1234")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"u", "r", "n", "U"})
    public void Single_letter_NID_from_the_scheme_is_parsed_correctly(String nid) throws URNSyntaxError {
        final URN urn = getURNParser().parse("urn:" + nid + ":abc");
        assertEquals(nid, urn.namespaceIdentifier().toString(), "Wrong NID");
        assertEquals("abc", urn.namespaceSpecificString().toString(), "Wrong NSS");
    }

    @Override
    URNParser<URN_2141> getURNParser() {
        return URN.rfc2141();
    }
}
