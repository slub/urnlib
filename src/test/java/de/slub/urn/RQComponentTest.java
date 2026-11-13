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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RQComponentTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a", "a=1", "a=", "=a", "a=b=c", "a=1&a=2", "a&&b", "a&", "&", "CCResolve:cc=uk",
            "op=map&lat=39.56&lon=-104.85&datetime=1969-07-21T02:56:15Z", "s=/", "q=a?b", "a%20b=%26"})
    public void Parsing_and_serializing_round_trips(String component) throws URNSyntaxError {
        assertEquals(component, RQComponent.parse(component).toString());
    }

    @Test
    public void Empty_string_parses_to_EMPTY() throws URNSyntaxError {
        assertSame(RQComponent.EMPTY, RQComponent.parse(""));
        assertTrue(RQComponent.EMPTY.isEmpty());
        assertEquals("", RQComponent.EMPTY.toString());
    }

    @Test
    public void Value_is_split_at_first_equals_sign() throws URNSyntaxError {
        RQComponent c = RQComponent.parse("a=b=c");
        assertEquals(Optional.of("b=c"), c.firstValue("a"));
    }

    @Test
    public void Parameter_without_equals_sign_has_no_value() throws URNSyntaxError {
        RQComponent.Parameter p = RQComponent.parse("bar").parameters().get(0);
        assertEquals("bar", p.name());
        assertEquals(Optional.empty(), p.value());
    }

    @Test
    public void Parameter_with_trailing_equals_sign_has_empty_value() throws URNSyntaxError {
        RQComponent.Parameter p = RQComponent.parse("bar=").parameters().get(0);
        assertEquals("bar", p.name());
        assertEquals(Optional.of(""), p.value());
    }

    @Test
    public void Missing_and_empty_value_are_different() throws URNSyntaxError {
        assertNotEquals(RQComponent.parse("bar"), RQComponent.parse("bar="));
    }

    @Test
    public void Duplicate_names_keep_all_values_in_order() throws URNSyntaxError {
        RQComponent c = RQComponent.parse("a=1&b=x&a=2&a");
        assertEquals(List.of("1", "2"), c.values("a"));
        assertEquals(Optional.of("1"), c.firstValue("a"));
        assertEquals(Set.of("a", "b"), c.names());
        assertEquals(4, c.parameters().size());
    }

    @Test
    public void Parameter_order_is_kept() throws URNSyntaxError {
        RQComponent c = RQComponent.parse("zeta=1&alpha=2&mid=3");
        assertEquals(List.of("zeta", "alpha", "mid"), List.copyOf(c.names()));
    }

    @Test
    public void Map_view_uses_first_value_and_empty_string_for_missing_values() throws URNSyntaxError {
        Map<String, String> map = RQComponent.parse("a=1&b&a=2").toMap();
        assertEquals(Map.of("a", "1", "b", ""), map);
        assertEquals(List.of("a", "b"), List.copyOf(map.keySet()));
    }

    @Test
    public void Parameters_cannot_be_modified() throws URNSyntaxError {
        RQComponent c = RQComponent.parse("a=1");
        assertThrows(UnsupportedOperationException.class, () -> c.parameters().clear());
        assertThrows(UnsupportedOperationException.class, () -> c.toMap().clear());
        assertThrows(UnsupportedOperationException.class, () -> c.values("a").clear());
    }

    @Test
    public void Built_component_equals_parsed_component() throws URNSyntaxError {
        RQComponent built = RQComponent.builder()
                .add("a", "1")
                .add("a", "b=c")
                .add("flag")
                .add("empty", "")
                .build();
        assertEquals("a=1&a=b=c&flag&empty=", built.toString());
        RQComponent parsed = RQComponent.parse(built.toString());
        assertEquals(built, parsed);
        assertEquals(built.hashCode(), parsed.hashCode());
    }

    @Test
    public void Empty_builder_returns_EMPTY() throws URNSyntaxError {
        assertSame(RQComponent.EMPTY, RQComponent.builder().build());
    }

    @Test
    public void Builder_rejects_names_and_values_that_would_not_round_trip() {
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("a=b", "c"));
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("a&b"));
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("a", "b&c"));
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("").build());
    }

    @Test
    public void Builder_rejects_null() {
        assertThrows(IllegalArgumentException.class, () -> RQComponent.builder().add(null));
        assertThrows(IllegalArgumentException.class, () -> RQComponent.builder().add("a", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a b", "a<b", "a#b", "a%zz", "a%2", "/a", "?a", "ä"})
    public void Invalid_component_throws_URNSyntaxError(String component) {
        assertThrows(URNSyntaxError.class, () -> RQComponent.parse(component));
    }

    @Test
    public void Builder_rejects_invalid_characters() {
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("a b"));
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("a", "<"));
        assertThrows(URNSyntaxError.class, () -> RQComponent.builder().add("/a").build());
    }

    @Test
    public void Long_component_is_validated_without_stack_overflow() throws URNSyntaxError {
        String component = "a=" + "b".repeat(100_000);
        assertEquals(component, RQComponent.parse(component).toString());
    }
}
