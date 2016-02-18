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

package de.slub;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class URNTest {

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_identifier_throws_exception() throws URNSyntaxException {
        new URN(null, "0451450523");
    }

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_specific_string_throws_exception() throws URNSyntaxException {
        new URN("isbn", null);
    }

    @Test
    public void returns_namespace_identifier() throws URNSyntaxException {
        final String nid = new URN("isbn", "0451450523").getNamespaceIdentifier();
        assertEquals("isbn", nid);
    }

    @Test(expected = URNSyntaxException.class)
    public void namespace_identifier_can_not_be_urn() throws URNSyntaxException {
        new URN("urn", "test");
    }

    @Test(expected = URNSyntaxException.class)
    public void invalid_char_in_namespace_identifier_throw_exception() throws URNSyntaxException {
        new URN("-is!bn", "test");
    }

    @Test
    public void returns_namespace_specific_string() throws URNSyntaxException {
        final String nid = new URN("isbn", "0451450523").getNamespaceSpecificString();
        assertEquals("0451450523", nid);
    }

    @Test
    public void toURI() throws URISyntaxException, URNSyntaxException {
        URI uri = new URN("isbn", "0451450523").toURI();
        assertEquals("urn:isbn:0451450523", uri.toASCIIString());
    }

    @Test
    public void reserved_chars_in_URI_are_escaped() throws Exception {
        URI uri = new URN("reserved", "%/?#").toURI();
        assertEquals("urn:reserved:%25%2F%3F%23", uri.toASCIIString());
    }

    @Test
    public void non_URN_chars_in_URI_are_escaped() throws Exception {
        URI uri = new URN("non-urn", "[]&<>^`{|}").toURI();
        assertEquals("urn:non-urn:%5B%5D%26%3C%3E%5E%60%7B%7C%7D", uri.toASCIIString());
    }

    @Test
    public void special_UTF8_chars_are_escaped() throws Exception {
        URI uri = new URN("non-urn", "ÄÜÖ").toURI();
        assertEquals("urn:non-urn:%C3%84%C3%9C%C3%96", uri.toASCIIString());
    }

    @Test(expected = URNSyntaxException.class)
    public void Non_URN_URI_cannot_be_parsed() throws Exception {
        new URN(new URI("http://foo"));
    }

    @Test(expected = URNSyntaxException.class)
    public void Invalid_NID_part_in_URI_cannot_be_parsed() throws Exception {
        new URN(new URI("urn:!?:1234"));
    }

    @Test
    public void URN_string_can_be_parsed_into_its_components() throws Exception {
        URI uri = new URI("urn:isbn:0451450523");
        URN urn = new URN(uri);
        assertEquals("Wrong NID ", "isbn", urn.getNamespaceIdentifier());
        assertEquals("Wrong NSS", "0451450523", urn.getNamespaceSpecificString());
    }

    @Test
    public void URN_parsed_namespace_specific_string_gets_decoded() throws Exception {
        URI uri = new URI("urn:non-urn:%C3%84%C3%9C%C3%96%5B%5D%26%3C%3E%5E%60%7B%7C%7D");
        URN urn = new URN(uri);
        assertEquals("Wrong NSS", "ÄÜÖ[]&<>^`{|}", urn.getNamespaceSpecificString());
    }

}
