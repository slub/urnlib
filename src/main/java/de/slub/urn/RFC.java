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
 * Represents a supported URN syntax RFC.
 * <p>
 * The rules of a particular namespace are not listed here, see {@link Namespace#specification()}.
 */
public enum RFC implements Specification {
    /**
     * RFC 2141, URN Syntax (May 1997).
     */
    RFC_2141("https://tools.ietf.org/html/rfc2141"),
    /**
     * RFC 8141, Uniform Resource Names (April 2017), superseding RFC 2141.
     */
    RFC_8141("https://tools.ietf.org/html/rfc8141");

    private final String url;

    RFC(String url) {
        this.url = url;
    }

    /**
     * Return the URL of specification document for a particular RFC.
     *
     * @return URL string literal
     */
    @Override
    public String url() {
        return url;
    }
}
