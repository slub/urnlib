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

import static org.junit.Assert.*;

public class NamespaceSpecificStringTest {

    @Test(expected = IllegalArgumentException.class)
    public void Empty_namespace_specific_string_throws_exception() throws URNSyntaxException {
        final String empty = "";
        NamespaceSpecificString.fromEncoded(empty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Initializing_Namespace_specific_string_with_null_throws_exception() throws Exception {
        NamespaceSpecificString.fromEncoded(null);
    }

    @Test
    public void Calling_toString_returns_namespace_specific_string() throws URNSyntaxException {
        final String expected = "a-valid-nss";
        final NamespaceSpecificString subject = NamespaceSpecificString.fromEncoded(expected);
        assertEquals(expected, subject.toString());
    }

    @Test
    public void Identically_initialized_NamespaceSpecificStrings_are_equal() throws Exception {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = NamespaceSpecificString.fromEncoded(nss);
        final NamespaceSpecificString nss2 = NamespaceSpecificString.fromEncoded(nss);
        assertEquals(nss1, nss2);
    }

    @Test
    public void Copied_NamespaceSpecificString_is_equal_to_orignal() throws Exception {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = NamespaceSpecificString.fromEncoded(nss);
        final NamespaceSpecificString nss2 = new NamespaceSpecificString(nss1);
        assertEquals(nss1, nss2);
    }

    @Test
    public void Copied_NamespaceSpecificString_is_not_identical_to_original() throws Exception {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = NamespaceSpecificString.fromEncoded(nss);
        final NamespaceSpecificString nss2 = new NamespaceSpecificString(nss1);
        assertFalse(nss1 == nss2);
    }

    @Test
    public void Identically_initialized_NamespaceSpecificStrings_have_same_hash_code() throws Exception {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = NamespaceSpecificString.fromEncoded(nss);
        final NamespaceSpecificString nss2 = NamespaceSpecificString.fromEncoded(nss);
        assertEquals(nss1.hashCode(), nss2.hashCode());
    }

    @Test(expected = URNSyntaxException.class)
    public void Encoded_Namespace_specific_string_containing_null_throws_exception() throws Exception {
        NamespaceSpecificString.fromEncoded("there-is-null-\u0000");
    }

    @Test(expected = URNSyntaxException.class)
    public void Raw_Namespace_specific_string_containing_null_throws_exception() throws Exception {
        NamespaceSpecificString.fromRawString("there-is-null-\u0000");
    }

    @Test
    public void URN_encoded_namespace_specific_string_gets_decoded() throws Exception {
        final String encodedNss = "%C3%84%C3%9C%C3%96%5B%5D%26%3C%3E%5E%60%7B%7C%7D";
        final String decodedNss = "ÄÜÖ[]&<>^`{|}";
        final NamespaceSpecificString subject = NamespaceSpecificString.fromEncoded(encodedNss);
        assertEquals(decodedNss, subject.raw());
    }

    @Test
    public void Characters_which_are_not_reserved_but_encoded_get_decoded_regardless() throws Exception {
        final NamespaceSpecificString subject = NamespaceSpecificString.fromEncoded("%c3%84%2c");
        assertEquals("Expected `%2c` to be decoded into `,`", "Ä,", subject.raw());
    }

    @Test
    public void Decodes_three_byte_UTF8_encoding() throws Exception {
        final NamespaceSpecificString subject = NamespaceSpecificString.fromEncoded("a123-%e0%a4%8b-456");
        assertEquals("Expected decoded `DEVANAGARI LETTER VOCALIC R`", "a123-\u090b-456", subject.raw());
    }

    @Test
    public void Unencoded_NamespaceSpecificString_get_encoded() throws Exception {
        final String decodedNss = "ÄÜÖ[]&<>^`{|}";
        final String encodedNss = "%c3%84%c3%9c%c3%96%5b%5d%26%3c%3e%5e%60%7b%7c%7d";
        final NamespaceSpecificString subject = NamespaceSpecificString.fromRawString(decodedNss);
        assertEquals(encodedNss, subject.toString());
    }

    @Test
    public void Unescaped_whitespace_in_NamespaceSpecificString_throws_exception() throws Exception {
        // https://en.wikipedia.org/wiki/Whitespace_character
        String[] whitespaceStrings = new String[]{
                "\u0009", "\u000B", "\u000C", "\u0020", "\u0085", "\u00A0", "\u1680", "\u2000", "\u2001", "\u2002",
                "\u2003", "\u2004", "\u2005", "\u2006", "\u2007", "\u2008", "\u2009", "\u200A", "\u2028", "\u2029",
                "\u202f", "\u205f", "\u3000"
        };
        for (String s : whitespaceStrings) {
            try {
                NamespaceSpecificString.fromEncoded(s);
                fail(String.format("Expected %s to be thrown for character \\u%04d",
                        URNSyntaxException.class.getSimpleName(), (int) s.charAt(0)));
            } catch (URNSyntaxException ignored) {
            }
        }
    }

}
