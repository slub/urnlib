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

/**
 * Defines an interface for classes which need to report about the RFC they are implementing.
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
     * @return True, if the given RFC is supported
     */
    boolean supports(RFC rfc);

}
