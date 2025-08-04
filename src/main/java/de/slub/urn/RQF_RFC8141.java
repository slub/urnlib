/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
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

import static java.util.Collections.EMPTY_MAP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class for representing and parsing optional resolution, query and fragment components
 * and making them accessible via unmodifiable maps.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8141#page-12">2.3 Optional Components</a>
 */
public final class RQF_RFC8141 {

    /**
     * Instance representing a the absense of RQF components.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Null_object_pattern">Null object pattern</a>
     */
    public static final RQF_RFC8141 NULL = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");

    private final Map<String, String> resolutionParameters;
    private final Map<String, String> queryParameters;
    private final String              fragment;
    private final String              stringRepresentation;

    /**
     * Construct a RQF component.
     *
     * @param resolutionParameters Map of resolution parameters
     * @param queryParameters Map of query parameters
     * @param fragment The fragment
     * @throws IllegalArgumentException if any of the parameters are null
     */
    public RQF_RFC8141(
            Map<String, String> resolutionParameters,
            Map<String, String> queryParameters,
            String fragment) {
        if (queryParameters == null) {
            throw new IllegalArgumentException("Resolution parameter map cannot be null");
        }
        if (fragment == null) {
            throw new IllegalArgumentException("Query parameter map cannot be null");
        }
        if (resolutionParameters == null) {
            throw new IllegalArgumentException("Fragment string cannot be null");
        }
        this.resolutionParameters = Collections.unmodifiableMap(resolutionParameters);
        this.queryParameters = Collections.unmodifiableMap(queryParameters);
        this.fragment = fragment;
        this.stringRepresentation = initialToString();
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two RQF objects are considered equal if their resolution parameters, query parameters and fragment are equal.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code RQF_RFC8141} instance and is equivalent to this instance.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RQF_RFC8141) {
            RQF_RFC8141 that = (RQF_RFC8141) obj;
            return this.resolutionParameters.equals(that.resolutionParameters)
                    && this.queryParameters.equals(that.queryParameters)
                    && this.fragment.equals(that.fragment);
        }
        return false;
    }

    private String initialToString() {
        StringBuilder sb = new StringBuilder();
        if (!resolutionParameters.isEmpty()) {
            sb.append("?+");
            final List<String> acc = new ArrayList<>();
            for (Map.Entry<String, String> kv : resolutionParameters.entrySet()) {
                acc.add(kv.getKey() + "=" + kv.getValue());
            }
            sb.append(String.join("&", acc));
        }
        if (!queryParameters.isEmpty()) {
            sb.append("?=");
            final List<String> acc = new ArrayList<>();
            for (Map.Entry<String, String> kv : queryParameters.entrySet()) {
            	acc.add(kv.getKey() + "=" + kv.getValue());
            }
            sb.append(String.join("&", acc));
        }
        if (!fragment.isEmpty()) {
            sb.append('#').append(fragment);
        }
        return sb.toString();
    }

    /**
     * Calculates hash code based on the string representation of this RQF instance.
     *
     * @return This namespace specific strings hash code
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Return the request, query and fragment components as a string literal.
     *
     * @return Encoded Namespace Specific String literal
     */
    @Override
    public String toString() {
        return stringRepresentation;
    }

    /**
     * Return a map of resolution parameters where the key is a parameter name and the value a parameter value.
     *
     * @return Map of resolution parameters
     */
    public Map<String, String> resolutionParameters() {
        return resolutionParameters;
    }

    /**
     * Return a map of query parameters where the key is a parameter name and the value a parameter value.
     *
     * @return Map of query parameters
     */
    public Map<String, String> queryParameters() {
        return queryParameters;
    }

    /**
     * Return the fragment component string.
     *
     * @return Fragment component
     */
    public String fragment() {
        return fragment;
    }

}
