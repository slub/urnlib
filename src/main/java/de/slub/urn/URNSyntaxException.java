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

/**
 * An Exception which is thrown if a URN or any part of a URN cannot be parsed due to violations of the URN syntax.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 */
public class URNSyntaxException extends Exception {

    /**
     * @see Exception#Exception(String)
     */
    public URNSyntaxException(String msg) {
        super(msg);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public URNSyntaxException(String msg, Throwable t) {
        super(msg, t);
    }
}
