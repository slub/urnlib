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

package de.slub;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class URNTest {

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_identifier_throws_exception() throws URNSyntaxException {
        new URN(null, "0451450523");
    }

    @Test(expected = URNSyntaxException.class)
    public void empty_namespace_specific_string_throws_exception() throws URNSyntaxException {
        new URN("isbn", null);
    }

    @Test
    public void returns_namespace_identifier() throws URNSyntaxException {
        final String nid = new URN("isbn", "0451450523").getNamespaceIdentifier();
        assertEquals("isbn", nid);
    }

    @Test(expected = URNSyntaxException.class)
    public void namespace_identifier_can_not_be_urn() throws URNSyntaxException {
        new URN("urn", "test");
    }

    @Test
    public void returns_namespace_specific_string() throws URNSyntaxException {
        final String nid = new URN("isbn", "0451450523").getNamespaceSpecificString();
        assertEquals("0451450523", nid);
    }

    @Test
    public void toURI() throws URISyntaxException, URNSyntaxException {
        URI uri = new URN("isbn", "0451450523").toURI();
        assertEquals("urn:isbn:0451450523", uri.toASCIIString());
    }
}