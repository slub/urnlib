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
 * Base class Exception for unrecoverable errors in an URN resolver.
 *
 * @author Ralf Claussnitzer
 */
public class URNResolvingError extends Exception {

    private URN failedUrn;

    /**
     * @see Exception#Exception(String)
     */
    public URNResolvingError(String message) {
        super(message);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public URNResolvingError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create an exception with message and a reference to the URN that needed to be resolved.
     *
     * @param message Exception message
     * @param urn     URN causing the resolving error
     */
    public URNResolvingError(String message, URN urn) {
        super(message);
        this.failedUrn = urn;
    }

    /**
     * Return URN causing the resolving error.
     *
     * @return The URN causing the resolving error
     */
    public URN failedUrn() {
        return failedUrn;
    }
}
