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

package de.slub.urn;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class URNResolverTest {

    private static URL URL_A, URL_B;
    private static URN URN_A, URN_AB;
    private static Map<URN, List<URL>> URIMAP;
    private DemoURNResolver subject;

    @Before
    public void createDemoResolver() {
        this.subject = new DemoURNResolver(URIMAP);
    }

    @Test
    public void Demonstrate_intended_use_of_resolve_method_returning_a_URL() throws Exception {
        assertEquals(
                String.format("%s should resolve to %s", URN_A, URL_A),
                URL_A, subject.resolve(URN_A).get(0));
    }

    @Test
    public void Demonstrate_intended_use_of_resolve_method_returning_multiple_URLs() throws Exception {
        List<URL> urls = subject.resolve(URN_AB);
        int expectedNumberOfUrls = 2;
        assertEquals(
                String.format("%s should resolve to %d URLs", URN_AB, expectedNumberOfUrls),
                expectedNumberOfUrls, urls.size());
    }

    @BeforeClass
    public static void createDemoUriMap() throws URNSyntaxException, MalformedURLException {
        URL_A = new URL("http://demo.org/a/1234");
        URL_B = new URL("http://demo.org/b/5678");

        URN_A = URN.fromString("urn:demo:a-1234");
        URN_AB = URN.fromString("urn:demo:ab-12");

        URIMAP = new HashMap<>();
        URIMAP.put(URN_A, new ArrayList<URL>(1) {{
            add(URL_A);
        }});
        URIMAP.put(URN_AB, new ArrayList<URL>(2) {{
            add(URL_A);
            add(URL_B);
        }});
    }

    private class DemoURNResolver implements URNResolver {

        private final Map<URN, List<URL>> map;

        DemoURNResolver(Map<URN, List<URL>> map) {
            this.map = new HashMap<>();
            this.map.putAll(map);
        }

        @Override
        public List<URL> resolve(final URN urn) throws URNResolvingException {
            return map.get(urn);
        }
    }
}