/*
 * Copyright (C) 2017 Saxon State and University Library Dresden (SLUB)
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

import java.util.Set;

/**
 * Defines an interface for URN resolvers.
 * <p>
 * URN resolvers map a given URN to a set of objects of type T.
 *
 * @param <T> Type of object the resolver resolves to
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141#appendix-A">Handling of URNs by URL resolvers/browsers</a>
 * @see <a href="https://tools.ietf.org/html/rfc2276">Architectural Principles of Uniform Resource Name Resolution</a>
 */
public interface URNResolver<T> {

    /**
     * Resolves a given URN to a set of objects.
     *
     * @param urn The URN to be resolved
     * @return Set of objects that describe a location or identifier for the resource referenced by the given URN.
     * It should return an empty set, if URN can not be resolved.
     * @throws URNResolvingException If some error prevents the resolving
     */
    Set<T> resolve(URN urn) throws URNResolvingException;

}
