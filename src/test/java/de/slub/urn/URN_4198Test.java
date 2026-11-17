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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class URN_4198Test {

    @Test
    public void Parses_RFC_example_with_year_DateId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A572007");
        assertEquals("example.com", fdc.namespaceSpecificString().providerId());
        assertEquals("2002", fdc.namespaceSpecificString().dateId());
        assertEquals("A572007", fdc.namespaceSpecificString().resourceId());
        assertEquals(LocalDate.of(2002, 1, 1), fdc.namespaceSpecificString().date());
    }

    @Test
    public void Parses_RFC_example_with_colon_in_ResourceId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.net:200406:ivr:51089");
        assertEquals("example.net", fdc.namespaceSpecificString().providerId());
        assertEquals("200406", fdc.namespaceSpecificString().dateId());
        assertEquals("ivr:51089", fdc.namespaceSpecificString().resourceId());
        assertEquals(LocalDate.of(2004, 6, 1), fdc.namespaceSpecificString().date());
    }

    @Test
    public void Parses_RFC_example_with_full_date_DateId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.org:20010527:img089322-038");
        assertEquals("example.org", fdc.namespaceSpecificString().providerId());
        assertEquals("20010527", fdc.namespaceSpecificString().dateId());
        assertEquals("img089322-038", fdc.namespaceSpecificString().resourceId());
        assertEquals(LocalDate.of(2001, 5, 27), fdc.namespaceSpecificString().date());
    }

    @Test
    public void Accepts_percent_encoded_octets_in_ResourceId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:20220902:%C3%A4FF");
        assertEquals("%C3%A4FF", fdc.namespaceSpecificString().resourceId());
    }

    @Test
    public void Accepts_binary_percent_encoded_octets_in_ResourceId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:20220902:%FFFF");
        assertEquals("%FFFF", fdc.namespaceSpecificString().resourceId());
    }

    @Test
    public void Round_trips_unchanged() throws URNSyntaxError {
        String literal = "urn:fdc:Example.com:20220902:%FFff%c3%A4";
        assertEquals(literal, URN.rfc4198().parse(literal).toString());
    }

    @Test
    public void Accepts_all_RFC_4198_other_characters_in_ResourceId() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:()+,-.:=@;$_!*'");
        assertEquals("()+,-.:=@;$_!*'", fdc.namespaceSpecificString().resourceId());
    }

    @Test
    public void Accepts_upper_case_NID() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:FDC:example.com:2002:A572007");
        assertEquals("example.com", fdc.namespaceSpecificString().providerId());
    }

    @Test
    public void Accepts_ProviderId_with_hyphens_digits_and_many_labels() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:a.0b-1.x-y.c0m:2002:A");
        assertEquals("a.0b-1.x-y.c0m", fdc.namespaceSpecificString().providerId());
    }

    @Test
    public void Accepts_single_character_labels() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:1.x:2002:A");
        assertEquals("1.x", fdc.namespaceSpecificString().providerId());
    }

    @Test
    public void Converts_URN_parsed_according_to_RFC_2141() throws URNSyntaxError {
        URN_4198 fdc = URN_4198.from(URN.rfc2141().parse("URN:FDC:Example.com:2002:A572007"));
        assertEquals("A572007", fdc.namespaceSpecificString().resourceId());
        assertEquals("urn:FDC:Example.com:2002:A572007", fdc.toString());
    }

    @Test
    public void Converts_URN_parsed_according_to_RFC_8141_keeping_RQF_components() throws URNSyntaxError {
        URN_4198 fdc = URN_4198.from(URN.rfc8141().parse("urn:fdc:example.com:2002:A572007?+r?=q#f"));
        assertEquals("urn:fdc:example.com:2002:A572007?+r?=q#f", fdc.toString());
        assertEquals("f", fdc.getRQFComponents().fragment().orElseThrow());
    }

    @Test
    public void Converting_a_Federated_Content_URN_returns_it() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A572007");
        assertSame(fdc, URN_4198.from(fdc));
    }

    @Test
    public void Parses_URI() throws Exception {
        URN_4198 fdc = URN.rfc4198().parse(new java.net.URI("urn:fdc:example.com:2002:A572007"));
        assertEquals("example.com", fdc.namespaceSpecificString().providerId());
    }

    @Test
    public void Keeps_RQF_components_of_underlying_URN() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A572007?=q#f");
        assertEquals("A572007", fdc.namespaceSpecificString().resourceId());
        assertEquals("urn:fdc:example.com:2002:A572007?=q#f", fdc.toString());
    }

    @Test
    public void Rejects_other_NID() throws URNSyntaxError {
        URN urn = URN.rfc8141().parse("urn:example:example.com:2002:A572007");
        URNSyntaxError e = assertThrows(URNSyntaxError.class, () -> URN_4198.from(urn));
        assertEquals(RFC.RFC_4198, e.getViolatedRfc());
        assertThrows(URNSyntaxError.class, () -> URN.rfc4198().parse("urn:example:example.com:2002:A572007"));
    }

    @Test
    public void Constructor_rejects_other_NID_and_null_arguments() throws URNSyntaxError {
        NSS_RFC4198 nss = NSS_RFC4198.of("example.com", "2002", "A");
        assertThrows(IllegalArgumentException.class,
                () -> new URN_4198(new NID_RFC8141("example"), nss, RQF_RFC8141.NULL));
        assertThrows(IllegalArgumentException.class, () -> new URN_4198(null, nss, RQF_RFC8141.NULL));
        assertThrows(IllegalArgumentException.class,
                () -> new URN_4198(new NID_RFC8141("fdc"), null, RQF_RFC8141.NULL));
        assertThrows(IllegalArgumentException.class, () -> new URN_4198(new NID_RFC8141("fdc"), nss, null));
    }

    @Test
    public void Rejects_literal_that_is_not_a_URN() {
        assertThrows(URNSyntaxError.class, () -> URN.rfc4198().parse("fdc:example.com:2002:A"));
    }

    @Test
    public void Rejects_null_URN() {
        assertThrows(IllegalArgumentException.class, () -> URN_4198.from(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "urn:fdc:example.com",
            "urn:fdc:example.com:2002",
            "urn:fdc:example.com:2002:",
    })
    public void Rejects_missing_fields(String literal) {
        assertThrows(URNSyntaxError.class, () -> URN.rfc4198().parse(literal));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1",
            "example.123",
            "example.1com",
            "com",
            "-example.com",
            "example-.com",
            "example.-com",
            "example.com-",
            "example..com",
            ".example.com",
            "example.com.",
            "exa_mple.com",
            "exa%2dmple.com",
    })
    public void Rejects_invalid_ProviderId(String providerId) {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> URN.rfc4198().parse("urn:fdc:" + providerId + ":2002:A"));
        assertEquals(RFC.RFC_4198, e.getViolatedRfc());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2",
            "20",
            "200",
            "20021",
            "2002011",
            "200201011",
            "200200",
            "200213",
            "20020100",
            "20020132",
            "20020230",
            "2002ab",
    })
    public void Rejects_invalid_DateId(String dateId) {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> URN.rfc4198().parse("urn:fdc:example.com:" + dateId + ":A"));
        assertEquals(RFC.RFC_4198, e.getViolatedRfc());
    }

    @Test
    public void Accepts_leap_day() throws URNSyntaxError {
        assertEquals(LocalDate.of(2004, 2, 29), URN.rfc4198().parse("urn:fdc:example.com:20040229:A").namespaceSpecificString().date());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a/b",
            "a~b",
            "a&b",
            "a%00b",
    })
    public void Rejects_invalid_ResourceId(String resourceId) {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> URN.rfc4198().parse("urn:fdc:example.com:2002:" + resourceId));
        assertEquals(RFC.RFC_4198, e.getViolatedRfc());
    }

    @Test
    public void Equal_literals_are_equal_and_have_same_hash_code() throws URNSyntaxError {
        URN_4198 fdc1 = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A572007?=q");
        URN_4198 fdc2 = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A572007?=q");
        assertEquals(fdc1, fdc2);
        assertEquals(fdc1.hashCode(), fdc2.hashCode());
    }

    @Test
    public void ProviderId_differing_in_case_is_not_equal_but_equivalent() throws URNSyntaxError {
        URN_4198 lower = URN.rfc4198().parse("urn:fdc:example.com:2002:A572007");
        URN_4198 upper = URN.rfc4198().parse("urn:FDC:Example.COM:2002:A572007");
        assertNotEquals(lower, upper);
        assertTrue(lower.isEquivalentTo(upper));
        assertTrue(upper.isEquivalentTo(lower));
        assertEquals("Example.COM", upper.namespaceSpecificString().providerId());
    }

    @Test
    public void ResourceId_differing_in_case_is_not_equivalent() throws URNSyntaxError {
        assertFalse(URN.rfc4198().parse("urn:fdc:example.com:2002:A572007")
                .isEquivalentTo(URN.rfc4198().parse("urn:fdc:example.com:2002:a572007")));
    }

    @Test
    public void Percent_encoded_hex_digits_differing_in_case_are_not_equal_but_equivalent() throws URNSyntaxError {
        URN_4198 lower = URN.rfc4198().parse("urn:fdc:example.com:2002:%ff");
        URN_4198 upper = URN.rfc4198().parse("urn:fdc:example.com:2002:%FF");
        assertNotEquals(lower, upper);
        assertTrue(lower.isEquivalentTo(upper));
    }

    @Test
    public void Different_DateId_forms_are_not_equivalent() throws URNSyntaxError {
        assertFalse(URN.rfc4198().parse("urn:fdc:example.com:2002:A")
                .isEquivalentTo(URN.rfc4198().parse("urn:fdc:example.com:200201:A")));
    }

    @Test
    public void RQF_components_matter_for_equality_but_not_for_equivalence() throws URNSyntaxError {
        URN_4198 plain = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        URN_4198 withRqf = URN.rfc4198().parse("urn:fdc:example.com:2002:A?+r#f");
        assertNotEquals(plain, withRqf);
        assertTrue(plain.isEquivalentTo(withRqf));
        assertTrue(withRqf.isEquivalentTo(plain));
    }

    @Test
    public void Assigned_name_has_no_RQF_components() throws URNSyntaxError {
        URN_4198 plain = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A");
        URN_4198 withRqf = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A?+r?=q#f");
        assertEquals("urn:fdc:Example.COM:2002:A", withRqf.assignedName().toString());
        assertEquals(plain, withRqf.assignedName());
        assertEquals(plain.hashCode(), withRqf.assignedName().hashCode());
        assertSame(plain, plain.assignedName());
    }

    @Test
    public void Is_not_equal_to_URN_of_another_type_with_same_literal() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        URN_8141 rfc8141 = URN.rfc8141().parse("urn:fdc:example.com:2002:A");
        assertNotEquals(fdc, rfc8141);
        assertNotEquals(rfc8141, fdc);
        assertNotEquals(fdc.namespaceSpecificString(), rfc8141.namespaceSpecificString());
        assertNotEquals(rfc8141.namespaceSpecificString(), fdc.namespaceSpecificString());
    }

    @Test
    public void Is_equivalent_to_URN_of_another_type_with_same_literal() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A");
        for (URN other : new URN[]{
                URN.rfc8141().parse("urn:fdc:Example.COM:2002:A"),
                URN.rfc2141().parse("urn:fdc:Example.COM:2002:A")}) {
            assertTrue(fdc.isEquivalentTo(other), other.getClass().getSimpleName());
            assertTrue(other.isEquivalentTo(fdc), other.getClass().getSimpleName());
        }
    }

    @Test
    public void ProviderId_rule_applies_to_URNs_of_other_types_when_asked_by_the_FDC_URN() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A");
        URN_8141 rfc8141 = URN.rfc8141().parse("urn:fdc:example.com:2002:A?=q");
        assertTrue(fdc.isEquivalentTo(rfc8141));
        // The generic URN does not know the rules of the fdc namespace
        assertFalse(rfc8141.isEquivalentTo(fdc));
        assertTrue(URN.areEquivalent(fdc, rfc8141));
        assertTrue(URN.areEquivalent(rfc8141, fdc));
    }

    @Test
    public void Is_not_equivalent_to_URN_of_another_namespace() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        assertFalse(fdc.isEquivalentTo(URN.rfc8141().parse("urn:example:example.com:2002:A")));
    }

    @Test
    public void Is_not_equivalent_to_URN_that_is_no_valid_FDC_URN() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        assertFalse(fdc.isEquivalentTo(URN.rfc8141().parse("urn:fdc:no-fields")));
        assertFalse(fdc.isEquivalentTo(URN.rfc8141().parse("urn:fdc:%2F%2f:2002:A")));
    }

    @Test
    public void Equivalence_applies_when_used_as_plain_URN() throws URNSyntaxError {
        URN lower = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        URN upper = URN.rfc4198().parse("urn:fdc:EXAMPLE.COM:2002:A");
        assertTrue(lower.isEquivalentTo(upper));
        assertEquals(lower.normalize(), upper.normalize());
        assertEquals(lower.normalize().hashCode(), upper.normalize().hashCode());
    }

    @Test
    public void Supports_RFC_4198_only() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.com:2002:A");
        assertEquals(RFC.RFC_4198, fdc.supportedRFC());
        assertTrue(fdc.supports(RFC.RFC_4198));
        assertFalse(fdc.supports(RFC.RFC_8141));
    }

    @Test
    public void Builds_URN_from_fields() throws URNSyntaxError {
        URN_4198 fdc = URN_4198.of("example.org", "20010527", "img089322-038");
        assertEquals("urn:fdc:example.org:20010527:img089322-038", fdc.toString());
        assertEquals(URN.rfc4198().parse("urn:fdc:example.org:20010527:img089322-038"), fdc);
    }

    @Test
    public void Builder_percent_encodes_ResourceId() throws URNSyntaxError {
        URN_4198 fdc = URN_4198.of("example.org", "2001", "a/b%c ä");
        assertEquals("a%2Fb%25c%20%C3%A4", fdc.namespaceSpecificString().resourceId());
        assertEquals("a/b%c ä", fdc.namespaceSpecificString().unencoded().split(":", 3)[2]);
    }

    @Test
    public void Builder_does_not_encode_invalid_ProviderId() {
        assertThrows(URNSyntaxError.class, () -> URN_4198.of("exa mple.org", "2001", "A"));
    }

    @Test
    public void Builder_rejects_invalid_DateId() {
        assertThrows(URNSyntaxError.class, () -> URN_4198.of("example.org", "200", "A"));
    }

    @Test
    public void Builder_rejects_empty_ResourceId() {
        assertThrows(URNSyntaxError.class, () -> URN_4198.of("example.org", "2001", ""));
    }

    @Test
    public void Builder_rejects_null_arguments() {
        assertThrows(IllegalArgumentException.class, () -> URN_4198.of(null, "2001", "A"));
        assertThrows(IllegalArgumentException.class, () -> URN_4198.of("example.org", null, "A"));
        assertThrows(IllegalArgumentException.class, () -> URN_4198.of("example.org", "2001", null));
    }

    @Test
    public void Syntax_error_references_RFC_4198() {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> URN.rfc4198().parse("urn:fdc:127.0.0.1:20220902:%FFFF"));
        assertTrue(e.toString().contains("https://tools.ietf.org/html/rfc4198"));
    }

    @Test
    public void Normal_form_has_lower_case_NID_and_ProviderId_and_upper_case_octets() throws URNSyntaxError {
        URN_4198 normalized = URN.rfc4198().parse("URN:FDC:Example.COM:2002:AbC%ff").normalize();
        assertEquals("urn:fdc:example.com:2002:AbC%FF", normalized.toString());
        assertEquals("example.com", normalized.namespaceSpecificString().providerId());
        assertEquals("AbC%FF", normalized.namespaceSpecificString().resourceId());
    }

    @Test
    public void Normal_form_is_equivalent_but_only_equal_if_already_normalized() throws URNSyntaxError {
        URN_4198 fdc = URN.rfc4198().parse("URN:FDC:Example.COM:2002:AbC%ff");
        assertTrue(fdc.isEquivalentTo(fdc.normalize()));
        assertNotEquals(fdc, fdc.normalize());
        assertEquals(fdc.normalize(), fdc.normalize().normalize());
    }

    @Test
    public void Normal_form_keeps_RQF_components_unchanged() throws URNSyntaxError {
        assertEquals("urn:fdc:example.com:2002:A?=Q%2c#F",
                URN.rfc4198().parse("urn:fdc:Example.COM:2002:A?=Q%2c#F").normalize().toString());
    }

    @Test
    public void Literal_round_trips_unchanged_including_RQF_components() throws URNSyntaxError {
        String literal = "URN:FDC:Example.COM:2002:AbC%ff?+r?=q#f";
        assertEquals("urn:FDC:Example.COM:2002:AbC%ff?+r?=q#f", URN.rfc4198().parse(literal).toString());
    }

    @Test
    public void NSS_is_equivalent_to_plain_NSS_with_same_literal() throws URNSyntaxError {
        NamespaceSpecificString fdc = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A").namespaceSpecificString();
        NamespaceSpecificString plain = URN.rfc8141().parse("urn:fdc:Example.COM:2002:A").namespaceSpecificString();
        assertNotEquals(fdc, plain);
        assertTrue(fdc.isEquivalentTo(plain));
        assertTrue(plain.isEquivalentTo(fdc));
    }

    @Test
    public void NSS_applies_ProviderId_rule_to_plain_NSS() throws URNSyntaxError {
        NamespaceSpecificString fdc = URN.rfc4198().parse("urn:fdc:Example.COM:2002:A").namespaceSpecificString();
        NamespaceSpecificString plain = URN.rfc8141().parse("urn:fdc:example.com:2002:A").namespaceSpecificString();
        assertTrue(fdc.isEquivalentTo(plain));
        assertFalse(plain.isEquivalentTo(fdc));
    }

    @Test
    public void NSS_can_be_constructed_from_URL_encoded_literal() throws URNSyntaxError {
        NSS_RFC4198 nss = new NSS_RFC4198("example.net:200406:ivr:51089", NamespaceSpecificString.Encoding.URL_ENCODED);
        assertEquals("ivr:51089", nss.resourceId());
        assertEquals(RFC.RFC_4198, nss.supportedRFC());
    }

    @Test
    public void NSS_rejects_characters_not_allowed_in_any_URN() {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> new NSS_RFC4198("example.net:2004:a b", NamespaceSpecificString.Encoding.URL_ENCODED));
        assertEquals(RFC.RFC_4198, e.getViolatedRfc());
    }

}
