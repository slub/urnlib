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

import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class URN_8141Test extends URNTest {

    @Override
    URN_8141 getSample(String str) throws URNSyntaxException {
        return URN.rfc8141().parse(str);
    }

    @Test
    public void URNs_are_equivalent_despite_different_RQF_components() throws URNSyntaxException {
        final String message = "RQF components should be ignored when comparing URNs";
        final LinkedHashSet<URN> equivalent = new LinkedHashSet<URN>(){{
            add(getSample("urn:example:a123,z456"));
            add(getSample("urn:example:a123,z456?+abc"));
            add(getSample("urn:example:a123,z456?=xyz"));
            add(getSample("urn:example:a123,z456#789"));
        }};
        for (URN urn1 : equivalent) {
            for (URN urn2 : equivalent) {
                assertEquals(message, urn1, urn2);
            }
        }
    }

    @Test
    public void URNs_with_different_NSS_path_parts_are_not_equivalent() throws URNSyntaxException {
        final String message = "URNs with different NSS should not be equivalent";
        final LinkedHashSet<URN> equivalent = new LinkedHashSet<URN>(){{
            add(getSample("urn:example:a123,z456"));
            add(getSample("urn:example:a123,z456/foo"));
            add(getSample("urn:example:a123,z456/bar"));
            add(getSample("urn:example:a123,z456/baz"));
        }};
        for (URN urn1 : equivalent) {
            for (URN urn2 : equivalent) {
                // avoid comparing objects with themselves
                if (urn1 != urn2) {
                    assertNotEquals(message, urn1, urn2);
                }
            }
        }
    }

}