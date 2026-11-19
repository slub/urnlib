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

import de.slub.urn.NamespaceSpecificString.Encoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.slub.urn.NamespaceSpecificString.Encoding.NOT_ENCODED;
import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static org.junit.jupiter.api.Assertions.*;

abstract class NamespaceSpecificStringTest {

    abstract NamespaceSpecificString newTestInstance(NamespaceSpecificString nss);

    abstract NamespaceSpecificString newTestInstance(String nss, Encoding encoding) throws URNSyntaxError;

    @Test
    public void Supports_the_reported_RFC() throws URNSyntaxError {
        RFCSupport nss = newTestInstance("abc", NOT_ENCODED);
        assertTrue(nss.supports(nss.supportedRFC()));
    }

    @Test
    public void Empty_namespace_specific_string_throws_exception() {
        final String empty = "";
        assertThrows(IllegalArgumentException.class, () -> newTestInstance(empty, URL_ENCODED));
    }

    @Test
    public void Initializing_Namespace_specific_string_with_null_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> newTestInstance(null, URL_ENCODED));
    }

    @Test
    public void Calling_toString_returns_namespace_specific_string() throws URNSyntaxError {
        final String expected = "a-valid-nss";
        final NamespaceSpecificString subject = newTestInstance(expected, URL_ENCODED);
        assertEquals(expected, subject.toString());
    }

    @Test
    public void Identically_initialized_NamespaceSpecificStrings_are_equal() throws URNSyntaxError {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = newTestInstance(nss, URL_ENCODED);
        final NamespaceSpecificString nss2 = newTestInstance(nss, URL_ENCODED);
        assertEquals(nss1, nss2);
        assertEquals(nss1.hashCode(), nss2.hashCode());
    }

    @Test
    public void Copied_NamespaceSpecificString_is_equal_to_original() throws URNSyntaxError {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = newTestInstance(nss, URL_ENCODED);
        final NamespaceSpecificString nss2 = newTestInstance(nss1);
        assertEquals(nss1, nss2);
        assertEquals(nss1.hashCode(), nss2.hashCode());
    }

    @Test
    public void Copied_NamespaceSpecificString_is_not_identical_to_original() throws URNSyntaxError {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = newTestInstance(nss, URL_ENCODED);
        final NamespaceSpecificString nss2 = newTestInstance(nss1);
        assertNotSame(nss1, nss2);
    }

    @Test
    public void Identically_initialized_NamespaceSpecificStrings_have_same_hash_code() throws URNSyntaxError {
        final String nss = "a-valid-nss";
        final NamespaceSpecificString nss1 = newTestInstance(nss, URL_ENCODED);
        final NamespaceSpecificString nss2 = newTestInstance(nss, URL_ENCODED);
        assertEquals(nss1.hashCode(), nss2.hashCode());
    }

    @Test
    public void Encoded_Namespace_specific_string_containing_null_throws_exception() {
        assertThrows(URNSyntaxError.class, () -> new NSS_RFC2141("there-is-null-\u0000", URL_ENCODED));
    }

    @Test
    public void Raw_Namespace_specific_string_containing_null_throws_exception() {
        assertThrows(URNSyntaxError.class, () -> new NSS_RFC2141("there-is-null-\u0000", NOT_ENCODED));
    }

    @Test
    public void URN_encoded_namespace_specific_string_gets_decoded() throws URNSyntaxError {
        final String encodedNss = "%C3%84%C3%9C%C3%96%5B%5D%26%3C%3E%5E%60%7B%7C%7D";
        final String decodedNss = "ÄÜÖ[]&<>^`{|}";
        final NamespaceSpecificString subject = newTestInstance(encodedNss, URL_ENCODED);
        assertEquals(decodedNss, subject.unencoded());
    }

    @Test
    public void Decodes_three_byte_UTF8_encoding() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("a123-%e0%a4%8b-456", URL_ENCODED);
        assertEquals("a123-\u090b-456", subject.unencoded(), "Expected decoded `DEVANAGARI LETTER VOCALIC R`");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a+b", "a/b", "/ab", "ab/", "100%", "%2f", "a?b", "a#b", "a b", "a:b", "a=b&c", "~-._",
            "\u00e4\u00f6\u00fc", "\u6f22\u5b57", "a\uD83D\uDE00b",
            "+/%?# \u00e4\u6f22\uD83D\uDE00"})
    public void Encoding_and_decoding_round_trips(String unencoded) throws URNSyntaxError {
        final NamespaceSpecificString encoded = newTestInstance(unencoded, NOT_ENCODED);
        assertEquals(unencoded, encoded.unencoded(), "Decoded NSS should equal original string");

        final NamespaceSpecificString reparsed = newTestInstance(encoded.toString(), URL_ENCODED);
        assertEquals(encoded, reparsed, "Re-parsed NSS should equal encoded NSS");
        assertEquals(encoded.hashCode(), reparsed.hashCode(), "Re-parsed NSS should have the same hash code");
        assertEquals(unencoded, reparsed.unencoded(), "Re-parsed NSS should decode to original string");
    }

    @Test
    public void Unencoded_NamespaceSpecificString_get_encoded() throws URNSyntaxError {
        final String decodedNss = "ÄÜÖ[]&<>^`{|}";
        final String encodedNss = "%C3%84%C3%9C%C3%96%5B%5D%26%3C%3E%5E%60%7B%7C%7D";
        final NamespaceSpecificString subject = newTestInstance(decodedNss, NOT_ENCODED);
        assertEquals(encodedNss, subject.toString());
    }

    @Test
    public void Unescaped_whitespace_in_NamespaceSpecificString_throws_exception() {
        // https://en.wikipedia.org/wiki/Whitespace_character
        String[] whitespaceStrings = new String[]{
                "\u0009", "\u000B", "\u000C", "\u0020", "\u0085", "\u00A0", "\u1680", "\u2000", "\u2001", "\u2002",
                "\u2003", "\u2004", "\u2005", "\u2006", "\u2007", "\u2008", "\u2009", "\u200A", "\u2028", "\u2029",
                "\u202f", "\u205f", "\u3000"
        };
        for (String s : whitespaceStrings) {
            try {
                newTestInstance(s, URL_ENCODED);
                fail(String.format("Expected %s to be thrown for character \\u%04d",
                        URNSyntaxError.class.getSimpleName(), (int) s.charAt(0)));
            } catch (URNSyntaxError ignored) {
            }
        }
    }

    @Test
    public void Characters_which_are_not_reserved_but_encoded_get_decoded_regardless() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("%c3%84%2c", URL_ENCODED);
        assertEquals("Ä,", subject.unencoded(), "Expected `%2c` to be decoded into `,`");
    }

    @Test
    public void Long_URL_encoded_namespace_specific_string_is_validated_without_stack_overflow() throws URNSyntaxError {
        final String nss = "a".repeat(100_000) + "%2c" + "b".repeat(100_000);
        assertEquals(nss, newTestInstance(nss, URL_ENCODED).toString());
    }

    @Test
    public void Long_invalid_URL_encoded_namespace_specific_string_throws_URNSyntaxError() {
        assertThrows(URNSyntaxError.class, () -> newTestInstance("a".repeat(100_000) + "<", URL_ENCODED));
    }

    @Test
    public void Plus_sign_is_not_decoded_as_space() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("a+b", URL_ENCODED);
        assertEquals("a+b", subject.unencoded());
        assertEquals(subject, newTestInstance(subject.unencoded(), NOT_ENCODED));
    }

    @ParameterizedTest
    @ValueSource(strings = {"%ff", "%FFFF", "a%c3", "%c3%28", "%e2%82", "%ed%a0%80"})
    public void Percent_encoded_octets_that_are_not_UTF8_are_accepted(String nss) throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance(nss, URL_ENCODED);
        assertEquals(nss, subject.toString());
        assertFalse(subject.isDecodable());
    }

    @Test
    public void Decoding_octets_that_are_not_UTF8_throws_IllegalStateException() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("a%ff", URL_ENCODED);
        assertThrows(IllegalStateException.class, subject::unencoded);
    }

    @Test
    public void Copy_of_NamespaceSpecificString_that_is_not_UTF8_is_not_decodable() throws URNSyntaxError {
        final NamespaceSpecificString copy = newTestInstance(newTestInstance("a%ff", URL_ENCODED));
        assertEquals("a%ff", copy.toString());
        assertFalse(copy.isDecodable());
    }

    @Test
    public void UTF8_encoded_NamespaceSpecificString_is_decodable() throws URNSyntaxError {
        assertTrue(newTestInstance("a%c3%84", URL_ENCODED).isDecodable());
        assertTrue(newTestInstance("\u00c4", NOT_ENCODED).isDecodable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"%FFFF", "%ffFF", "%Ff", "a%2Cb", "%C3%84", "%c3%84"})
    public void URL_encoded_literal_round_trips_unchanged(String nss) throws URNSyntaxError {
        assertEquals(nss, newTestInstance(nss, URL_ENCODED).toString());
    }

    @Test
    public void Percent_encoded_octets_differing_in_case_are_not_equal() throws URNSyntaxError {
        final NamespaceSpecificString lower = newTestInstance("FF%ff", URL_ENCODED);
        final NamespaceSpecificString upper = newTestInstance("FF%FF", URL_ENCODED);
        final NamespaceSpecificString mixed = newTestInstance("FF%fF", URL_ENCODED);
        assertNotEquals(lower, upper);
        assertNotEquals(lower, mixed);
        assertNotEquals(upper, mixed);
    }

    @Test
    public void Identical_URL_encoded_literals_are_equal_and_have_same_hash_code() throws URNSyntaxError {
        final NamespaceSpecificString nss1 = newTestInstance("FF%ff", URL_ENCODED);
        final NamespaceSpecificString nss2 = newTestInstance("FF%ff", URL_ENCODED);
        assertEquals(nss1, nss2);
        assertEquals(nss1.hashCode(), nss2.hashCode());
    }

    @Test
    public void Encoding_produces_the_same_literal_as_parsing_upper_case_octets() throws URNSyntaxError {
        final NamespaceSpecificString encoded = newTestInstance("\u00c4", NOT_ENCODED);
        final NamespaceSpecificString parsed = newTestInstance("%C3%84", URL_ENCODED);
        assertEquals(parsed, encoded);
        assertEquals(parsed.hashCode(), encoded.hashCode());
    }

    @Test
    public void NamespaceSpecificString_is_not_equal_to_null_or_other_types() throws URNSyntaxError {
        final NamespaceSpecificString nss = newTestInstance("abc", URL_ENCODED);
        assertNotEquals(null, nss);
        assertNotEquals("abc", nss);
    }

    @Test
    public void Literal_characters_are_compared_case_sensitively() throws URNSyntaxError {
        assertNotEquals(newTestInstance("ff%ff", URL_ENCODED), newTestInstance("FF%ff", URL_ENCODED));
    }

    @Test
    public void Percent_encoded_octets_are_not_decoded_for_comparison() throws URNSyntaxError {
        assertNotEquals(newTestInstance("a%2cb", URL_ENCODED), newTestInstance("a,b", URL_ENCODED));
    }

    @Test
    public void Truncated_percent_encoding_throws_URNSyntaxError() {
        for (String nss : new String[]{"%", "a%2", "%g0", "%2g"}) {
            assertThrows(URNSyntaxError.class, () -> newTestInstance(nss, URL_ENCODED), nss);
        }
    }

    @Test
    public void Supplementary_character_gets_encoded_as_four_UTF8_octets() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("a\uD83D\uDE00b", NOT_ENCODED);
        assertEquals("a%F0%9F%98%80b", subject.toString());
    }

    @Test
    public void Four_UTF8_octets_get_decoded_into_supplementary_character() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("a%F0%9F%98%80b", URL_ENCODED);
        assertEquals("a\uD83D\uDE00b", subject.unencoded());
        assertEquals(subject, newTestInstance(subject.unencoded(), NOT_ENCODED));
    }

    @Test
    public void Unpaired_surrogate_throws_URNSyntaxError() {
        assertThrows(URNSyntaxError.class, () -> newTestInstance("a\uD83Db", NOT_ENCODED));
        assertThrows(URNSyntaxError.class, () -> newTestInstance("a\uDE00", NOT_ENCODED));
    }

    @Test
    public void Normal_form_has_upper_case_percent_encoded_octets_only() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("abc%ff%c3%A4-Def%2c", URL_ENCODED);
        assertEquals("abc%FF%C3%A4-Def%2C", subject.normalize().toString());
    }

    @Test
    public void Normal_form_does_not_decode_percent_encoded_octets() throws URNSyntaxError {
        assertEquals("%41%2C", newTestInstance("%41%2c", URL_ENCODED).normalize().toString());
    }

    @Test
    public void Normal_form_is_equivalent_and_of_the_same_type() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("abc%ff", URL_ENCODED);
        assertTrue(subject.isEquivalentTo(subject.normalize()));
        assertSame(subject.getClass(), subject.normalize().getClass());
    }

    @Test
    public void Normal_form_is_only_equal_if_already_normalized() throws URNSyntaxError {
        assertNotEquals(newTestInstance("abc%ff", URL_ENCODED), newTestInstance("abc%ff", URL_ENCODED).normalize());
        assertEquals(newTestInstance("abc%FF", URL_ENCODED), newTestInstance("abc%FF", URL_ENCODED).normalize());
    }

    @Test
    public void Encoded_literal_is_already_in_normal_form() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("\u00c4/b", NOT_ENCODED);
        assertEquals(subject, subject.normalize());
    }

    @Test
    public void Percent_encoded_octets_differing_in_case_are_equivalent() throws URNSyntaxError {
        final NamespaceSpecificString lower = newTestInstance("FF%ff", URL_ENCODED);
        final NamespaceSpecificString upper = newTestInstance("FF%FF", URL_ENCODED);
        final NamespaceSpecificString mixed = newTestInstance("FF%fF", URL_ENCODED);
        assertTrue(lower.isEquivalentTo(upper));
        assertTrue(upper.isEquivalentTo(lower));
        assertTrue(lower.isEquivalentTo(mixed));
    }

    @Test
    public void Literal_characters_differing_in_case_are_not_equivalent() throws URNSyntaxError {
        assertFalse(newTestInstance("ff%ff", URL_ENCODED).isEquivalentTo(newTestInstance("FF%ff", URL_ENCODED)));
    }

    @Test
    public void Percent_encoded_octets_are_not_decoded_for_equivalence() throws URNSyntaxError {
        assertFalse(newTestInstance("a%2cb", URL_ENCODED).isEquivalentTo(newTestInstance("a,b", URL_ENCODED)));
    }

    @Test
    public void NamespaceSpecificString_is_not_equivalent_to_null() throws URNSyntaxError {
        assertFalse(newTestInstance("abc", URL_ENCODED).isEquivalentTo(null));
    }

    @Test
    public void Equivalence_works_across_types() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("abc%ff", URL_ENCODED);
        final NamespaceSpecificString rfc2141 = new NSS_RFC2141("abc%FF", URL_ENCODED);
        final NamespaceSpecificString rfc8141 = new NSS_RFC8141("abc%FF", URL_ENCODED);
        assertTrue(subject.isEquivalentTo(rfc2141));
        assertTrue(subject.isEquivalentTo(rfc8141));
        assertTrue(rfc2141.isEquivalentTo(subject));
        assertTrue(rfc8141.isEquivalentTo(subject));
        assertNotEquals(rfc2141, rfc8141);
    }

    @Test
    public void Normalizing_is_idempotent() throws URNSyntaxError {
        final NamespaceSpecificString normalized = newTestInstance("abc%ff", URL_ENCODED).normalize();
        assertEquals(normalized.toString(), normalized.normalize().toString());
    }

    @Test
    public void Normal_form_of_unencoded_string_can_still_be_decoded() throws URNSyntaxError {
        final NamespaceSpecificString subject = newTestInstance("\u00c4/b", NOT_ENCODED);
        assertEquals("%C3%84%2Fb", subject.normalize().toString());
        assertEquals("\u00c4/b", subject.normalize().unencoded());
    }

    @Test
    public void Copying_null_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> newTestInstance((NamespaceSpecificString) null));
    }

}
