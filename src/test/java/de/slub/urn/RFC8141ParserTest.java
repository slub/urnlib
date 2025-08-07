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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RFC8141ParserTest extends URNParserTest {

    @Test
    public void Slash_character_is_allowed() throws URNSyntaxError {
        getURNParser().parse("urn:example:1/406/47452/2");
    }

    @Override
    URNParser<URN_8141> getURNParser() {
        return URN.rfc8141();
    }

    @Test
    public void Slash_character_is_part_of_NSS() throws URNSyntaxError {
        URN urn = getURNParser().parse("urn:example:1/406/47452/2");
        assertEquals("1/406/47452/2", urn.namespaceSpecificString().toString());
    }

    @Test
    public void R_component_get_parsed_into_resolution_parameter_map() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("CCResolve:cc"), "r-component should contain key");
        assertEquals("uk", resolutionParameters.get("CCResolve:cc"), "r-component key not as expected");
    }

    /**
     * This is actually not specified by RFC 8141, but rather anticipated here.
     */
    @Test
    public void R_component_can_hold_multiple_parameters() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk&Foo=bar");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("Foo"), "r-component should contain key");
        assertEquals("bar", resolutionParameters.get("Foo"), "r-component key not as expected");
    }

    @Test
    public void Q_component_get_parsed_into_query_parameter_map() throws URNSyntaxError {
        URN_8141 urn = getURNParser()
                .parse("urn:example:weather?=op=map&lat=39.56&lon=-104.85&datetime=1969-07-21T02:56:15Z");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> queryParameters = rqf.queryParameters();
        assertNotNull(queryParameters);

        for (Map.Entry<String, String> me : new HashMap<String, String>() {{
            put("op", "map");
            put("lat", "39.56");
            put("lon", "-104.85");
            put("datetime", "1969-07-21T02:56:15Z");
        }}.entrySet()) {
            assertTrue(queryParameters.containsKey(me.getKey())
                            && queryParameters.get(me.getKey()).equals(me.getValue()),
                    String.format("q-component should have entry for `%s` with value `%s`",
                            me.getKey(),
                            me.getValue())
            );
        }
    }

    @Test
    public void Trailing_dash_is_ignored() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:foo:bar#");
        RQF_RFC8141 rqf = urn.getRQFComponents();
        assertTrue(rqf.fragment().isEmpty());
    }

    @Test
    public void F_component_gets_parsed_into_fragment_string() throws URNSyntaxError {
        URN_8141 urn = getURNParser()
                .parse("urn:example:foo-bar-baz-qux#somepart");
        RQF_RFC8141 rqf = urn.getRQFComponents();
        assertEquals("somepart", rqf.fragment(), "Missing fragment `somepart`");
    }

    @Test
    public void Resolution_parameters_without_values_get_empty_value() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:foo:bar?+foo");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("foo"));
        assertEquals("", resolutionParameters.get("foo"));
    }

    @Test
    public void Query_parameters_without_values_get_empty_value() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:foo:bar?=bar");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> queryParameters = rqf.queryParameters();
        assertTrue(queryParameters.containsKey("bar"));
        assertEquals("", queryParameters.get("bar"));
    }

    @Test
    public void Parses_individual_resolution_parameters() throws URNSyntaxError {
        String str = "urn:foo:bar?+a=b&c=d";
        URN_8141 urn = getURNParser().parse(str);

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("a"));
        assertTrue(resolutionParameters.containsValue("b"));
        assertTrue(resolutionParameters.containsKey("c"));
        assertTrue(resolutionParameters.containsValue("d"));
    }

    @Test
    public void Parses_individual_query_parameters() throws URNSyntaxError {
        String str = "urn:foo:bar?=q=v&u=w";
        URN_8141 urn = getURNParser().parse(str);

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> queryParameters = rqf.queryParameters();
        assertTrue(queryParameters.containsKey("q"));
        assertTrue(queryParameters.containsValue("v"));
        assertTrue(queryParameters.containsKey("u"));
        assertTrue(queryParameters.containsValue("w"));
    }

    @Test
    public void Parses_fragment_part() throws URNSyntaxError {
        String str = "urn:foo:bar#bar";
        URN_8141 urn = getURNParser().parse(str);
        RQF_RFC8141 rqf = urn.getRQFComponents();
        assertEquals("bar", rqf.fragment());
    }

    @Test
    public void Parses_RQF_components() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+CCResolve:cc=uk?=op=map#somepart");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.containsKey("CCResolve:cc"), "r-component should contain key");
        assertEquals("uk", resolutionParameters.get("CCResolve:cc"), "r-component value not as expected");

        Map<String, String> queryParameters = rqf.queryParameters();
        assertTrue(queryParameters.containsKey("op"), "q-component should contain key");
        assertEquals("map", queryParameters.get("op"), "q-component value not as expected");

        assertEquals("somepart", rqf.fragment(), "Missing fragment `somepart`");
    }

    @Test
    public void Parses_RQF_components_with_empty_R() throws URNSyntaxError {
        URN_8141 urn = getURNParser().parse("urn:example:foo-bar-baz-qux?+?=op=map#somepart");

        RQF_RFC8141 rqf = urn.getRQFComponents();

        Map<String, String> resolutionParameters = rqf.resolutionParameters();
        assertTrue(resolutionParameters.isEmpty(), "r-component should be empty");

        Map<String, String> queryParameters = rqf.queryParameters();
        assertTrue(queryParameters.containsKey("op"), "q-component should contain key");
        assertEquals("map", queryParameters.get("op"), "q-component value not as expected");

        assertEquals("somepart", rqf.fragment(), "Missing fragment `somepart`");
    }

}
