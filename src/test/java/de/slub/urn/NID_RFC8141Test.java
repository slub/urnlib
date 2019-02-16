/*
 * Copyright (C) 2018 Saxon State and University Library Dresden (SLUB)
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

import static de.slub.urn.RFC.RFC_8141;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NID_RFC8141Test extends NamespaceIdentifierTest<NID_RFC8141> {

    @Test
    public void Supports_RFC_2141() throws URNSyntaxError {
        NamespaceIdentifier nid = newTestInstance("foo");
        assertEquals(RFC_8141, nid.supportedRFC());
    }

    @Override
    NID_RFC8141 newTestInstance(String nid) throws URNSyntaxError {
        return new NID_RFC8141(nid);
    }

    @Override
    NID_RFC8141 newTestInstance(NID_RFC8141 nid) {
        return new NID_RFC8141(nid);
    }

    @Test(expected = URNSyntaxError.class)
    public void Does_not_allow_single_letter_namespace_identifier() throws URNSyntaxError {
        // While RFC 2141 allows single letter NIDs, RFC 8141 syntax rules do not
        newTestInstance("A");
    }

    @Test
    public void Identifier_starting_with_alpha_alpha_hyphen_qualifies_not_formal() throws URNSyntaxError {
        final String informal = "ab-a";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertFalse(nid.isFormal());
    }

    @Test
    public void Identifier_starting_with_alpha_alpha_hyphen_hyphen_qualifies_not_formal() throws URNSyntaxError {
        final String informal = "ab--a";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertFalse(nid.isFormal());
    }

    @Test
    public void Identifier_starting_with_X_hyphen_qualifies_not_formal() throws URNSyntaxError {
        final String informal = "X-a";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertFalse(nid.isFormal());
    }

    @Test
    public void Identifier_may_contain_X_hyphen() throws URNSyntaxError {
        final String okNamespaceIdentifier = "does-not-start-with-X";
        newTestInstance(okNamespaceIdentifier);
    }

    @Test
    public void Identifier_starting_with_urn_hyphen_qualifies_informal() throws URNSyntaxError {
        final String informal = "urn-1";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertTrue(nid.isInformal());
    }

    @Test
    public void Identifier_starting_with_urn_hyphen_qualifies_not_formal() throws URNSyntaxError {
        final String informal = "urn-1";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertFalse(nid.isFormal());
    }

    @Test
    public void Identifier_may_be_both_not_formal_and_not_informal() throws URNSyntaxError {
        final String informal = "xn--a";
        NID_RFC8141  nid      = newTestInstance(informal);
        assertFalse(nid.isFormal());
        assertFalse(nid.isInformal());
    }

}