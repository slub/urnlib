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

import static org.junit.jupiter.api.Assertions.*;

abstract class NamespaceIdentifierTest<T extends NamespaceIdentifier> {

    /**
     * Return instance of the {@code NamespaceIdentifier} implementation under test
     *
     * @param nid Namespace identifier literal
     * @return Instance for testing
     */
    abstract T newTestInstance(String nid) throws URNSyntaxError;

    /**
     * Return instance of the {@code NamespaceIdentifier} implementation under test
     *
     * @param nid Namespace identifier
     * @return Instance for testing
     */
    abstract T newTestInstance(NamespaceIdentifier nid);

    @Test
    public void Supports_the_reported_RFC() throws URNSyntaxError {
        RFCSupport nid = newTestInstance("abc");
        assertTrue(nid.supports(nid.supportedRFC()));
    }

    @Test
    public void Empty_namespace_identifier_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> newTestInstance(""));
    }

    @Test
    public void Too_long_namespace_identifier_throws_exception() {
        try {
            newTestInstance("abcdefghijklmnopqrstuvwxyzABCDEFG");
            fail("Expected exception" + URNSyntaxError.class.getName());
        } catch (URNSyntaxError e) {
            assertTrue(e.getMessage().contains("too long"));
        }
    }

    @Test
    public void Passing_null_string_to_constructor_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> newTestInstance((String) null));
    }

    @Test
    public void Passing_null_object_to_constructor_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> newTestInstance((T) null));

    }

    @Test
    public void Calling_toString_returns_namespace_identifier() throws URNSyntaxError {
        final String expected = "isbn";
        final NamespaceIdentifier subject = newTestInstance(expected);
        assertEquals(expected, subject.toString());
    }

    @Test
    public void URN_as_namespace_identifier_throws_exception() {
        final String badNamespaceIdentifier = "urn";
        assertThrows(URNSyntaxError.class, () -> newTestInstance(badNamespaceIdentifier));
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_are_equal() throws URNSyntaxError {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid);
        assertEquals(nid1, nid2);
        assertEquals(nid1.hashCode(), nid2.hashCode());
    }

    @Test
    public void NamespaceIdentifiers_differing_in_case_are_not_equal() throws URNSyntaxError {
        final String nid = "A-Valid-Nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid.toUpperCase());
        final NamespaceIdentifier nid2 = newTestInstance(nid.toLowerCase());
        assertNotEquals(nid1, nid2);
    }

    @Test
    public void Copied_NamespaceIdentifier_is_equal_to_original() throws URNSyntaxError {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid1);
        assertEquals(nid1, nid2);
        assertEquals(nid1.hashCode(), nid2.hashCode());
    }

    @Test
    public void Copied_NamespaceIdentifier_is_not_identical_to_original() throws URNSyntaxError {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid1);
        assertNotSame(nid1, nid2);
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_have_same_hash_code() throws URNSyntaxError {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid);
        assertEquals(nid1.hashCode(), nid2.hashCode());
    }

    @Test
    public void Identifier_must_not_be_longer_than_32_characters() {
        final String tooLong = "abcdefghijklmnopqrstuvwxyz-1234567";
        assertThrows(URNSyntaxError.class, () -> newTestInstance(tooLong));
    }

    @Test
    public void NamespaceIdentifier_is_not_equal_to_null_or_other_types() throws URNSyntaxError {
        final NamespaceIdentifier nid = newTestInstance("a-valid-nid");
        assertNotEquals(null, nid);
        assertNotEquals("a-valid-nid", nid);
    }

    @Test
    public void Normal_form_is_lower_case() throws URNSyntaxError {
        final NamespaceIdentifier subject = newTestInstance("A-Valid-NID");
        assertEquals("a-valid-nid", subject.normalize().toString());
    }

    @Test
    public void Normal_form_is_equivalent_and_of_the_same_type() throws URNSyntaxError {
        final NamespaceIdentifier subject = newTestInstance("A-Valid-NID");
        assertTrue(subject.isEquivalentTo(subject.normalize()));
        assertSame(subject.getClass(), subject.normalize().getClass());
    }

    @Test
    public void Normal_form_is_only_equal_if_already_normalized() throws URNSyntaxError {
        assertNotEquals(newTestInstance("A-Valid-NID"), newTestInstance("A-Valid-NID").normalize());
        assertEquals(newTestInstance("a-valid-nid"), newTestInstance("a-valid-nid").normalize());
    }

    @Test
    public void NamespaceIdentifiers_differing_in_case_are_equivalent() throws URNSyntaxError {
        final NamespaceIdentifier upper = newTestInstance("A-VALID-NID");
        final NamespaceIdentifier lower = newTestInstance("a-valid-nid");
        assertTrue(upper.isEquivalentTo(lower));
        assertTrue(lower.isEquivalentTo(upper));
    }

    @Test
    public void Different_NamespaceIdentifiers_are_not_equivalent() throws URNSyntaxError {
        assertFalse(newTestInstance("a-valid-nid").isEquivalentTo(newTestInstance("another-nid")));
    }

    @Test
    public void NamespaceIdentifier_is_not_equivalent_to_null() throws URNSyntaxError {
        assertFalse(newTestInstance("a-valid-nid").isEquivalentTo(null));
    }

}
