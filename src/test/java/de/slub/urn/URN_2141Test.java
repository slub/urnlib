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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class URN_2141Test extends URNTest {

    @Test
    public void Returns_supported_RFC_2141() throws URNSyntaxError {
        URN urn = getSample("urn:foo:bar");
        assertEquals(RFC.RFC_2141, urn.supportedRFC());
    }

    @Override
    URN_2141 getSample(String urnLiteral) throws URNSyntaxError {
        return URN.rfc2141().parse(urnLiteral);
    }

    @Test
    public void Raises_exception_on_null_arguments() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new URN_2141(null, null));
    }

}