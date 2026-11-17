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

/**
 * Implements a parser for Federated Content URNs according to RFC 4198.
 * <p>
 * A literal is split into its parts according to RFC 8141, so resolution, query and fragment components are
 * accepted. The Namespace Identifier has to be {@code fdc} and the Namespace Specific String has to follow RFC 4198.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc4198">A Uniform Resource Name (URN) Namespace for Federated Content</a>
 */
public class RFC4198Parser implements URNParser<URN_4198> {

    /**
     * Creates a new parser for Federated Content URNs.
     */
    public RFC4198Parser() {
    }

    /**
     * @see URNParser#parse(String)
     */
    @Override
    public URN_4198 parse(String urnLiteral) throws URNSyntaxError {
        final String[] nidAndRest = Parsing.splitNamespaceIdentifier(
                Parsing.requireURNLiteral(urnLiteral), RFC.RFC_8141);
        final NID_RFC8141 nid = new NID_RFC8141(nidAndRest[0]);
        final String[] nssAndRqf = Parsing.splitNamespaceSpecificString(nidAndRest[1], urnLiteral, RFC.RFC_8141);
        return URN_4198.create(nid, nssAndRqf[0], RQF_RFC8141.parse(nssAndRqf[1]));
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_4198 parse(URI uri) throws URNSyntaxError {
        return parse(Parsing.literalOf(uri));
    }

}
