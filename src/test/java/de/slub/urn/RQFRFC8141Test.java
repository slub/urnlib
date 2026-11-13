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

import static org.junit.jupiter.api.Assertions.*;

public class RQFRFC8141Test {

    @Test
    public void NULL_object_is_empty() {
        RQF_RFC8141 n = RQF_RFC8141.NULL;
        assertTrue(n.resolutionParameters().isEmpty());
        assertTrue(n.queryParameters().isEmpty());
        assertTrue(n.fragment().isEmpty());
    }

    @Test
    public void NULL_object_is_equal_to_itself() {
        assertEquals(RQF_RFC8141.NULL, RQF_RFC8141.NULL);
    }

    @Test
    public void NULL_objects_have_equal_hash_codes() {
        assertEquals(RQF_RFC8141.NULL.hashCode(), RQF_RFC8141.NULL.hashCode());
    }

    @Test
    public void NULL_object_is_not_equal_to_null() {
        assertNotEquals(RQF_RFC8141.NULL, null);
    }

    @Test
    public void Passing_null_as_resolutionComponent_throws_exception() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> new RQF_RFC8141(null, RQComponent.EMPTY, ""));
        assertEquals("Resolution component cannot be null", e.getMessage());
    }

    @Test
    public void Passing_null_as_queryComponent_throws_exception() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> new RQF_RFC8141(RQComponent.EMPTY, null, ""));
        assertEquals("Query component cannot be null", e.getMessage());
    }

    @Test
    public void Passing_null_as_fragment_throws_exception() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> new RQF_RFC8141(RQComponent.EMPTY, RQComponent.EMPTY, null));
        assertEquals("Fragment string cannot be null", e.getMessage());
    }

    @Test
    public void ToString_With_Parameters_And_Fragment() throws URNSyntaxError {
        RQComponent resolutionComponent = RQComponent.builder()
                .add("resolutionParameter1", "foo1")
                .add("resolutionParameter0", "foo0")
                .build();
        RQComponent queryComponent = RQComponent.builder()
                .add("queryParameter0", "bar0")
                .add("queryParameters1", "bar1")
                .build();
        String fragment = "fragment0";
        RQF_RFC8141 urn = new RQF_RFC8141(resolutionComponent, queryComponent, fragment);
        String expected = "?+resolutionParameter1=foo1&resolutionParameter0=foo0?=queryParameter0=bar0&queryParameters1=bar1#fragment0";

        String actual = urn.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void ToString_Without_Parameter_And_Fragment() throws URNSyntaxError {
        RQF_RFC8141 urn = new RQF_RFC8141(RQComponent.EMPTY, RQComponent.EMPTY, "");
        String expected = "";

        String actual = urn.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void Equal_instances_have_equal_hash_codes() throws URNSyntaxError {
        RQF_RFC8141 r1 = new RQF_RFC8141(RQComponent.parse("a=1&b=2"), RQComponent.parse("q"), "f");
        RQF_RFC8141 r2 = RQF_RFC8141.parse("?+a=1&b=2?=q#f");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void Parameter_order_is_significant_for_equality() throws URNSyntaxError {
        assertNotEquals(RQF_RFC8141.parse("?=a=1&b=2"), RQF_RFC8141.parse("?=b=2&a=1"));
    }

    @Test
    public void Parsing_and_serializing_round_trips() throws URNSyntaxError {
        for (String rqf : new String[]{
                "?+a=1", "?=a=1", "#f", "?+r?=q#f", "?=zeta=1&alpha=2&mid=3&bb=4&aa=5", "?=a=b=c",
                "?=a=1&a=2", "?=bar", "?=bar=", "?=a&&b&", "?=k=v?+r=1", "#frag?=k=v", "?+a?b/c?=d/e?f#g/h?i"}) {
            assertEquals(rqf, RQF_RFC8141.parse(rqf).toString());
        }
    }

    @Test
    public void Fragment_starts_at_first_number_sign() throws URNSyntaxError {
        RQF_RFC8141 rqf = RQF_RFC8141.parse("#frag?=k=v");
        assertEquals("frag?=k=v", rqf.fragment());
        assertTrue(rqf.queryComponent().isEmpty());
    }

    @Test
    public void R_component_ends_at_first_q_component_delimiter() throws URNSyntaxError {
        RQF_RFC8141 rqf = RQF_RFC8141.parse("?+a=?+?=b?+c");
        assertEquals("a=?+", rqf.resolutionComponent().toString());
        assertEquals("b?+c", rqf.queryComponent().toString());
    }

    @Test
    public void R_component_containing_q_component_delimiter_cannot_be_constructed() throws URNSyntaxError {
        RQComponent r = RQComponent.builder().add("a", "x?=y").build();
        assertThrows(URNSyntaxError.class, () -> new RQF_RFC8141(r, RQComponent.EMPTY, ""));
    }

    @Test
    public void Invalid_characters_in_fragment_throw_URNSyntaxError() {
        for (String fragment : new String[]{"f g", "f#g", "a<b", "%zz"}) {
            assertThrows(URNSyntaxError.class,
                    () -> new RQF_RFC8141(RQComponent.EMPTY, RQComponent.EMPTY, fragment), fragment);
        }
    }

    @Test
    public void Bare_question_mark_after_NSS_throws_URNSyntaxError() {
        assertThrows(URNSyntaxError.class, () -> RQF_RFC8141.parse("?x"));
    }

}
