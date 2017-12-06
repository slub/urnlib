/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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

import de.slub.urn.NamespaceSpecificString.Encoding;
import org.junit.Test;

public class NSS_RFC21421Test extends NamespaceSpecificStringTest {

    @Test(expected = URNSyntaxException.class)
    public void Initializing_with_illegal_character_throws_exception() throws URNSyntaxException {
        String illegalNss = "ill/eg&al";
        newTestInstance(illegalNss, Encoding.URL_ENCODED);
    }

    @Override
    NamespaceSpecificString newTestInstance(String nss, Encoding encoding) throws URNSyntaxException {
        return new NSS_RFC2141(nss, encoding);
    }

    @Override
    NamespaceSpecificString newTestInstance(NamespaceSpecificString nss) {
        return new NSS_RFC2141(nss);
    }
}
