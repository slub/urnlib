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

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

abstract class URNParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void Null_string_argument_throws_exception() throws URNSyntaxException {
        getURNParser().parse((String) null);
    }

    abstract URNParser<?> getURNParser();

    @Test(expected = IllegalArgumentException.class)
    public void Null_URI_argument_throws_exception() throws URNSyntaxException {
        getURNParser().parse((URI) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Empty_string_throws_IllegalArgumentException() throws URNSyntaxException {
        getURNParser().parse("");
    }

    @Test(expected = URNSyntaxException.class)
    public void Parsing_non_URN_URIs_throws_exception() throws URISyntaxException, URNSyntaxException {
        getURNParser().parse(new URI("http://foo"));
    }

    @Test
    public void URN_string_can_be_parsed_into_its_components() throws Exception {
        final String nid = "isbn";
        final String nss = "0451450523";
        final URN    urn = getURNParser().parse(new URI(String.format("urn:%s:%s", nid, nss)));
        assertEquals("Wrong NID ", nid, urn.getNamespaceIdentifier());
        assertEquals("Wrong NSS", nss, urn.getNamespaceSpecificString());
    }

    @Test(expected = URNSyntaxException.class)
    public void Non_URN_URI_throws_exception() throws URISyntaxException, URNSyntaxException {
        getURNParser().parse(new URI("urn:invalid-urn-part"));
    }

    @Test(expected = URNSyntaxException.class)
    public void URN_string_containing_null_throws_exception() throws URNSyntaxException {
        getURNParser().parse("urn:foo:a123-\u0000-456-%2c");
    }

    @Test(expected = URNSyntaxException.class)
    public void String_containing_empty_URN_parts_throws_exception() throws URNSyntaxException {
        getURNParser().parse("urn::");
    }

    @Test(expected = URNSyntaxException.class)
    public void Parsing_throws_exception_when_NSS_part_is_missing() throws URNSyntaxException {
        getURNParser().parse("urn:foo");
    }

    @Test
    public void Can_parse_URN_with_NSS_having_colons() throws URNSyntaxException {
        URN urn = getURNParser().parse("urn:foo:bar:baz");
        assertEquals("bar:baz", urn.getNamespaceSpecificString());
    }

}
