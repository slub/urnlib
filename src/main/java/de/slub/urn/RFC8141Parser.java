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

import java.net.URI;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;

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
        final String[] nidAndRest = Parsing.splitNamespaceIdentifier(
                Parsing.requireURNLiteral(urnLiteral), RFC.RFC_8141);
        final NID_RFC8141 nid = new NID_RFC8141(nidAndRest[0]);
        final String[] nssAndRqf = Parsing.splitNamespaceSpecificString(nidAndRest[1], urnLiteral, RFC.RFC_8141);
        final NSS_RFC8141 nss = new NSS_RFC8141(nssAndRqf[0], URL_ENCODED);
        final RQF_RFC8141 rqfComponents = RQF_RFC8141.parse(nssAndRqf[1]);

        return new URN_8141(nid, nss, rqfComponents);
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_8141 parse(URI uri) throws URNSyntaxError {
        return parse(Parsing.literalOf(uri));
    }

}
