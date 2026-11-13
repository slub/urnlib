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

import java.net.URI;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;

/**
 * Implements a parser for URNs according to RFC 2141.
 *
 * @author Ralf Claussnitzer
 * @see URN_2141
 */
public class RFC2141Parser implements URNParser<URN_2141> {

    /**
     * Create a new parser for RFC 2141 URNs. Consider using {@link URN#rfc2141()} instead.
     */
    public RFC2141Parser() {
    }

    /**
     * @see URNParser#parse(String)
     */
    @Override
    public URN_2141 parse(String str) throws URNSyntaxError {
        final String[] parts = Parsing.splitNamespaceIdentifier(Parsing.requireURNLiteral(str), RFC.RFC_2141);
        return new URN_2141(
                new NID_RFC2141(parts[0]),
                new NSS_RFC2141(parts[1], URL_ENCODED));
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_2141 parse(URI uri) throws URNSyntaxError {
        return parse(Parsing.literalOf(uri));
    }
}
