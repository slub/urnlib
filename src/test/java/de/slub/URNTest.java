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
import static org.junit.Assert.assertNotEquals;

public class URNTest {

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_identifier_throws_exception() throws URNSyntaxException {
        new URN(null, "0451450523");
    }

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_specific_string_throws_exception() throws URNSyntaxException {
        new URN("isbn", null);
    }

    @Test(expected = URNSyntaxException.class)
    public void Namespace_specific_string_containing_null_throws_exception() throws Exception {
        new URN("isbn", "\u0000");
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
        URI uri = new URN("reserved", "%/?#,").toURI();
        assertEquals("urn:reserved:%25%2f%3f%23,", uri.toASCIIString());
    }

    @Test
    public void non_URN_chars_in_URI_are_escaped() throws Exception {
        URI uri = new URN("non-urn", "[]&<>^`{|}").toURI();
        assertEquals("urn:non-urn:%5b%5d%26%3c%3e%5e%60%7b%7c%7d", uri.toASCIIString());
    }

    @Test
    public void special_UTF8_chars_are_escaped() throws Exception {
        URI uri = new URN("non-urn", "ÄÜÖ").toURI();
        assertEquals("urn:non-urn:%c3%84%c3%9c%c3%96", uri.toASCIIString());
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

    @Test
    public void Non_reserved_encoded_characters_get_decoded() throws Exception {
        URN urn = new URN("urn:mix:%c3%84%2c");
        assertEquals("Expected `%2c` to be decoded into `,`", "Ä,", urn.getNamespaceSpecificString());
    }

    @Test
    public void Decodes_three_byte_UTF8_encoding() throws Exception {
        final URN urn = new URN("urn:foo:a123-%e0%a4%8b-456");
        assertEquals("Expected decoded `DEVANAGARI LETTER VOCALIC R`", "a123-\u090b-456", urn.getNamespaceSpecificString());
    }

    @Test(expected = URNSyntaxException.class)
    public void URN_String_containing_null_throws_exception() throws Exception {
        new URN("urn:foo:a123-\u0000-456-%2c");
    }

    @Test(expected = URNSyntaxException.class)
    public void Convenient_constructor_throws_exception_when_parsing_null() throws URNSyntaxException {
        final String nullString = null;
        new URN(nullString);
    }

    @Test(expected = URNSyntaxException.class)
    public void Convenient_constructor_throws_exception_when_parsing_empty_string() throws URNSyntaxException {
        new URN("");
    }

    @Test(expected = URNSyntaxException.class)
    public void Convenient_constructor_throws_exception_when_parsing_non_URN() throws URNSyntaxException {
        new URN("http://foo");
    }

    @Test(expected = URNSyntaxException.class)
    public void Convenient_constructor_throws_exception_when_parsing_misstructured_string() throws URNSyntaxException {
        new URN("urn:foo");
    }

    @Test
    public void Convenient_constructor_can_parse_URN_from_string() throws Exception {
        final URN urn = new URN("urn:isbn:0451450523");
        assertEquals("Wrong NID ", "isbn", urn.getNamespaceIdentifier());
        assertEquals("Wrong NSS", "0451450523", urn.getNamespaceSpecificString());
    }

    @Test
    public void Identical_URNs_have_the_same_hash_code() throws Exception {
        assertEquals("Hashcode of identical URNs should be equal",
                new URN("urn:foo:bar").hashCode(), new URN("urn:foo:bar").hashCode());
    }

    @Test
    public void Identical_URNs_are_equal() throws Exception {
        assertEquals("URNs should be equal",
                new URN("urn:foo:bar"), new URN("urn:foo:bar"));
    }

    @Test
    public void Lexical_unequivalent_URNs_are_not_equal() throws Exception {
        final String message = "Lexical unequivalent URNs should be not equal";
        assertNotEquals(message, new URN("urn:foo:a123%2C456"), new URN("URN:FOO:a123,456"));
        assertNotEquals(message, new URN("urn:foo:A123,456"), new URN("URN:FOO:a123,456"));
    }

    @Test
    public void Lexical_equivalent_URNs_are_equal() throws Exception {
        final String message = "Lexical equivalent URNs should be equal";
        assertEquals(message, new URN("URN:foo:a123,456"), new URN("urn:foo:a123,456"));
        assertEquals(message, new URN("URN:foo:a123,456"), new URN("urn:FOO:a123,456"));
        assertEquals(message, new URN("urn:foo:a123,456"), new URN("urn:FOO:a123,456"));
        assertEquals(message, new URN("urn:foo:a123%2C456"), new URN("URN:FOO:a123%2c456"));
    }

}
