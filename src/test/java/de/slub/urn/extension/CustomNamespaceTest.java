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

import de.slub.urn.Namespace;
import de.slub.urn.NamespaceIdentifier;
import de.slub.urn.NamespaceSpecificString;
import de.slub.urn.NamespaceURN;
import de.slub.urn.RFC;
import de.slub.urn.Specification;
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxError;
import de.slub.urn.URN_8141;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks that a third party can define a namespace from outside of the library's package, using only public API, and
 * that a namespace cannot make the library produce an invalid URN.
 */
public class CustomNamespaceTest {

    static final Specification SPECIFICATION = new Specification() {
        @Override
        public String name() {
            return "Ticket Specification";
        }

        @Override
        public String url() {
            return "https://example.org/tickets";
        }
    };

    /**
     * Tickets are numbered and belong to a case-insensitive queue: {@code urn:ticket:<queue>-<number>}. The private
     * namespace {@code urn:x-ticket} is accepted as well.
     */
    static class TicketNamespace implements Namespace<Integer> {
        @Override
        public boolean accepts(NamespaceIdentifier nid) {
            final String lower = nid.toString().toLowerCase(Locale.ROOT);
            return lower.equals("ticket") || lower.equals("x-ticket");
        }

        @Override
        public Integer read(NamespaceSpecificString nss) throws URNSyntaxError {
            if (!nss.toString().matches("[A-Za-z]+-[0-9]+")) {
                throw URNSyntaxError.syntaxError(SPECIFICATION, "Not a ticket: " + nss);
            }
            return Integer.valueOf(nss.toString().split("-")[1]);
        }

        @Override
        public String normalForm(String rfcNormalForm) {
            return rfcNormalForm.toLowerCase(Locale.ROOT);
        }

        @Override
        public Specification specification() {
            return SPECIFICATION;
        }
    }

    static final Namespace<Integer> TICKETS = new TicketNamespace();

    /**
     * A namespace that violates the contract of {@link Namespace#normalForm(String)} in a given way.
     */
    static Namespace<Integer> ticketsWithNormalForm(UnaryOperator<String> normalForm) {
        return new TicketNamespace() {
            @Override
            public String normalForm(String rfcNormalForm) {
                return normalForm.apply(rfcNormalForm);
            }
        };
    }

    /**
     * A namespace that accepts every Namespace Specific String and keeps the given equivalence rules.
     */
    static Namespace<Integer> anythingWithNormalForm(UnaryOperator<String> normalForm) {
        return new TicketNamespace() {
            @Override
            public Integer read(NamespaceSpecificString nss) {
                return 0;
            }

            @Override
            public String normalForm(String rfcNormalForm) {
                return normalForm.apply(rfcNormalForm);
            }
        };
    }

    @Test
    public void Parses_URN_of_custom_namespace() throws URNSyntaxError {
        NamespaceURN<Integer> ticket = URN.in(TICKETS).parse("urn:ticket:Support-42?=open#top");
        assertEquals(42, ticket.name());
        assertEquals("urn:ticket:Support-42?=open#top", ticket.toString());
        assertEquals("ticket", ticket.namespaceIdentifier().toString());
        assertEquals("Support-42", ticket.namespaceSpecificString().toString());
        assertEquals("top", ticket.getRQFComponents().fragment().orElseThrow());
        assertSame(TICKETS, ticket.namespace());
        assertEquals(RFC.RFC_8141, ticket.supportedRFC());
    }

    @Test
    public void Namespace_decides_on_Namespace_Identifier() throws URNSyntaxError {
        assertEquals(7, URN.in(TICKETS).parse("urn:X-Ticket:a-7").name());
        URNSyntaxError e = assertThrows(URNSyntaxError.class, () -> URN.in(TICKETS).parse("urn:example:a-7"));
        assertEquals(SPECIFICATION, e.getViolatedSpecification());
        assertNull(e.getViolatedRfc());
    }

    @Test
    public void Namespace_decides_on_Namespace_Specific_String() {
        URNSyntaxError e = assertThrows(URNSyntaxError.class, () -> URN.in(TICKETS).parse("urn:ticket:42"));
        assertEquals(SPECIFICATION, e.getViolatedSpecification());
        assertTrue(e.toString().contains("https://example.org/tickets"), e.toString());
    }

    @Test
    public void Namespace_is_not_asked_about_URN_that_violates_the_syntax() {
        URNSyntaxError e = assertThrows(URNSyntaxError.class,
                () -> URN.in(anythingWithNormalForm(s -> s)).parse("urn:ticket:not valid"));
        assertEquals(RFC.RFC_8141, e.getViolatedRfc());
    }

    @Test
    public void URN_of_namespace_is_a_URN() throws URNSyntaxError {
        URN urn = URN.in(TICKETS).parse("urn:ticket:Support-42");
        assertEquals("urn:ticket:Support-42", urn.toString());
        assertEquals("urn:ticket:support-42", urn.normalize().toString());
    }

    @Test
    public void Converts_URN_of_any_type() throws URNSyntaxError {
        assertEquals(42, URN.create("urn:ticket:a-42").as(TICKETS).name());
        assertEquals(42, URN.rfc2141().parse("urn:ticket:a-42").as(TICKETS).name());
        NamespaceURN<Integer> ticket = URN.create("urn:ticket:a-42").as(TICKETS);
        assertSame(ticket, ticket.as(TICKETS));
        assertThrows(URNSyntaxError.class, () -> URN.create("urn:ticket:a").as(TICKETS));
    }

    @Test
    public void Converts_URN_of_one_namespace_into_another() throws URNSyntaxError {
        Namespace<Integer> other = new TicketNamespace();
        NamespaceURN<Integer> ticket = URN.in(TICKETS).parse("urn:ticket:a-42?=q");
        NamespaceURN<Integer> converted = ticket.as(other);
        assertNotSame(ticket, converted);
        assertSame(other, converted.namespace());
        assertEquals(ticket.toString(), converted.toString());
    }

    @Test
    public void Is_equal_to_URN_of_same_namespace_with_same_components_only() throws URNSyntaxError {
        NamespaceURN<Integer> ticket = URN.in(TICKETS).parse("urn:ticket:a-42?=q");
        assertEquals(ticket, URN.in(TICKETS).parse("urn:ticket:a-42?=q"));
        assertEquals(ticket.hashCode(), URN.in(TICKETS).parse("urn:ticket:a-42?=q").hashCode());
        assertNotEquals(ticket, URN.in(TICKETS).parse("urn:ticket:A-42?=q"));
        assertNotEquals(ticket, URN.in(TICKETS).parse("urn:ticket:a-42"));
        assertNotEquals(ticket, URN.in(new TicketNamespace()).parse("urn:ticket:a-42?=q"));
        URN_8141 generic = URN.create("urn:ticket:a-42?=q");
        assertNotEquals(ticket, generic);
        assertNotEquals(generic, ticket);
        assertEquals(generic, ticket.urn());
    }

    @Test
    public void Applies_equivalence_rules_of_namespace() throws URNSyntaxError {
        NamespaceURN<Integer> upper = URN.in(TICKETS).parse("urn:ticket:SUPPORT-42");
        NamespaceURN<Integer> lower = URN.in(TICKETS).parse("urn:TICKET:support-42#f");
        assertNotEquals(upper, lower);
        assertTrue(upper.isEquivalentTo(lower));
        assertTrue(lower.isEquivalentTo(upper));
        assertEquals(upper.normalize().assignedName(), lower.normalize().assignedName());
        assertFalse(upper.isEquivalentTo(URN.in(TICKETS).parse("urn:ticket:SUPPORT-43")));
        assertFalse(upper.isEquivalentTo(null));
    }

    @Test
    public void Generic_URN_does_not_know_equivalence_rules_of_namespace() throws URNSyntaxError {
        NamespaceURN<Integer> ticket = URN.in(TICKETS).parse("urn:ticket:SUPPORT-42");
        URN generic = URN.create("urn:ticket:support-42");
        assertTrue(ticket.isEquivalentTo(generic));
        assertFalse(generic.isEquivalentTo(ticket));
        assertTrue(URN.areEquivalent(ticket, generic));
        assertTrue(URN.areEquivalent(generic, ticket));
        // What is equivalent by the syntax is equivalent for both
        assertTrue(generic.isEquivalentTo(URN.in(TICKETS).parse("urn:TICKET:support-42")));
    }

    @Test
    public void Namespace_cannot_separate_URNs_that_are_equivalent_by_the_syntax() throws URNSyntaxError {
        // The namespace gets to see the normal form of the syntax only, so the case of hex digits is out of its reach
        Namespace<Integer> namespace = anythingWithNormalForm(s -> s);
        NamespaceURN<Integer> lower = URN.in(namespace).parse("urn:ticket:a%ff");
        NamespaceURN<Integer> upper = URN.in(namespace).parse("urn:ticket:a%FF");
        assertTrue(lower.isEquivalentTo(upper));
        assertTrue(upper.isEquivalentTo(lower));
    }

    @Test
    public void Assigned_name_keeps_namespace_and_name() throws URNSyntaxError {
        NamespaceURN<Integer> ticket = URN.in(TICKETS).parse("urn:ticket:a-42?+r?=q#f");
        NamespaceURN<Integer> assignedName = ticket.assignedName();
        assertEquals("urn:ticket:a-42", assignedName.toString());
        assertEquals(42, assignedName.name());
        assertSame(TICKETS, assignedName.namespace());
        assertSame(assignedName, assignedName.assignedName());
    }

    @Test
    public void Normal_form_of_namespace_that_is_no_valid_NSS_does_not_yield_a_URN() throws URNSyntaxError {
        for (String invalid : new String[]{"not valid", "a?b", "", "/a-1", null}) {
            NamespaceURN<Integer> urn = URN.in(anythingWithNormalForm(s -> invalid)).parse("urn:ticket:a-42");
            assertThrows(IllegalStateException.class, urn::normalize, String.valueOf(invalid));
        }
    }

    @Test
    public void Normal_form_of_namespace_that_violates_the_namespace_does_not_yield_a_URN() throws URNSyntaxError {
        NamespaceURN<Integer> ticket = URN.in(ticketsWithNormalForm(s -> "no-ticket")).parse("urn:ticket:a-42");
        assertThrows(IllegalStateException.class, ticket::normalize);
    }

    @Test
    public void Normal_form_of_namespace_that_is_not_normal_by_the_syntax_does_not_yield_a_URN()
            throws URNSyntaxError {
        NamespaceURN<Integer> urn = URN.in(anythingWithNormalForm(s -> s.toLowerCase(Locale.ROOT)))
                .parse("urn:ticket:a%FF");
        assertThrows(IllegalStateException.class, urn::normalize);
    }

    @Test
    public void Namespace_that_returns_no_name_does_not_yield_a_URN() {
        Namespace<Integer> noName = new TicketNamespace() {
            @Override
            public Integer read(NamespaceSpecificString nss) {
                return null;
            }
        };
        assertThrows(IllegalStateException.class, () -> URN.in(noName).parse("urn:ticket:a-42"));
    }

}
