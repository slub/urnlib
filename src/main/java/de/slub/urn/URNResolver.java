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

import java.net.URL;
import java.util.List;

/**
 * Defines an interface for URN resolvers.
 *
 * URN resolvers map a given URN to a list of URLs.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141#appendix-A">Handling of URNs by URL resolvers/browsers</a>
 */
public interface URNResolver {

    /**
     * Resolves a given URN to a list of URLs.
     *
     * @param urn The URN to be resolved
     *
     * @return List of URLs that describe a location for the resource referenced by the given URN.
     * It shall return an empty list, if no URLs are known.
     *
     * @throws URNResolvingException If some error prevents the resolving
     */
    List<URL> resolve(URN urn) throws URNResolvingException;

}
