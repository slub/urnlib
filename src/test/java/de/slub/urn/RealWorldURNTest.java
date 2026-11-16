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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks the parsers against URNs collected from real-world sources, see the header of the resource files.
 */
public class RealWorldURNTest {

    private static final String VALID_UNDER_BOTH_RFCS = "real-world-urns.txt";
    private static final String VALID_UNDER_RFC8141_ONLY = "real-world-urns-rfc8141-only.txt";

    static Stream<String> urnsValidUnderBothRFCs() {
        return readURNs(VALID_UNDER_BOTH_RFCS).stream();
    }

    static Stream<String> urnsValidUnderRFC8141Only() {
        return readURNs(VALID_UNDER_RFC8141_ONLY).stream();
    }

    static Stream<String> urnsValidUnderRFC8141() {
        return Stream.concat(urnsValidUnderBothRFCs(), urnsValidUnderRFC8141Only());
    }

    private static List<String> readURNs(String resource) {
        try (InputStream in = RealWorldURNTest.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing test resource " + resource);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("urnsValidUnderRFC8141")
    public void RFC8141_parser_accepts_real_world_URN_and_round_trips_it(String urnLiteral) throws URNSyntaxError {
        URN_8141 urn = URN.rfc8141().parse(urnLiteral);
        assertEquals(urn, URN.rfc8141().parse(urn.toString()), "Parsing the string representation should yield an equal URN");
    }

    @ParameterizedTest
    @MethodSource("urnsValidUnderBothRFCs")
    public void RFC2141_parser_accepts_real_world_URN_and_round_trips_it(String urnLiteral) throws URNSyntaxError {
        URN_2141 urn = URN.rfc2141().parse(urnLiteral);
        assertEquals(urn, URN.rfc2141().parse(urn.toString()), "Parsing the string representation should yield an equal URN");
    }

    @ParameterizedTest
    @MethodSource("urnsValidUnderRFC8141Only")
    public void RFC2141_parser_rejects_real_world_URN_using_RFC8141_syntax(String urnLiteral) {
        assertThrows(URNSyntaxError.class, () -> URN.rfc2141().parse(urnLiteral));
    }
}
