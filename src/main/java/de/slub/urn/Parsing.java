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

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Shared parsing helpers for the RFC specific URN parsers.
 */
final class Parsing {

    private Parsing() {
    }

    /**
     * Split a URN literal into its Namespace Identifier and the remaining scheme specific part.
     * <p>
     * The remaining part starts right after the second colon. It contains the Namespace Specific String
     * and, depending on the RFC, optional further components.
     *
     * @param urnLiteral URN literal, must not be null or empty
     * @param rfc        RFC to report in case of syntax errors
     * @return Array with two elements: the NID literal and the non-empty remainder
     * @throws URNSyntaxError if the literal does not start with "urn:", the NID is empty, or nothing follows the NID
     */
    static String[] splitNamespaceIdentifier(String urnLiteral, RFC rfc) throws URNSyntaxError {
        final String[] parts = urnLiteral.split(":", 3);
        if (parts.length < 3 || !URN.SCHEME.equalsIgnoreCase(parts[0])) {
            throw syntaxError(rfc, String.format("Invalid format `%s` is probably not a URN", urnLiteral));
        }
        if (parts[1].isEmpty()) {
            throw syntaxError(rfc, String.format("Empty Namespace Identifier in `%s`", urnLiteral));
        }
        if (parts[2].isEmpty()) {
            throw syntaxError(rfc, String.format("Empty Namespace Specific String in `%s`", urnLiteral));
        }
        return new String[]{parts[1], parts[2]};
    }
}
