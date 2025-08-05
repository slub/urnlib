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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

abstract class NamespaceIdentifierTest<T extends NamespaceIdentifier> {

    /**
     * Return instance of the {@code NamespaceIdentifier} implementation under test
     *
     * @param nid Namespace identifier literal
     * @return Instance for testing
     */
    abstract T newTestInstance(String nid) throws URNSyntaxError;

    @Test
    public void Supports_the_reported_RFC() throws URNSyntaxError {
        RFCSupport nid = newTestInstance("abc");
        assertTrue(nid.supports(nid.supportedRFC()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void Empty_namespace_identifier_throws_exception() throws URNSyntaxError {
        newTestInstance("");
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

    @Test(expected = IllegalArgumentException.class)
    public void Passing_null_string_to_constructor_throws_exception() throws URNSyntaxError {
        newTestInstance((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void Passing_null_object_to_constructor_throws_exception() {
        newTestInstance((T) null);
    }

    /**
     * Return instance of the {@code NamespaceIdentifier} implementation under test
     *
     * @param nid Namespace identifier
     * @return Instance for testing
     */
    abstract T newTestInstance(T nid);

    @Test
    public void Calling_toString_returns_namespace_identifier() throws URNSyntaxError {
        final String              expected = "isbn";
        final NamespaceIdentifier subject  = newTestInstance(expected);
        assertEquals(expected, subject.toString());
    }

    @Test(expected = URNSyntaxError.class)
    public void URN_as_namespace_identifier_throws_exception() throws URNSyntaxError {
        final String badNamespaceIdentifier = "urn";
        newTestInstance(badNamespaceIdentifier);
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_are_equal() throws URNSyntaxError {
        final String              nid  = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid);
        assertEquals(nid1, nid2);
    }

    @Test
    public void NamespaceIdentifiers_equality_check_is_case_insensitive() throws URNSyntaxError {
        final String              nid  = "A-Valid-Nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid.toUpperCase());
        final NamespaceIdentifier nid2 = newTestInstance(nid.toLowerCase());
        assertEquals(nid1, nid2);
    }

    @Test
    public void Copied_NamespaceIdentifier_is_equal_to_orignal() throws URNSyntaxError {
        final String              nid  = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance((T) nid1);
        assertEquals(nid1, nid2);
    }

    @Test
    public void Copied_NamespaceIdentifier_is_not_identical_to_original() throws URNSyntaxError {
        final String              nid  = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance((T) nid1);
        assertNotSame(nid1, nid2);
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_have_same_hash_code() throws URNSyntaxError {
        final String              nid  = "a-valid-nid";
        final NamespaceIdentifier nid1 = newTestInstance(nid);
        final NamespaceIdentifier nid2 = newTestInstance(nid);
        assertEquals(nid1.hashCode(), nid2.hashCode());
    }

    @Test(expected = URNSyntaxError.class)
    public void Identifier_must_not_be_longer_than_32_characters() throws URNSyntaxError {
        final String tooLong = "abcdefghijklmnopqrstuvwxyz-1234567";
        newTestInstance(tooLong);
    }

}
