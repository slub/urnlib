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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;

/**
 * Class for representing and parsing a URNs RQF (resolution, query, fragment) parameters
 * and making them accessible via unmodifiable maps.
 */
public class RQFComponents {

    public static final RQFComponents NULL = new RQFComponents(EMPTY_MAP, EMPTY_MAP, "");

    private final Map<String, String> resolutionParameters;
    private final Map<String, String> queryParameters;
    private final String              fragment;

    private RQFComponents(
            Map<String, String> resolutionParameters,
            Map<String, String> queryParameters,
            String fragment) {
        this.resolutionParameters = Collections.unmodifiableMap(resolutionParameters);
        this.queryParameters = Collections.unmodifiableMap(queryParameters);
        this.fragment = fragment;
    }

    public static RQFComponents parse(String rqfComponents) {
        return new RQFComponents(
                parseResolutionParameters(rqfComponents),
                parseQueryParameters(rqfComponents),
                parseFragment(rqfComponents));
    }

    private static Map<String, String> parseResolutionParameters(String s) {
        final Pattern rComponentPattern = Pattern.compile("\\?\\+([^?#]*)");
        return parseComponentsFromMatcher(rComponentPattern.matcher(s));
    }

    private static Map<String, String> parseQueryParameters(String s) {
        final Pattern qComponentPattern = Pattern.compile("\\?=([^?#]*)");
        return parseComponentsFromMatcher(qComponentPattern.matcher(s));
    }

    private static String parseFragment(String s) {
        final Pattern fragment = Pattern.compile("#(.*)$");
        Matcher       matcher  = fragment.matcher(s);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static Map<String, String> parseComponentsFromMatcher(Matcher rComponentMatcher) {
        List<String> components = EMPTY_LIST;
        if (rComponentMatcher.find()) {
            final String params = rComponentMatcher.toMatchResult().group(1);
            components = Arrays.asList(params.split("&"));
        }

        Map<String, String> parameters = new HashMap<>();
        for (String rComponent : components) {
            final String[] kv  = rComponent.split("=");
            final String   key = kv[0];
            final String   val = (kv.length == 2) ? kv[1] : "";
            parameters.put(key, val);
        }

        return parameters;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RQFComponents) {
            RQFComponents that = (RQFComponents) obj;
            return this.resolutionParameters.equals(that.resolutionParameters)
                    && this.queryParameters.equals(that.queryParameters)
                    && this.fragment.equals(that.fragment);
        }
        return false;
    }

    @Override
    public String toString() {
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
