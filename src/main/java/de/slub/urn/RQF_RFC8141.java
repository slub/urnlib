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

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;

/**
 * Class for representing and parsing a URNs RQF (resolution, query, fragment) parameters
 * and making them accessible via unmodifiable maps.
 */
public final class RQF_RFC8141 {

    public static final RQF_RFC8141 NULL = new RQF_RFC8141(EMPTY_MAP, EMPTY_MAP, "");

    private final Map<String, String> resolutionParameters;
    private final Map<String, String> queryParameters;
    private final String              fragment;
    private final String              stringRepresentation;

    /**
     * @param resolutionParameters
     * @param queryParameters
     * @param fragment
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
            for (Map.Entry<String, String> kv : resolutionParameters.entrySet()) {
                sb.append(kv.getKey()).append('=').append(kv.getValue());
            }
        }
        if (!queryParameters.isEmpty()) {
            sb.append("?=");
            for (Map.Entry<String, String> kv : queryParameters.entrySet()) {
                sb.append(kv.getKey()).append('=').append(kv.getValue());
            }
        }
        if (!fragment.isEmpty()) {
            sb.append('#').append(fragment);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

    public Map<String, String> resolutionParameters() {
        return resolutionParameters;
    }

    public Map<String, String> queryParameters() {
        return queryParameters;
    }

    public String fragment() {
        return fragment;
    }

}
