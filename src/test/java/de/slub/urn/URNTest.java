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

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class URNTest {

    private static final String IRRELEVANT_NOT_EMPTY_STRING = "irrelevant";

    @Test(expected = URNSyntaxException.class)
    public void Empty_namespace_identifier_throws_exception() throws URNSyntaxException {
        URN.newInstance(null, IRRELEVANT_NOT_EMPTY_STRING);
    }

    @Test(expected = URNSyntaxException.class)
    public void Empty_namespace_specific_string_throws_exception() throws URNSyntaxException {
        URN.newInstance(IRRELEVANT_NOT_EMPTY_STRING, null);
    }

    @Test
    public void Returns_namespace_identifier() throws URNSyntaxException {
        final String namespaceIdentifier = "isbn";
        final String nid = URN.newInstance(namespaceIdentifier, IRRELEVANT_NOT_EMPTY_STRING).getNamespaceIdentifier();
        assertEquals(namespaceIdentifier, nid);
    }

    @Test
    public void Returns_namespace_specific_string() throws URNSyntaxException {
        final String namespaceSpecificString = "0451450523";
        final String nid = URN.newInstance(IRRELEVANT_NOT_EMPTY_STRING, namespaceSpecificString).getNamespaceSpecificString();
        assertEquals(namespaceSpecificString, nid);
    }

    @Test
    public void Generates_valid_URI() throws URISyntaxException, URNSyntaxException {
        final String asciiString = URN.newInstance("isbn", "0451450523").toURI().toASCIIString();
        assertEquals("urn:isbn:0451450523", asciiString);
    }

    @Test
    public void Reserved_chars_in_URI_are_escaped() throws Exception {
        final String asciiString = URN.newInstance("reserved", "%/?#,").toURI().toASCIIString();
        assertEquals("urn:reserved:%25%2f%3f%23,", asciiString);
    }

    @Test
    public void Non_URN_chars_in_URI_are_escaped() throws Exception {
        final String asciiString = URN.newInstance("non-urn", " []&<>^`{|}").toURI().toASCIIString();
        assertEquals("urn:non-urn:%20%5b%5d%26%3c%3e%5e%60%7b%7c%7d", asciiString);
    }

    @Test
    public void Special_UTF8_chars_are_escaped() throws Exception {
        final String asciiString = URN.newInstance("non-urn", "ÄÜÖ").toURI().toASCIIString();
        assertEquals("urn:non-urn:%c3%84%c3%9c%c3%96", asciiString);
    }

    @Test(expected = URNSyntaxException.class)
    public void Parsing_non_URN_URIs_throws_exception() throws Exception {
        URN.fromURI(new URI("http://foo"));
    }

    @Test(expected = URNSyntaxException.class)
    public void Invalid_NID_part_in_URI_throws_exception() throws Exception {
        URN.fromURI(new URI("urn:!?:1234"));
    }

    @Test
    public void URN_string_can_be_parsed_into_its_components() throws Exception {
        final String nid = "isbn";
        final String nss = "0451450523";
        final URN urn = URN.fromURI(new URI(String.format("urn:%s:%s", nid, nss)));
        assertEquals("Wrong NID ", nid, urn.getNamespaceIdentifier());
        assertEquals("Wrong NSS", nss, urn.getNamespaceSpecificString());
    }

    @Test(expected = URNSyntaxException.class)
    public void Non_URN_URI_throws_exception() throws Exception {
        URN.fromURI(new URI("urn:invalid-urn-part"));
    }

    @Test(expected = URNSyntaxException.class)
    public void URN_string_containing_null_throws_exception() throws Exception {
        URN.fromString("urn:foo:a123-\u0000-456-%2c");
    }

    @Test(expected = URNSyntaxException.class)
    public void String_containing_empty_URN_parts_throws_exception() throws Exception {
        URN.fromString("urn::");
    }

    @Test(expected = URNSyntaxException.class)
    public void Factory_method_fromString_throws_exception_when_parsing_null() throws URNSyntaxException {
        URN.fromString(null);
    }

    @Test(expected = URNSyntaxException.class)
    public void Factory_method_fromString_throws_exception_when_parsing_empty_string() throws URNSyntaxException {
        URN.fromString("");
    }

    @Test(expected = URNSyntaxException.class)
    public void Factory_method_fromString_throws_exception_when_parsing_non_URN() throws URNSyntaxException {
        URN.fromString("http://foo");
    }

    @Test(expected = URNSyntaxException.class)
    public void Factory_method_throws_exception_when_NSS_part_is_missing() throws URNSyntaxException {
        URN.fromString("urn:foo");
    }

    @Test
    public void Factory_method_fromString_can_parse_URN_from_string() throws Exception {
        String nid = "isbn";
        String nss = "0451450523";
        final URN urn = URN.fromString(String.format("urn:%s:%s", nid, nss));
        assertEquals("Wrong NID ", nid, urn.getNamespaceIdentifier());
        assertEquals("Wrong NSS", nss, urn.getNamespaceSpecificString());
    }

    @Test
    public void Equal_URNs_have_the_same_hash_code() throws Exception {
        assertEquals("Hashcode of identical URNs should be equal",
                URN.fromString("urn:foo:bar").hashCode(), URN.fromString("urn:foo:bar").hashCode());
    }

    @Test
    public void Lexical_unequivalent_URNs_dont_generate_identical_representations() throws Exception {
        final String message = "Lexical unequivalent URNs should be not equal";
        assertNotEquals(message, URN.fromString("urn:foo:a123%2C456"), URN.fromString("URN:FOO:a123,456"));
        assertNotEquals(message, URN.fromString("urn:foo:A123,456"), URN.fromString("URN:FOO:a123,456"));
    }

    @Test
    public void Lexical_equivalent_URNs_generate_identical_representations() throws Exception {
        final String message = "Lexical equivalent URNs should be equal";
        assertEquals(message, URN.fromString("urn:foo:bar"), URN.fromString("urn:foo:bar"));
        assertEquals(message, URN.fromString("URN:foo:a123,456"), URN.fromString("urn:foo:a123,456"));
        assertEquals(message, URN.fromString("URN:foo:a123,456"), URN.fromString("urn:FOO:a123,456"));
        assertEquals(message, URN.fromString("urn:foo:a123,456"), URN.fromString("urn:FOO:a123,456"));
        assertEquals(message, URN.fromString("urn:foo:a123%2C456"), URN.fromString("URN:FOO:a123%2c456"));
    }

    @Test
    public void Initializing_URN_using_another_URN_object_gives_equal_objects() throws Exception {
        final URN urn1 = URN.newInstance("test", "1224");
        final URN urn2 = URN.fromURN(urn1);
        assertEquals("Both URNs should be equal", urn1, urn2);
    }

    @Test
    public void Cloning_gives_equal_objects() throws Exception {
        final URN urn1 = URN.newInstance("test", "1224");
        final URN urn2 = (URN) urn1.clone();
        assertEquals("Both URNs should be equal", urn1, urn2);
    }

    @Test(expected = NullPointerException.class)
    public void Convenience_methods_create_throws_NullPointerException_on_null_argument() {
        URN.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Convenience_methods_create_throws_IllegalArgumentException_on_invalid_URN_literal() {
        URN.create("");
    }

    @Test
    public void Convenience_methods_create_return_URN() {
        String str = "urn:foo:bar";
        URN urn = URN.create(str);
        assertNotNull("Return value should not be null", urn);
        assertEquals("URN literal should be equal to input string", str, urn.toString());
    }


}
