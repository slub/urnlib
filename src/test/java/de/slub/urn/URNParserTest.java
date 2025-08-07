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

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class URNParserTest {

    @Test
    public void Null_string_argument_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> getURNParser().parse((String) null));
    }

    abstract URNParser<?> getURNParser();

    @Test
    public void Null_URI_argument_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> getURNParser().parse((URI) null));
    }

    @Test
    public void Empty_string_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> getURNParser().parse(""));
    }

    @Test
    public void Parsing_non_URN_URIs_throws_exception() {
        assertThrows(URNSyntaxError.class, () -> getURNParser().parse(new URI("http://foo")));
    }

    @Test
    public void URN_string_can_be_parsed_into_its_components() throws Exception {
        final String nid = "isbn";
        final String nss = "0451450523";
        final URN urn = getURNParser().parse(new URI(String.format("urn:%s:%s", nid, nss)));
        assertEquals(nid, urn.namespaceIdentifier().toString(), "Wrong NID ");
        assertEquals(nss, urn.namespaceSpecificString().toString(), "Wrong NSS");
    }

    @Test
    public void Non_URN_URI_throws_exception() {
        assertThrows(URNSyntaxError.class,
                () -> getURNParser().parse(new URI("urn:invalid-urn-part")));
    }

    @Test
    public void URN_string_containing_null_throws_exception() {
        assertThrows(URNSyntaxError.class,
                () -> getURNParser().parse("urn:foo:a123-\u0000-456-%2c"));
    }

    @Test
    public void String_containing_empty_URN_parts_throws_exception() {
        assertThrows(URNSyntaxError.class,
                () -> getURNParser().parse("urn::"));
    }

    @Test
    public void Parsing_throws_exception_when_NSS_part_is_missing() {
        assertThrows(URNSyntaxError.class,
                () -> getURNParser().parse("urn:foo"));
    }

    @Test
    public void Can_parse_URN_with_NSS_having_colons() throws URNSyntaxError {
        URN urn = getURNParser().parse("urn:foo:bar:baz");
        assertEquals("bar:baz", urn.namespaceSpecificString().toString());
    }

}
