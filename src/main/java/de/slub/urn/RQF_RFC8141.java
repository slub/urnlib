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

import java.util.Map;
import java.util.Objects;

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents the optional resolution (r), query (q) and fragment (f) components of a URN according to RFC 8141.
 * <p>
 * The r- and q-components are modelled as {@link RQComponent}s, the f-component as a string. All of them are kept
 * in percent-encoded form. Instances are immutable.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8141#section-2.3">RFC 8141, 2.3 Optional Components</a>
 */
public final class RQF_RFC8141 {

    /**
     * Instance representing the absence of RQF components.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Null_object_pattern">Null object pattern</a>
     */
    public static final RQF_RFC8141 NULL = createNull();

    private final RQComponent resolutionComponent;
    private final RQComponent queryComponent;
    private final String      fragment;
    private final String      stringRepresentation;

    /**
     * Construct RQF components.
     * <p>
     * Empty components are omitted from the string representation.
     *
     * @param resolutionComponent The r-component, use {@link RQComponent#EMPTY} if absent
     * @param queryComponent      The q-component, use {@link RQComponent#EMPTY} if absent
     * @param fragment            The percent-encoded f-component, use an empty string if absent
     * @throws IllegalArgumentException if any of the parameters is null
     * @throws URNSyntaxError           if the r-component contains {@code ?=}, which would end it prematurely,
     *                                  or the fragment contains characters not allowed by RFC 3986
     */
    public RQF_RFC8141(RQComponent resolutionComponent, RQComponent queryComponent, String fragment)
            throws URNSyntaxError {
        if (resolutionComponent == null) {
            throw new IllegalArgumentException("Resolution component cannot be null");
        }
        if (queryComponent == null) {
            throw new IllegalArgumentException("Query component cannot be null");
        }
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment string cannot be null");
        }
        if (resolutionComponent.toString().contains("?=")) {
            throw syntaxError(RFC.RFC_8141, String.format(
                    "r-component `%s` must not contain `?=`, which would start the q-component", resolutionComponent));
        }
        // f-component = fragment = *( pchar / "/" / "?" ), see RFC 3986, 3.5
        if (!Parsing.isValidRqfComponentContent(fragment)) {
            throw syntaxError(RFC.RFC_8141, String.format("Not allowed characters in fragment `%s`", fragment));
        }
        this.resolutionComponent = resolutionComponent;
        this.queryComponent = queryComponent;
        this.fragment = fragment;
        this.stringRepresentation = initialToString();
    }

    private static RQF_RFC8141 createNull() {
        try {
            return new RQF_RFC8141(RQComponent.EMPTY, RQComponent.EMPTY, "");
        } catch (URNSyntaxError e) {
            throw new IllegalStateException("Cannot create empty RQF components", e);
        }
    }

    /**
     * Parse the RQF part of a URN literal, i.e. everything following the Namespace Specific String.
     * <p>
     * Components are recognized in the order defined by RFC 8141:
     * {@code [ "?+" r-component ] [ "?=" q-component ] [ "#" f-component ]}. The f-component starts at the first
     * {@code #}, the r-component ends at the first {@code ?=}. Within the q- and f-components, {@code ?+} and
     * {@code ?=} are data.
     *
     * @param rqf The RQF part, either empty or starting with {@code ?+}, {@code ?=} or {@code #}
     * @return The parsed components
     * @throws URNSyntaxError if the components are not in the defined order or contain invalid characters
     */
    static RQF_RFC8141 parse(String rqf) throws URNSyntaxError {
        if (rqf.isEmpty()) {
            return NULL;
        }
        String rest = rqf;
        String fragment = "";
        final int hash = rest.indexOf('#');
        if (hash >= 0) {
            fragment = rest.substring(hash + 1);
            rest = rest.substring(0, hash);
        }
        String rComponent = "";
        if (rest.startsWith("?+")) {
            final int q = rest.indexOf("?=", 2);
            rComponent = q < 0 ? rest.substring(2) : rest.substring(2, q);
            rest = q < 0 ? "" : rest.substring(q);
        }
        String qComponent = "";
        if (rest.startsWith("?=")) {
            qComponent = rest.substring(2);
            rest = "";
        }
        if (!rest.isEmpty()) {
            throw syntaxError(RFC.RFC_8141, String.format(
                    "Invalid optional components `%s`, expected order is `?+` r-component, `?=` q-component, " +
                            "`#` f-component", rqf));
        }
        return new RQF_RFC8141(RQComponent.parse(rComponent), RQComponent.parse(qComponent), fragment);
    }

    /**
     * Return the r-component.
     *
     * @return The r-component, {@link RQComponent#EMPTY} if absent
     */
    public RQComponent resolutionComponent() {
        return resolutionComponent;
    }

    /**
     * Return the q-component.
     *
     * @return The q-component, {@link RQComponent#EMPTY} if absent
     */
    public RQComponent queryComponent() {
        return queryComponent;
    }

    /**
     * Return a simplified map view of the r-component.
     *
     * @return Map of resolution parameters
     * @see RQComponent#toMap()
     */
    public Map<String, String> resolutionParameters() {
        return resolutionComponent.toMap();
    }

    /**
     * Return a simplified map view of the q-component.
     *
     * @return Map of query parameters
     * @see RQComponent#toMap()
     */
    public Map<String, String> queryParameters() {
        return queryComponent.toMap();
    }

    /**
     * Return the fragment component string.
     *
     * @return Percent-encoded f-component, empty if absent
     */
    public String fragment() {
        return fragment;
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two RQF objects are considered equal if their r-components, q-components and fragments are equal.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code RQF_RFC8141} instance and is equivalent to this instance.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RQF_RFC8141) {
            final RQF_RFC8141 that = (RQF_RFC8141) obj;
            return this.resolutionComponent.equals(that.resolutionComponent)
                    && this.queryComponent.equals(that.queryComponent)
                    && this.fragment.equals(that.fragment);
        }
        return false;
    }

    /**
     * Calculates hash code consistent with {@link #equals(Object)}.
     *
     * @return Hash code of this instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(resolutionComponent, queryComponent, fragment);
    }

    /**
     * Return the r-, q- and f-components in URN syntax, including their introducing sequences.
     *
     * @return RQF string, empty if all components are empty
     */
    @Override
    public String toString() {
        return stringRepresentation;
    }

    private String initialToString() {
        final StringBuilder sb = new StringBuilder();
        if (!resolutionComponent.isEmpty()) {
            sb.append("?+").append(resolutionComponent);
        }
        if (!queryComponent.isEmpty()) {
            sb.append("?=").append(queryComponent);
        }
        if (!fragment.isEmpty()) {
            sb.append('#').append(fragment);
        }
        return sb.toString();
    }
}
