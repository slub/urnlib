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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RFC8141ParserTest extends URNParserTest {

    @Test
    public void Slash_character_is_allowed() throws URNSyntaxException {
        getURNParser().parse("urn:example:1/406/47452/2");
    }

    @Override
    URNParser<URN_8141> getURNParser() {
        return URN.rfc8141();
    }

    @Test
    public void Slash_character_is_part_of_NSS() throws URNSyntaxException {
        URN urn = getURNParser().parse("urn:example:1/406/47452/2");
        assertEquals("1/406/47452/2", urn.namespaceSpecificString());
    }

    @Test
    public void R_component_get_parsed_into_resolution_parameter_map() throws URNSyntaxException {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk");

        Map<String, String> resolutionParameters = urn.resolutionParameters();
        assertTrue("r-component should contain key", resolutionParameters.containsKey("CCResolve:cc"));
        assertEquals("r-component key not as expected", "uk", resolutionParameters.get("CCResolve:cc"));
    }

    /**
     * This is actually not specified by RFC 8141, but rather anticipated here.
     */
    @Test
    public void R_component_can_hold_multiple_parameters() throws URNSyntaxException {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk&Foo=bar");

        Map<String, String> resolutionParameters = urn.resolutionParameters();
        assertTrue("r-component should contain key", resolutionParameters.containsKey("Foo"));
        assertEquals("r-component key not as expected", "bar", resolutionParameters.get("Foo"));
    }

    @Test
    public void Q_component_get_parsed_into_query_parameter_map() throws URNSyntaxException {
        URN_8141 urn = getURNParser()
                .parse("urn:example:weather?=op=map&lat=39.56&lon=-104.85&datetime=1969-07-21T02:56:15Z");

        Map<String, String> queryParameters = urn.queryParameters();
        assertNotNull(queryParameters);

        for (Map.Entry<String, String> me : new HashMap<String, String>() {{
            put("op", "map");
            put("lat", "39.56");
            put("lon", "-104.85");
            put("datetime", "1969-07-21T02:56:15Z");
        }}.entrySet()) {
            assertTrue(String.format("q-component should have entry for `%s` with value `%s`",
                    me.getKey(),
                    me.getValue()),
                    queryParameters.containsKey(me.getKey())
                            && queryParameters.get(me.getKey()).equals(me.getValue()));
        }
    }

    @Test
    public void Trailing_dash_is_ignored() throws URNSyntaxException {
        URN_8141 urn = getURNParser().parse("urn:foo:bar#");
        assertTrue(urn.fragment().isEmpty());
    }

    @Test
    public void F_component_gets_parsed_into_fragment_string() throws URNSyntaxException {
        URN_8141 urn = getURNParser()
                .parse("urn:example:foo-bar-baz-qux#somepart");
        assertEquals("Missing fragment `somepart`", "somepart", urn.fragment());
    }

    @Test
    public void Resolution_parameters_without_values_get_empty_value() throws URNSyntaxException {
        URN_8141 urn = getURNParser().parse("urn:foo:bar?+foo");

        Map<String, String> resolutionParameters = urn.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("foo"));
        assertEquals("", resolutionParameters.get("foo"));
    }

    @Test
    public void Query_parameters_without_values_get_empty_value() throws URNSyntaxException {
        URN_8141 urn = getURNParser().parse("urn:foo:bar?=bar");

        Map<String, String> queryParameters = urn.queryParameters();
        assertTrue(queryParameters.containsKey("bar"));
        assertEquals("", queryParameters.get("bar"));
    }

    @Test
    public void Parses_individual_resolution_parameters() throws URNSyntaxException {
        String   str = "urn:foo:bar?+a=b&c=d";
        URN_8141 urn = getURNParser().parse(str);

        Map<String, String> resolutionParameters = urn.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("a"));
        assertTrue(resolutionParameters.containsValue("b"));
        assertTrue(resolutionParameters.containsKey("c"));
        assertTrue(resolutionParameters.containsValue("d"));
    }

    @Test
    public void Parses_individual_query_parameters() throws URNSyntaxException {
        String   str = "urn:foo:bar?=q=v&u=w";
        URN_8141 urn = getURNParser().parse(str);

        Map<String, String> queryParameters = urn.queryParameters();
        assertTrue(queryParameters.containsKey("q"));
        assertTrue(queryParameters.containsValue("v"));
        assertTrue(queryParameters.containsKey("u"));
        assertTrue(queryParameters.containsValue("w"));
    }

    @Test
    public void Parses_fragment_part() throws URNSyntaxException {
        String   str = "urn:foo:bar#bar";
        URN_8141 urn = getURNParser().parse(str);
        assertEquals("bar", urn.fragment());
    }

}
