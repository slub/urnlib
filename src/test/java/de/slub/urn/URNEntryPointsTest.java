/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static de.slub.urn.NamespaceSpecificString.Encoding.NOT_ENCODED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the static methods of {@link URN} that create URNs without choosing an RFC first.
 */
public class URNEntryPointsTest {

    @Test
    public void Parse_reads_literal_according_to_RFC_8141() throws URNSyntaxError {
        URN_8141 urn = URN.parse("urn:example:a/b?=q#f");
        assertEquals("urn:example:a/b?=q#f", urn.toString());
        assertEquals(RFC.RFC_8141, urn.supportedRFC());
        assertEquals(URN.rfc8141().parse("urn:example:a/b?=q#f"), urn);
    }

    @Test
    public void Parse_reads_URI() throws Exception {
        assertEquals(URN.parse("urn:example:a"), URN.parse(new URI("urn:example:a")));
    }

    @Test
    public void Parse_rejects_invalid_literal_with_checked_exception() {
        URNSyntaxError e = assertThrows(URNSyntaxError.class, () -> URN.parse("urn:example:a b"));
        assertEquals(RFC.RFC_8141, e.getViolatedRfc());
    }

    @Test
    public void Create_reads_literal_according_to_RFC_8141() throws URNSyntaxError {
        assertEquals(URN.parse("urn:example:a/b?=q#f"), URN.create("urn:example:a/b?=q#f"));
    }

    @Test
    public void Create_rejects_invalid_literal_with_unchecked_exception() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> URN.create("urn:urn:a"));
        assertInstanceOf(URNSyntaxError.class, e.getCause());
        assertEquals(e.getCause().getMessage(), e.getMessage());
    }

    @Test
    public void Create_and_parse_reject_null() {
        assertThrows(IllegalArgumentException.class, () -> URN.create(null));
        assertThrows(IllegalArgumentException.class, () -> URN.parse((String) null));
        assertThrows(IllegalArgumentException.class, () -> URN.parse((URI) null));
    }

    @Test
    public void Builds_URN_from_literals_of_its_parts() throws URNSyntaxError {
        URN_8141 urn = URN.of("Example", "a/b%2f");
        assertEquals("urn:Example:a/b%2f", urn.toString());
        assertEquals(URN.parse("urn:Example:a/b%2f"), urn);
    }

    @Test
    public void Building_from_literals_rejects_invalid_parts() {
        assertThrows(URNSyntaxError.class, () -> URN.of("urn", "a"));
        assertThrows(URNSyntaxError.class, () -> URN.of("example", "a b"));
        assertThrows(URNSyntaxError.class, () -> URN.of("example", "a?=q"));
        assertThrows(IllegalArgumentException.class, () -> URN.of(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> URN.of("example", (String) null));
    }

    @Test
    public void Builds_URN_from_its_parts() throws URNSyntaxError {
        URN_8141 urn = URN.of(new NID_RFC8141("example"), new NSS_RFC8141("a b", NOT_ENCODED));
        assertEquals("urn:example:a%20b", urn.toString());
        assertEquals(RQF_RFC8141.NULL, urn.getRQFComponents());
        assertThrows(IllegalArgumentException.class, () -> URN.of((NID_RFC8141) null, new NSS_RFC8141("a", NOT_ENCODED)));
    }

    @Test
    public void Components_have_the_type_of_their_RFC() throws URNSyntaxError {
        assertTrue(URN.create("urn:example:a").namespaceIdentifier().isFormal());
        NSS_RFC8141 nss8141 = URN.create("urn:example:a").namespaceSpecificString();
        NID_RFC2141 nid2141 = URN.rfc2141().parse("urn:example:a").namespaceIdentifier();
        NSS_RFC2141 nss2141 = URN.rfc2141().parse("urn:example:a").namespaceSpecificString();
        assertEquals("a", nss8141.toString());
        assertEquals("example", nid2141.toString());
        assertEquals("a", nss2141.toString());
    }

}
