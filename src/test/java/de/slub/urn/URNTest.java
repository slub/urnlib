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

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class URNTest {

    @Test
    final public void Returns_namespace_identifier() throws URNSyntaxError {
        URN urn = getSample("urn:isbn:irrelevant");
        NamespaceIdentifier nid = urn.namespaceIdentifier();
        assertEquals("isbn", nid.toString());
    }

    /**
     * Return a URN sample parsed from given string using the appropriate parser.
     * <p>
     * Tests implementing this method need to select an URN parser.
     *
     * @param urnLiteral URN string literal to be parsed into URN for further testing
     * @return URN created from given URN string literal
     * @throws URNSyntaxError if parsing fails
     */
    abstract URN getSample(String urnLiteral) throws URNSyntaxError;

    @Test
    final public void Supports_the_reported_RFC() throws URNSyntaxError {
        RFCSupport urn = getSample("urn:abc:123");
        assertTrue(urn.supports(urn.supportedRFC()));
    }

    @Test
    final public void Returns_namespace_specific_string() throws URNSyntaxError {
        URN urn = getSample("urn:foo:0451450523");
        NamespaceSpecificString nss = urn.namespaceSpecificString();
        assertEquals("0451450523", nss.toString());
    }

    @Test
    final public void Generates_valid_URI() throws URISyntaxException, URNSyntaxError {
        URN urn = getSample("urn:isbn:0451450523");
        String asciiString = urn.toURI().toASCIIString();
        assertEquals("urn:isbn:0451450523", asciiString);
    }

    @Test
    final public void Equal_URNs_have_the_same_hash_code() throws URNSyntaxError {
        assertEquals(
                getSample("urn:foo:bar").hashCode(),
                getSample("urn:foo:bar").hashCode(),
                "Hashcode of identical URNs should be equal");
    }

    @Test
    final public void URNs_differing_only_in_scheme_case_are_equal() throws URNSyntaxError {
        // The scheme is not a component of the URN and is always serialized in lower case
        final URN reference = getSample("urn:example:a123,z456");
        final URN urn = getSample("URN:example:a123,z456");
        assertEquals(reference.toString(), urn.toString());
        assertEquals(reference, urn);
        assertEquals(reference.hashCode(), urn.hashCode(), "Hashcode of equal URNs should be equal");
        assertTrue(new HashSet<>(List.of(reference)).contains(urn), "Equal URN should be found in HashSet");
    }

    @Test
    final public void URNs_differing_in_NID_case_are_not_equal() throws URNSyntaxError {
        final URN reference = getSample("urn:example:a123,z456");
        final URN urn = getSample("urn:EXAMPLE:a123,z456");
        assertNotEquals(reference, urn);
        assertFalse(new HashSet<>(List.of(reference)).contains(urn), "Different URN should not be found in HashSet");
    }

    @Test
    final public void URN_is_not_equal_to_null_or_other_types() throws URNSyntaxError {
        final URN urn = getSample("urn:foo:bar");
        assertNotEquals(null, urn);
        assertNotEquals("urn:foo:bar", urn);
    }

    @Test
    final public void Identical_URNs_generate_equivalent_representations() throws URNSyntaxError {
        final String message = "Identical URNs should be equal";
        final LinkedHashSet<String> equivalent = new LinkedHashSet<>() {{
            add("urn:foo:bar");
            add("URN:foo:a123,456");
            add("urn:foo:a123,456");
            add("urn:foo:a123%2C456");
        }};
        for (String urn : equivalent) {
            assertEquals(getSample(urn), getSample(urn), message);
            assertEquals(getSample(urn).hashCode(), getSample(urn).hashCode(), "Hashcode of equal URNs should be equal");
        }
    }

    @Test
    final public void URNs_with_percent_encoding_are_not_equal_to_URNs_without_encoding() throws URNSyntaxError {
        final String message = "URNs with percent encoding should not be equivalent to ones without encoding";
        assertNotEquals(
                getSample("urn:example:a123%2Cz456"),
                getSample("urn:example:a123,z456"),
                message);
        assertNotEquals(
                getSample("URN:EXAMPLE:a123%2cz456"),
                getSample("urn:example:a123,z456"),
                message);
    }

    @Test
    final public void URNs_with_percent_encoding_but_different_case_are_not_equal() throws URNSyntaxError {
        final URN upperCase = getSample("urn:foo:a123%2C456");
        final URN lowerCase = getSample("urn:foo:a123%2c456");
        assertNotEquals(upperCase, lowerCase, "Percent-encoded octets should be compared exactly");
    }

    @Test
    final public void URNs_with_different_NSS_case_are_not_equal_to_each_other() throws URNSyntaxError {
        final String message = "URNs with different case in NSS should not be equal";
        final LinkedHashSet<URN> equivalent = new LinkedHashSet<>() {{
            add(getSample("urn:example:a123,z456"));
            add(getSample("URN:example:a123,z456"));
            add(getSample("urn:EXAMPLE:a123,z456"));
        }};
        final LinkedHashSet<URN> notEquivalent = new LinkedHashSet<>() {{
            add(getSample("urn:example:A123,z456"));
            add(getSample("urn:example:a123,Z456"));
        }};
        for (URN urn1 : notEquivalent) {
            for (URN urn2 : equivalent) {
                assertNotEquals(urn1, urn2, message);
            }
        }
    }

    @Test
    final public void URN_with_cyrillic_letter_is_not_equal_to_URN_with_latin_letter() throws URNSyntaxError {
        final String message = "URNs with cyrillic letter should not be equal to URN with latin letter";
        final LinkedHashSet<URN> equivalent = new LinkedHashSet<>() {{
            add(getSample("urn:example:a123,z456"));
            add(getSample("URN:example:a123,z456"));
            add(getSample("urn:EXAMPLE:a123,z456"));
        }};
        URN cyrillicLetterA = getSample("urn:example:%D0%B0123,z456");
        for (URN urn1 : equivalent) {
            assertNotEquals(urn1, cyrillicLetterA, message);
        }
    }

    @Test
    public void URN_with_percent_encoded_octets_round_trips_unchanged() throws URNSyntaxError {
        final String literal = "urn:example:hash-%FFFF%c3%A4";
        assertEquals(literal, getSample(literal).toString());
    }

    @Test
    public void Normal_form_has_lower_case_NID_and_upper_case_percent_encoded_octets() throws URNSyntaxError {
        assertEquals("urn:example:AbC%FF%2C", getSample("URN:Example:AbC%ff%2c").normalize().toString());
    }

    @Test
    public void Normal_form_is_equivalent_and_of_the_same_type() throws URNSyntaxError {
        final URN subject = getSample("URN:Example:AbC%ff%2c");
        assertTrue(subject.isEquivalentTo(subject.normalize()));
        assertSame(subject.getClass(), subject.normalize().getClass());
    }

    @Test
    public void Normal_form_is_only_equal_if_already_normalized() throws URNSyntaxError {
        assertNotEquals(getSample("urn:Example:AbC%ff"), getSample("urn:Example:AbC%ff").normalize());
        assertEquals(getSample("urn:example:AbC%FF"), getSample("urn:example:AbC%FF").normalize());
    }

    @Test
    public void Normalizing_is_idempotent() throws URNSyntaxError {
        final URN normalized = getSample("URN:Example:AbC%ff%2c").normalize();
        assertEquals(normalized, normalized.normalize());
    }

    @Test
    public void Equivalent_URNs_have_equal_normal_forms() throws URNSyntaxError {
        final URN urn1 = getSample("URN:FOO:a123%2c456").normalize();
        final URN urn2 = getSample("urn:foo:a123%2C456").normalize();
        assertEquals(urn1, urn2);
        assertEquals(urn1.hashCode(), urn2.hashCode());
        assertEquals(urn1.toString(), urn2.toString());
    }

    @Test
    public void Different_URNs_have_different_normal_forms() throws URNSyntaxError {
        assertNotEquals(getSample("urn:foo:A123").normalize(), getSample("urn:foo:a123").normalize());
    }

    @Test
    public void URNs_differing_in_scheme_or_NID_case_are_equivalent() throws URNSyntaxError {
        // RFC 8141, 3.2, first example
        final List<URN> equivalent = List.of(
                getSample("urn:example:a123,z456"),
                getSample("URN:example:a123,z456"),
                getSample("urn:EXAMPLE:a123,z456"));
        for (URN urn1 : equivalent) {
            for (URN urn2 : equivalent) {
                assertTrue(urn1.isEquivalentTo(urn2), urn1 + " vs. " + urn2);
                assertTrue(URN.areEquivalent(urn1, urn2), urn1 + " vs. " + urn2);
            }
        }
    }

    @Test
    public void URNs_with_percent_encoding_but_different_case_are_equivalent() throws URNSyntaxError {
        // RFC 8141, 3.2, fourth example
        final URN upperCase = getSample("urn:example:a123%2Cz456");
        final URN lowerCase = getSample("URN:EXAMPLE:a123%2cz456");
        assertTrue(upperCase.isEquivalentTo(lowerCase));
        assertTrue(lowerCase.isEquivalentTo(upperCase));
    }

    @Test
    public void URNs_with_percent_encoding_are_not_equivalent_to_URNs_without_encoding() throws URNSyntaxError {
        // RFC 8141, 3.2, fourth example
        assertFalse(getSample("urn:example:a123%2Cz456").isEquivalentTo(getSample("urn:example:a123,z456")));
    }

    @Test
    public void URNs_with_different_NSS_case_are_not_equivalent() throws URNSyntaxError {
        // RFC 8141, 3.2, fifth example
        final URN reference = getSample("urn:example:a123,z456");
        assertFalse(reference.isEquivalentTo(getSample("urn:example:A123,z456")));
        assertFalse(reference.isEquivalentTo(getSample("urn:example:a123,Z456")));
    }

    @Test
    public void URN_with_cyrillic_letter_is_not_equivalent_to_URN_with_latin_letter() throws URNSyntaxError {
        // RFC 8141, 3.2, sixth example
        assertFalse(getSample("urn:example:a123,z456").isEquivalentTo(getSample("urn:example:%D0%B0123,z456")));
    }

    @Test
    public void URNs_with_different_NID_are_not_equivalent() throws URNSyntaxError {
        assertFalse(getSample("urn:foo:a123").isEquivalentTo(getSample("urn:bar:a123")));
    }

    @Test
    public void URN_is_not_equivalent_to_null() throws URNSyntaxError {
        final URN urn = getSample("urn:foo:bar");
        assertFalse(urn.isEquivalentTo(null));
        assertFalse(URN.areEquivalent(urn, null));
        assertFalse(URN.areEquivalent(null, urn));
        assertFalse(URN.areEquivalent(null, null));
    }

    @Test
    public void URN_is_equivalent_to_URN_of_other_type_with_same_string() throws URNSyntaxError {
        final URN subject = getSample("urn:Example:AbC%ff");
        for (URN other : List.of(
                URN.rfc2141().parse("urn:Example:AbC%ff"),
                URN.rfc8141().parse("urn:Example:AbC%ff"),
                URN.rfc2141().parse("urn:example:AbC%FF"),
                URN.rfc8141().parse("urn:example:AbC%FF"))) {
            assertTrue(subject.isEquivalentTo(other), other.getClass().getSimpleName() + " " + other);
            assertTrue(other.isEquivalentTo(subject), other.getClass().getSimpleName() + " " + other);
            assertTrue(URN.areEquivalent(subject, other), other.getClass().getSimpleName() + " " + other);
        }
    }

    @Test
    public void Assigned_name_of_URN_without_further_components_is_equal() throws URNSyntaxError {
        final URN subject = getSample("urn:Example:AbC%ff");
        assertEquals(subject, subject.assignedName());
        assertSame(subject.getClass(), subject.assignedName().getClass());
    }

    @Test
    public void Assigned_name_is_not_normalized() throws URNSyntaxError {
        assertEquals("urn:Example:AbC%ff", getSample("urn:Example:AbC%ff").assignedName().toString());
    }

    @Test
    public void Normalized_assigned_name_serves_as_key_for_equivalent_URNs() throws URNSyntaxError {
        final HashSet<URN> keys = new HashSet<>();
        keys.add(getSample("urn:Example:a123%2c456").normalize().assignedName());
        assertTrue(keys.contains(getSample("URN:EXAMPLE:a123%2C456").normalize().assignedName()));
        assertFalse(keys.contains(getSample("urn:example:A123%2C456").normalize().assignedName()));
    }

}
