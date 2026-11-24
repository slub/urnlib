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
 * Defines an interface for URN parsers.
 * <p>
 * It supports parsing URNs from string literals as well as parsing from URIs. A parser is obtained from {@link URN}:
 * <ul>
 *     <li>{@link URN#rfc8141()} for the current URN syntax,</li>
 *     <li>{@link URN#rfc2141()} for the syntax it replaced,</li>
 *     <li>{@link URN#in(Namespace)} for the URNs of a particular namespace.</li>
 * </ul>
 * A parser either returns a valid URN or throws. The returned URN keeps the literal as it was given, there is no
 * normalization while parsing. The parsers of this library have no mutable state and can be shared between threads.
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
     * @throws URNSyntaxError           If the given string cannot be parsed into a URN
     */
    T parse(String str) throws URNSyntaxError;

    /**
     * Constructing a new URN instance by parsing URN parts from a URI instance.
     *
     * @param uri The URI to be parsed into a URN
     * @return A new URN instance
     * @throws IllegalArgumentException If the given URI is null
     * @throws URNSyntaxError           If the given URI cannot be parsed into a URN
     */
    T parse(URI uri) throws URNSyntaxError;

}
