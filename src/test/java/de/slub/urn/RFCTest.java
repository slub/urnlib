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

import static de.slub.urn.RFC.RFC_2141;
import static de.slub.urn.RFC.RFC_8141;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RFCTest {

    @Test
    public void RFC_2141_equals_itself() {
        assertEquals(RFC_2141, RFC_2141);
    }

    @Test
    public void RFC_2141_is_not_equal_to_RFC8141() {
        assertNotEquals(RFC_2141, RFC_8141);
    }

    @Test
    public void RFC_2141_URL_is_specification_reference() {
        assertEquals("https://tools.ietf.org/html/rfc2141", RFC_2141.url());
    }

    @Test
    public void RFC_8141_URL_is_specification_reference() {
        assertEquals("https://tools.ietf.org/html/rfc8141", RFC_8141.url());
    }

}