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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
    public void NULL_object_is_not_equal_to_null() {
        assertNotEquals(RQF_RFC8141.NULL, null);
    }

    @Test
    public void Different_objects_are_not_equal() {
        RQF_RFC8141 n1 = RQF_RFC8141.parse("?+a=b?=foo=bar");
        RQF_RFC8141 n2 = RQF_RFC8141.parse("?+a=b?=foo=bar#baz");
        assertNotEquals(n1, n2);
    }

    @Test
    public void Equals_NULL_object_when_parsing_empty_string() {
        RQF_RFC8141 n = RQF_RFC8141.parse("");
        assertEquals(RQF_RFC8141.NULL, n);
    }

    @Test
    public void Resolution_parameters_without_values_get_empty_value() {
        RQF_RFC8141 n = RQF_RFC8141.parse("?+foo");

        Map<String, String> resolutionParameters = n.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("foo"));
        assertEquals("", resolutionParameters.get("foo"));
    }

    @Test
    public void Query_parameters_without_values_get_empty_value() {
        RQF_RFC8141 n = RQF_RFC8141.parse("?=bar");

        Map<String, String> queryParameters = n.queryParameters();
        assertTrue(queryParameters.containsKey("bar"));
        assertEquals("", queryParameters.get("bar"));
    }

    @Test
    public void Empty_RQFComponent_generates_empty_string() {
        assertEquals("", RQF_RFC8141.NULL.toString());
    }

    @Test
    public void Identical_objects_have_same_hash() {
        RQF_RFC8141 n1 = RQF_RFC8141.parse("?+a=b?=foo=bar#baz");
        RQF_RFC8141 n2 = RQF_RFC8141.parse("?+a=b?=foo=bar#baz");
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    public void Parsed_string_equals_toString_result() {
        String      s = "?+a=b?=foo=bar#baz";
        RQF_RFC8141 n = RQF_RFC8141.parse(s);
        assertEquals(s, n.toString());
    }

    @Test
    public void Parses_resolution_parts() {
        String      s = "?+a=b&c=d";
        RQF_RFC8141 n = RQF_RFC8141.parse(s);

        Map<String, String> resolutionParameters = n.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("a"));
        assertTrue(resolutionParameters.containsValue("b"));
        assertTrue(resolutionParameters.containsKey("c"));
        assertTrue(resolutionParameters.containsValue("d"));
    }

    @Test
    public void Parses_query_parts() {
        String      s = "?=q=v&u=w";
        RQF_RFC8141 n = RQF_RFC8141.parse(s);

        Map<String, String> queryParameters = n.queryParameters();
        assertTrue(queryParameters.containsKey("q"));
        assertTrue(queryParameters.containsValue("v"));
        assertTrue(queryParameters.containsKey("u"));
        assertTrue(queryParameters.containsValue("w"));
    }

    @Test
    public void Parses_fragment_part() {
        String      s = "#bar";
        RQF_RFC8141 n = RQF_RFC8141.parse(s);
        assertEquals("bar", n.fragment());
    }

    @Test
    public void Empty_RQF_components_serialize_to_empty_string() {
        RQF_RFC8141 n = RQF_RFC8141.parse("");
        assertEquals("", n.toString());
    }

}