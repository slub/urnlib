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


import de.slub.urn.NamespaceSpecificString.Encoding;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NSS_RFC8141Test extends NamespaceSpecificStringTest {

    @Test
    public void Returns_supported_RFC_8141() throws URNSyntaxException {
        NamespaceSpecificString nss = newTestInstance("abc", Encoding.NOT_ENCODED);
        assertEquals(RFC.RFC_8141, nss.supportedRFC());
    }

    @Test
    public void Slashes_are_allowed() throws URNSyntaxException {
        String nss = "1/406/47452/2";
        newTestInstance(nss, Encoding.URL_ENCODED);
    }

    @Test
    public void Questionmark_chracter_is_allowed() throws URNSyntaxException {
        String nss = "1?q";
        newTestInstance(nss, Encoding.URL_ENCODED);
    }

    @Test
    public void Tilde_character_is_allowed() throws URNSyntaxException {
        String nss = "1~q";
        newTestInstance(nss, Encoding.URL_ENCODED);
    }

    @Test
    public void Ampersand_character_is_allowed() throws URNSyntaxException {
        String nss = "1&q";
        newTestInstance(nss, Encoding.URL_ENCODED);
    }

    @Test
    public void At_sign_character_is_allowed() throws URNSyntaxException {
        String nss = "1@q";
        newTestInstance(nss, Encoding.URL_ENCODED);
    }

    @Override
    NamespaceSpecificString newTestInstance(String nss, Encoding encoding) throws URNSyntaxException {
        return new NSS_RFC8141(nss, encoding);
    }

    @Override
    NamespaceSpecificString newTestInstance(NamespaceSpecificString nss) {
        return new NSS_RFC8141(nss);
    }
}