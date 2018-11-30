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

/**
 * Defines an interface for URN parsers.
 * <p>
 * It supports parsing URNs from string literals as well as parsing from URIs.
 *
 * @param <T> Type of URN the parser will produce
 * @author Ralf Claussnitzer
 */
public interface URNParser<T extends URN> {

    /**
     * Create a new URN instance by parsing a URN string literal.
     *
     * @param str String to be parsed into an URN instance
     * @return New URN instance
     * @throws IllegalArgumentException If the given string is null
     * @throws URNSyntaxException       If the given string cannot be parsed into a URN
     */
    T parse(String str) throws URNSyntaxException;

    /**
     * Constructing a new URN instance by parsing URN parts from a URI instance.
     *
     * @param uri The URI to be parsed into a URN
     * @return A new URN instance
     * @throws IllegalArgumentException If the given URI is null
     * @throws URNSyntaxException       If the given URI cannot be parsed into a URN
     */
    T parse(URI uri) throws URNSyntaxException;

}
