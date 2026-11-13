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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParsingTest {

    @Test
    public void NSS_without_RQF_components_is_returned_whole() throws URNSyntaxError {
        assertArrayEquals(new String[]{"foo", ""},
                Parsing.splitNamespaceSpecificString("foo", "urn:ex:foo", RFC.RFC_8141));
    }

    @Test
    public void NSS_ends_at_r_component() throws URNSyntaxError {
        assertArrayEquals(new String[]{"foo", "?+a"},
                Parsing.splitNamespaceSpecificString("foo?+a", "urn:ex:foo?+a", RFC.RFC_8141));
    }

    @Test
    public void NSS_ends_at_f_component() throws URNSyntaxError {
        assertArrayEquals(new String[]{"foo", "#x"},
                Parsing.splitNamespaceSpecificString("foo#x", "urn:ex:foo#x", RFC.RFC_8141));
    }

    @Test
    public void Empty_NSS_before_RQF_components_throws_URNSyntaxError() {
        assertThrows(URNSyntaxError.class,
                () -> Parsing.splitNamespaceSpecificString("?=q", "urn:ex:?=q", RFC.RFC_8141));
    }

    @Test
    public void Null_URN_literal_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Parsing.requireURNLiteral(null));
    }

    @Test
    public void Null_URI_throws_IllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Parsing.literalOf(null));
    }
}
