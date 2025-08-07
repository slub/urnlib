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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class URNResolverDemoTest {

    private static URI URI_A, URI_B;
    private static URN URN_A, URN_AB;
    private static Map<URN, Set<URI>> URIMAP;
    private        DemoURNResolver    subject;

    @BeforeAll
    public static void createDemoUriMap() throws URNSyntaxError {
        URI_A = URI.create("http://demo.org/a/1234");
        URI_B = URI.create("http://demo.org/b/5678");

        URN_A = URN.rfc2141().parse("urn:demo:a-1234");
        URN_AB = URN.rfc2141().parse("urn:demo:ab-12");

        URIMAP = new HashMap<>();
        URIMAP.put(URN_A, new HashSet<>(1) {{
            add(URI_A);
        }});
        URIMAP.put(URN_AB, new HashSet<>(2) {{
            add(URI_A);
            add(URI_B);
        }});
    }

    @BeforeEach
    public void createDemoResolver() {
        this.subject = new DemoURNResolver(URIMAP);
    }

    @Test
    public void Demonstrate_intended_use_of_resolve_method_returning_a_URL() {
        assertTrue(
                subject.resolve(URN_A).contains(URI_A),
                String.format("%s should resolve to %s", URN_A, URI_A));
    }

    @Test
    public void Demonstrate_intended_use_of_resolve_method_returning_multiple_URLs() {
        Set<URI> urls = subject.resolve(URN_AB);
        int expectedNumberOfUrls = 2;
        assertEquals(
                expectedNumberOfUrls, urls.size(),
                String.format("%s should resolve to %d URLs", URN_AB, expectedNumberOfUrls));
    }

    private static class DemoURNResolver implements URNResolver<URI> {

        private final Map<URN, Set<URI>> map;

        DemoURNResolver(Map<URN, Set<URI>> map) {
            this.map = new HashMap<>();
            this.map.putAll(map);
        }

        @Override
        public Set<URI> resolve(final URN urn) {
            return map.get(urn);
        }
    }
}
