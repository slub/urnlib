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

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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

    @Test(expected = IllegalArgumentException.class)
    public void Passing_null_as_resolutionParameterMap_throws_exception() {
        new RQF_RFC8141(null, Collections.EMPTY_MAP, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void Passing_null_as_queryParameterMap_throws_exception() {
        new RQF_RFC8141(Collections.EMPTY_MAP, null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void Passing_null_as_fragment_throws_exception() {
        new RQF_RFC8141(Collections.EMPTY_MAP, Collections.EMPTY_MAP, null);
    }


    @Test
    public void ToString_With_Parameters_And_Fragment() {
        Map<String, String> resolutionParameters = new HashMap<>();
        resolutionParameters.put("resolutionParameter0", "foo0");
        resolutionParameters.put("resolutionParameter1", "foo1");
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("queryParameter0", "bar0");
        queryParameters.put("queryParameters1", "bar1");
        String fragment = "fragment0";
        RQF_RFC8141 urn = new RQF_RFC8141(resolutionParameters, queryParameters, fragment);
        String expected = "?+resolutionParameter1=foo1&resolutionParameter0=foo0?=queryParameter0=bar0&queryParameters1=bar1#fragment0";

        String actual = urn.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void ToString_Without_Parameter_And_Fragment() {
        Map<String, String> resolutionParameters = new HashMap<>();
        Map<String, String> queryParameters = new HashMap<>();
        String fragment = "";
        RQF_RFC8141 urn = new RQF_RFC8141(resolutionParameters, queryParameters, fragment);
        String expected = "";

        String actual = urn.toString();

        assertEquals(expected, actual);
    }

}