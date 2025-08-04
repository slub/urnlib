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
import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Implements a parser for URNs according to RFC 2141.
 *
 * @author Ralf Claussnitzer
 * @see URN_2141
 */
public class RFC2141Parser implements URNParser<URN_2141> {

    /**
     * @see URNParser#parse(String)
     */
    @Override
    public URN_2141 parse(String str) throws URNSyntaxError {
        if ((str == null) || (str.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = str.split(":");

        if (parts.length < 3 || !URN.SCHEME.equalsIgnoreCase(parts[0])) {
            throw syntaxError(RFC.RFC_2141,
                    String.format("Invalid format `%s` is probably not a URN", str));
        }

        return new URN_2141(
                new NID_RFC2141(parts[1]),
                new NSS_RFC2141(str.substring(str.indexOf(parts[1]) + parts[1].length() + 1), URL_ENCODED));
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_2141 parse(URI uri) throws URNSyntaxError {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return parse(uri.toASCIIString());
    }
}
