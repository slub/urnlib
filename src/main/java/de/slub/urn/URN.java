/*
 * Copyright (C) 2018 Saxon State and University Library Dresden (SLUB)
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
import java.net.URISyntaxException;

public abstract class URN {

    public static final String SCHEME = "urn";

    /**
     * Returns a parser to produce and parse URNs according to RFC 2141
     *
     * @return Parser instance
     */
    public static URNParser<URN_2141> rfc2141() {
        return new RFC2141Parser();
    }

    /**
     * Returns a parser to produce and parse URNs according to RFC 8141
     *
     * @return Parser instance
     */
    public static URNParser<URN_8141> rfc8141() {
        return new RFC8141Parser();
    }

    /**
     * Return the URNs Namespace Identifier part.
     *
     * @return The Namespace Identifier
     */
    public abstract String getNamespaceIdentifier();

    /**
     * Return the URNs Namespace Specific String part.
     *
     * @return The URL decoded Namespace Specific String
     */
    public abstract String getNamespaceSpecificString();

    /**
     * Returns the RFC this URN is based on.
     *
     * @return Supported RFC for this URN.
     */
    public abstract RFC supportedRfc();

    /**
     * Return a URI instance for this URN.
     *
     * @return URI instance
     * @throws URISyntaxException If a URI couldn't be constructed
     */
    public URI toURI() throws URISyntaxException {
        return new URI(this.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
