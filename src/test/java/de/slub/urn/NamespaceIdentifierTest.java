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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NamespaceIdentifierTest {

    @Test(expected = URNSyntaxException.class)
    public void Empty_namespace_identifier_throws_exception() throws URNSyntaxException {
        final String empty = "";
        new NID_RFC2141(empty);
    }

    @Test(expected = URNSyntaxException.class)
    public void Passing_null_to_constructor_throws_exception() throws URNSyntaxException {
        new NID_RFC2141((String) null);
    }

    @Test
    public void Calling_toString_returns_namespace_identifier() throws URNSyntaxException {
        final String expected = "isbn";
        final NamespaceIdentifier subject = new NID_RFC2141(expected);
        assertEquals(expected, subject.toString());
    }

    @Test
    public void Allows_single_letter_namespace_identifier() throws URNSyntaxException {
        new NID_RFC2141("A");
    }

    @Test(expected = URNSyntaxException.class)
    public void URN_as_namespace_identifier_throws_exception() throws URNSyntaxException {
        final String badNamespaceIdentifier = "urn";
        new NID_RFC2141(badNamespaceIdentifier);
    }

    @Test(expected = URNSyntaxException.class)
    public void Invalid_char_in_namespace_identifier_throw_exception() throws URNSyntaxException {
        final String badNamespaceIdentifier = "-is!bn";
        new NID_RFC2141(badNamespaceIdentifier);
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_are_equal() throws Exception {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = new NID_RFC2141(nid);
        final NamespaceIdentifier nid2 = new NID_RFC2141(nid);
        assertEquals(nid1, nid2);
    }

    @Test
    public void NamespaceIdentifiers_equality_check_is_case_insensitive() throws Exception {
        final String nid = "A-Valid-Nid";
        final NamespaceIdentifier nid1 = new NID_RFC2141(nid.toUpperCase());
        final NamespaceIdentifier nid2 = new NID_RFC2141(nid.toLowerCase());
        assertEquals(nid1, nid2);
    }

    @Test
    public void Copied_NamespaceIdentifier_is_equal_to_orignal() throws Exception {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = new NID_RFC2141(nid);
        final NamespaceIdentifier nid2 = new NID_RFC2141(nid1);
        assertEquals(nid1, nid2);
    }

    @Test
    public void Copied_NamespaceIdentifier_is_not_identical_to_original() throws Exception {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = new NID_RFC2141(nid);
        final NamespaceIdentifier nid2 = new NID_RFC2141(nid1);
        assertFalse(nid1 == nid2);
    }

    @Test
    public void Identically_initialized_NamespaceIdentifiers_have_same_hash_code() throws Exception {
        final String nid = "a-valid-nid";
        final NamespaceIdentifier nid1 = new NID_RFC2141(nid);
        final NamespaceIdentifier nid2 = new NID_RFC2141(nid);
        assertEquals(nid1.hashCode(), nid2.hashCode());
    }

}
