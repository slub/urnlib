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
package de.slub.urn.extension;

import de.slub.urn.NamespaceIdentifier;
import de.slub.urn.NamespaceSpecificString;
import de.slub.urn.RFC;
import de.slub.urn.URN;
import de.slub.urn.URNParser;
import de.slub.urn.URNSyntaxError;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Locale;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that the library can be extended with a URN type of its own from outside of the library's package, using
 * only public and protected API.
 */
public class ThirdPartyExtensionTest {

    static final class CustomNID extends NamespaceIdentifier {
        CustomNID(String nid) throws URNSyntaxError {
            super(nid);
        }

        @Override
        protected String validateNamespaceIdentifier(String nid) {
            return nid.matches("[A-Za-z]+") ? null : "Only letters are allowed";
        }

        @Override
        public CustomNID normalize() {
            try {
                return new CustomNID(normalizedLiteral());
            } catch (URNSyntaxError e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public RFC supportedRFC() {
            return RFC.RFC_2141;
        }
    }

    static final class CustomNSS extends NamespaceSpecificString {
        CustomNSS(String nss, Encoding encoding) throws URNSyntaxError {
            super(nss, encoding);
        }

        @Override
        protected boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
            return encoded.matches("([A-Za-z0-9.:]|%[0-9A-Fa-f]{2})+");
        }

        // This namespace is case-insensitive
        @Override
        protected String normalForm(String encoded) {
            return super.normalForm(encoded.toLowerCase(Locale.ROOT));
        }

        @Override
        public CustomNSS normalize() {
            try {
                return new CustomNSS(normalizedLiteral(), URL_ENCODED);
            } catch (URNSyntaxError e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public RFC supportedRFC() {
            return RFC.RFC_2141;
        }
    }

    static final class CustomURN extends URN {
        private final CustomNID nid;
        private final CustomNSS nss;

        CustomURN(CustomNID nid, CustomNSS nss) {
            this.nid = nid;
            this.nss = nss;
        }

        @Override
        public NamespaceIdentifier namespaceIdentifier() {
            return nid;
        }

        @Override
        public NamespaceSpecificString namespaceSpecificString() {
            return nss;
        }

        @Override
        public CustomURN normalize() {
            return new CustomURN(nid.normalize(), nss.normalize());
        }

        @Override
        public CustomURN assignedName() {
            return this;
        }

        @Override
        public RFC supportedRFC() {
            return RFC.RFC_2141;
        }

        @Override
        public String toString() {
            return SCHEME + ":" + nid + ":" + nss;
        }
    }

    static final class CustomParser implements URNParser<CustomURN> {
        @Override
        public CustomURN parse(String str) throws URNSyntaxError {
            final String[] parts = str.split(":", 3);
            return new CustomURN(new CustomNID(parts[1]), new CustomNSS(parts[2], URL_ENCODED));
        }

        @Override
        public CustomURN parse(URI uri) throws URNSyntaxError {
            return parse(uri.toString());
        }
    }

    @Test
    public void Custom_URN_type_can_be_normalized() throws URNSyntaxError {
        CustomURN urn = new CustomParser().parse("urn:Custom:Abc%ff");
        CustomURN normalized = urn.normalize();
        assertEquals("urn:custom:abc%FF", normalized.toString());
        assertEquals("urn:Custom:Abc%ff", urn.toString());
        assertTrue(urn.isEquivalentTo(normalized));
    }

    @Test
    public void Custom_URN_type_inherits_component_wise_equality() throws URNSyntaxError {
        CustomURN urn1 = new CustomParser().parse("urn:custom:abc");
        CustomURN urn2 = new CustomParser().parse("urn:custom:abc");
        assertEquals(urn1, urn2);
        assertEquals(urn1.hashCode(), urn2.hashCode());
        assertNotEquals(urn1, new CustomParser().parse("urn:custom:ABC"));
    }

    @Test
    public void Custom_URN_type_never_equals_library_URN() throws URNSyntaxError {
        URN library = URN.rfc2141().parse("urn:custom:abc");
        CustomURN custom = new CustomParser().parse("urn:custom:abc");
        assertEquals(library.toString(), custom.toString());
        assertNotEquals(library, custom);
        assertNotEquals(custom, library);
        assertNotEquals(library.namespaceIdentifier(), custom.namespaceIdentifier());
        assertNotEquals(library.namespaceSpecificString(), custom.namespaceSpecificString());
    }

    @Test
    public void Custom_URN_type_is_equivalent_to_library_URN_with_same_string() throws URNSyntaxError {
        URN library = URN.rfc8141().parse("urn:custom:abc");
        CustomURN custom = new CustomParser().parse("urn:custom:abc");
        assertTrue(custom.isEquivalentTo(library));
        assertTrue(library.isEquivalentTo(custom));
    }

    @Test
    public void Custom_NSS_type_can_define_its_own_equivalence_rules() throws URNSyntaxError {
        CustomURN upper = new CustomParser().parse("urn:custom:ABC%ff");
        CustomURN lower = new CustomParser().parse("urn:custom:abc%FF");
        assertNotEquals(upper, lower);
        assertTrue(upper.isEquivalentTo(lower));
        assertTrue(lower.isEquivalentTo(upper));
        assertEquals(upper.normalize(), lower.normalize());
        assertEquals(upper.normalize().hashCode(), lower.normalize().hashCode());
    }

    @Test
    public void Custom_equivalence_rules_apply_to_library_URNs_when_asked_by_the_custom_type() throws URNSyntaxError {
        CustomURN custom = new CustomParser().parse("urn:custom:abc");
        URN library = URN.rfc8141().parse("urn:custom:ABC");
        // The custom type knows that the namespace is case-insensitive, the generic type does not
        assertTrue(custom.isEquivalentTo(library));
        assertFalse(library.isEquivalentTo(custom));
        assertTrue(URN.areEquivalent(custom, library));
        assertTrue(URN.areEquivalent(library, custom));
    }

}
