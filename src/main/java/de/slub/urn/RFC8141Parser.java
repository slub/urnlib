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
 * Implements a parser for URNs according to RFC 8141.
 *
 * @author Ralf Claussnitzer
 * @see URN_8141
 */
public class RFC8141Parser implements URNParser<URN_8141> {

    /**
     * @see URNParser#parse(String)
     */
    @Override
    public URN_8141 parse(String urnLiteral) throws URNSyntaxError {
        if ((urnLiteral == null) || (urnLiteral.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = Parsing.splitNamespaceIdentifier(urnLiteral, RFC.RFC_8141);
        // NID is the first part after "urn:"
        final NID_RFC8141 nid = new NID_RFC8141(parts[0]);
        // rest of the literal holds NSS and optional RQF components
        final String schemeSpecificPart = parts[1];
        // NSS = pchar *(pchar / "/") contains neither "?" nor "#", so the first of these starts the RQF components
        int nssEnd = schemeSpecificPart.length();
        for (int i = 0; i < schemeSpecificPart.length(); i++) {
            final char c = schemeSpecificPart.charAt(i);
            if (c == '?' || c == '#') {
                nssEnd = i;
                break;
            }
        }
        final String nssString = schemeSpecificPart.substring(0, nssEnd);
        if (nssString.isEmpty()) {
            throw syntaxError(RFC.RFC_8141,
                    String.format("Empty Namespace Specific String in `%s`", urnLiteral));
        }
        final NSS_RFC8141 nss = new NSS_RFC8141(nssString, URL_ENCODED);
        final RQF_RFC8141 rqfComponents = RQF_RFC8141.parse(schemeSpecificPart.substring(nssEnd));

        return new URN_8141(nid, nss, rqfComponents);
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_8141 parse(URI uri) throws URNSyntaxError {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return parse(uri.toASCIIString());
    }

}
