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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents an r-component or a q-component of a URN according to RFC 8141 as an ordered list of parameters.
 * <p>
 * RFC 8141 defines both components as {@code pchar *( pchar / "/" / "?" )} without imposing further structure.
 * This class reads them using the common {@code name=value&name=value} convention in a lossless way:
 * <ul>
 *     <li>parameters are separated by {@code &} and keep their order,</li>
 *     <li>names may occur more than once,</li>
 *     <li>a parameter is split at its <em>first</em> {@code =}, so values may contain {@code =},</li>
 *     <li>a parameter without {@code =} has no value, which is different from an empty value ({@code name=}),</li>
 *     <li>empty parameters (as in {@code a&&b}) are kept.</li>
 * </ul>
 * Hence {@link #parse(String)} and {@link #toString()} are inverse to each other: every valid component string
 * is reproduced exactly, and every instance built with {@link #builder()} parses back into an equal instance.
 * <p>
 * Names and values are kept in their percent-encoded form.
 * <p>
 * {@code RQComponent} instances are immutable.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8141#section-2.3">RFC 8141, 2.3 Optional Components</a>
 */
public final class RQComponent implements Iterable<RQComponent.Parameter> {

    /**
     * Instance representing an absent or empty component.
     */
    public static final RQComponent EMPTY = new RQComponent(Collections.emptyList());

    private final List<Parameter> parameters;
    private final String          stringRepresentation;

    private RQComponent(List<Parameter> parameters) {
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.stringRepresentation = serialize(this.parameters);
    }

    /**
     * Parse a component string, not including the introducing {@code ?+} or {@code ?=} sequence.
     *
     * @param component The component string
     * @return The parsed component; {@link #EMPTY} for an empty string
     * @throws URNSyntaxError           if the string contains characters not allowed by RFC 8141
     * @throws IllegalArgumentException if the argument is null
     */
    public static RQComponent parse(String component) throws URNSyntaxError {
        if (component == null) {
            throw new IllegalArgumentException("Component string cannot be null");
        }
        if (component.isEmpty()) {
            return EMPTY;
        }
        validateComponent(component);
        final List<Parameter> parameters = new ArrayList<>();
        for (String segment : component.split("&", -1)) {
            final int eq = segment.indexOf('=');
            parameters.add(eq < 0
                    ? new Parameter(segment, null)
                    : new Parameter(segment.substring(0, eq), segment.substring(eq + 1)));
        }
        return new RQComponent(parameters);
    }

    /**
     * Create a builder for constructing a component parameter by parameter.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private static String serialize(List<Parameter> parameters) {
        final StringBuilder sb = new StringBuilder();
        for (Iterator<Parameter> it = parameters.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    private static void validateComponent(String component) throws URNSyntaxError {
        // r-component / q-component = pchar *( pchar / "/" / "?" )
        if (component.startsWith("/") || component.startsWith("?")) {
            throw syntaxError(RFC.RFC_8141,
                    String.format("Component `%s` must not start with `/` or `?`", component));
        }
        if (!Parsing.isValidRqfComponentContent(component)) {
            throw syntaxError(RFC.RFC_8141,
                    String.format("Not allowed characters in component `%s`", component));
        }
    }

    /**
     * Return all parameters in order of appearance.
     *
     * @return Unmodifiable list of parameters
     */
    public List<Parameter> parameters() {
        return parameters;
    }

    @Override
    public Iterator<Parameter> iterator() {
        return parameters.iterator();
    }

    /**
     * Tell whether this component has no parameters.
     *
     * @return True, if there are no parameters
     */
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    /**
     * Tell whether there is at least one parameter with the given name.
     *
     * @param name Parameter name
     * @return True, if a parameter with this name exists
     */
    public boolean contains(String name) {
        return parameters.stream().anyMatch(p -> p.name.equals(name));
    }

    /**
     * Return the value of the first parameter with the given name.
     *
     * @param name Parameter name
     * @return The value; empty if there is no such parameter or it has no value
     */
    public Optional<String> firstValue(String name) {
        return parameters.stream()
                .filter(p -> p.name.equals(name))
                .findFirst()
                .flatMap(Parameter::value);
    }

    /**
     * Return the values of all parameters with the given name, in order of appearance.
     * Parameters without a value are skipped.
     *
     * @param name Parameter name
     * @return Unmodifiable list of values
     */
    public List<String> values(String name) {
        final List<String> values = new ArrayList<>();
        for (Parameter p : parameters) {
            if (p.name.equals(name) && p.value != null) {
                values.add(p.value);
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Return all distinct parameter names in order of first appearance.
     *
     * @return Unmodifiable set of names
     */
    public Set<String> names() {
        final Set<String> names = new LinkedHashSet<>();
        for (Parameter p : parameters) {
            names.add(p.name);
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * Return a simplified view as a map from parameter name to the value of its first occurrence.
     * <p>
     * The view is lossy: later occurrences of a name are not included, and a missing value is represented as an
     * empty string. Use {@link #parameters()} for the exact content.
     *
     * @return Unmodifiable map ordered by first appearance of each name
     */
    public Map<String, String> toMap() {
        final Map<String, String> map = new LinkedHashMap<>();
        for (Parameter p : parameters) {
            map.putIfAbsent(p.name, p.value == null ? "" : p.value);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Return the component string, not including the introducing {@code ?+} or {@code ?=} sequence.
     *
     * @return The component string
     */
    @Override
    public String toString() {
        return stringRepresentation;
    }

    /**
     * Two components are equal if they have the same parameters in the same order, which is the case exactly
     * if their string representations are equal.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is an equal {@code RQComponent}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RQComponent && parameters.equals(((RQComponent) obj).parameters);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode();
    }

    /**
     * A single parameter of an r- or q-component.
     */
    public static final class Parameter {
        private final String name;
        private final String value;

        private Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Return the parameter name, i.e. the part before the first {@code =}.
         *
         * @return Parameter name, possibly empty
         */
        public String name() {
            return name;
        }

        /**
         * Return the parameter value, i.e. the part after the first {@code =}.
         *
         * @return Parameter value; empty if the parameter has no {@code =} at all
         */
        public Optional<String> value() {
            return Optional.ofNullable(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Parameter)) {
                return false;
            }
            final Parameter that = (Parameter) obj;
            return name.equals(that.name) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        /**
         * Return the parameter in component syntax, either {@code name} or {@code name=value}.
         *
         * @return Parameter string
         */
        @Override
        public String toString() {
            return value == null ? name : name + '=' + value;
        }
    }

    /**
     * Builder for {@link RQComponent} instances. Names and values have to be given in percent-encoded form.
     */
    public static final class Builder {
        private final List<Parameter> parameters = new ArrayList<>();

        private Builder() {
        }

        /**
         * Add a parameter without value, serialized as {@code name}.
         *
         * @param name Parameter name
         * @return This builder
         * @throws URNSyntaxError           if the name contains {@code =}, {@code &} or characters not allowed
         *                                  by RFC 8141
         * @throws IllegalArgumentException if the name is null
         */
        public Builder add(String name) throws URNSyntaxError {
            return addParameter(name, null);
        }

        /**
         * Add a parameter with value, serialized as {@code name=value}.
         *
         * @param name  Parameter name
         * @param value Parameter value, may be empty and may contain {@code =}
         * @return This builder
         * @throws URNSyntaxError           if the name contains {@code =} or {@code &}, the value contains
         *                                  {@code &}, or either contains characters not allowed by RFC 8141
         * @throws IllegalArgumentException if name or value is null
         */
        public Builder add(String name, String value) throws URNSyntaxError {
            if (value == null) {
                throw new IllegalArgumentException("Parameter value cannot be null");
            }
            return addParameter(name, value);
        }

        private Builder addParameter(String name, String value) throws URNSyntaxError {
            if (name == null) {
                throw new IllegalArgumentException("Parameter name cannot be null");
            }
            if (name.indexOf('=') >= 0 || name.indexOf('&') >= 0) {
                throw syntaxError(RFC.RFC_8141,
                        String.format("Parameter name `%s` must not contain `=` or `&`", name));
            }
            if (value != null && value.indexOf('&') >= 0) {
                throw syntaxError(RFC.RFC_8141,
                        String.format("Parameter value `%s` must not contain `&`", value));
            }
            final Parameter parameter = new Parameter(name, value);
            if (!Parsing.isValidRqfComponentContent(parameter.toString())) {
                throw syntaxError(RFC.RFC_8141,
                        String.format("Not allowed characters in parameter `%s`", parameter));
            }
            parameters.add(parameter);
            return this;
        }

        /**
         * Build the component.
         *
         * @return The new component
         * @throws URNSyntaxError if the resulting component is not valid according to RFC 8141, e.g. because it
         *                        starts with {@code /} or {@code ?}, or consists of a single empty parameter which
         *                        could not be distinguished from an empty component
         */
        public RQComponent build() throws URNSyntaxError {
            if (parameters.isEmpty()) {
                return EMPTY;
            }
            final RQComponent component = new RQComponent(parameters);
            if (component.stringRepresentation.isEmpty()) {
                throw syntaxError(RFC.RFC_8141, "A component cannot consist of a single empty parameter");
            }
            validateComponent(component.stringRepresentation);
            return component;
        }
    }
}
