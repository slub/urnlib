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
 * Defines an interface for classes which need to report about the RFC they are implementing.
 * <p>
 * It is implemented by {@link URN}, {@link NamespaceIdentifier} and {@link NamespaceSpecificString}. Every instance
 * follows exactly one URN syntax. {@link #supports(RFC)} is true for that one only: an RFC 8141 type does not report
 * support for RFC 2141, although most RFC 2141 literals are valid RFC 8141 literals.
 *
 * @author Ralf Claussnitzer
 */
public interface RFCSupport {

    /**
     * Return RFC supported by this instance.
     *
     * @return The supported RFC
     */
    RFC supportedRFC();

    /**
     * Tells whether a given RFC is supported.
     *
     * @param rfc The RFC to check if it is supported
     * @return True, if the given RFC is the one returned by {@link #supportedRFC()}
     */
    boolean supports(RFC rfc);

}
