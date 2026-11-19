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

/**
 * A document that defines rules for URNs, and that a {@link URNSyntaxError} can refer to.
 * <p>
 * The URN syntaxes supported by this library are the constants of {@link RFC}. A {@link Namespace} names the
 * document that defines its rules by an implementation of its own.
 *
 * @author Ralf Claussnitzer
 */
public interface Specification {

    /**
     * Return the name of the specification, e.g. {@code RFC 4198}.
     *
     * @return The name
     */
    String name();

    /**
     * Return the URL of the specification document.
     *
     * @return URL string literal
     */
    String url();

}
