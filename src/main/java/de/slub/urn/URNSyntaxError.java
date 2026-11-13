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

/**
 * An Exception which is thrown if a URN or any part of a URN cannot be parsed due to violations of the URN syntax.
 * <p>
 * It supports three different syntax errors and stores a reference to the RFC that has been violated.
 *
 * @author Ralf Claussnitzer
 */
public class URNSyntaxError extends Exception {
    /**
     * The type of the identified violation, {@code null} if not specified.
     */
    private final ErrorTypes error;
    /**
     * The violated RFC, {@code null} if not specified.
     */
    private final RFC        violatedRfc;

    /**
     * Construct a new syntax error exception.
     *
     * @param msg         Error message
     * @param error       The identified violation error
     * @param violatedRfc The violated RFC
     */
    public URNSyntaxError(String msg, ErrorTypes error, RFC violatedRfc) {
        super(msg);
        this.error = error;
        this.violatedRfc = violatedRfc;
    }

    /**
     * Construct a new syntax error exception without error type and violated RFC.
     *
     * @param msg Error message
     * @see Exception#Exception(String)
     */
    public URNSyntaxError(String msg) {
        super(msg);
        this.error = null;
        this.violatedRfc = null;
    }

    /**
     * Construct a new syntax error exception with a cause, but without error type and violated RFC.
     *
     * @param msg Error message
     * @param t   The cause of this error
     * @see Exception#Exception(String, Throwable)
     */
    public URNSyntaxError(String msg, Throwable t) {
        super(msg, t);
        this.error = null;
        this.violatedRfc = null;
    }

    /**
     * Create a new syntax error representing a `reserved identifier` error.
     *
     * @param rfc The violated RFC
     * @param nid The namespace identifier
     * @return URNSyntaxError exception for throwing
     */
    public static URNSyntaxError reservedIdentifier(RFC rfc, String nid) {
        return new URNSyntaxError(
                String.format("Namespace identifier can not be '%s'", nid),
                ErrorTypes.RESERVED,
                rfc);
    }

    /**
     * Create a new syntax error representing a `syntactically invalid identifier` error.
     *
     * @param rfc The violated RFC
     * @param msg A detailed error message
     * @return URNSyntaxError exception for throwing
     */
    public static URNSyntaxError syntaxError(RFC rfc, String msg) {
        return new URNSyntaxError(msg, ErrorTypes.SYNTAX_ERROR, rfc);
    }

    /**
     * Create a new syntax error representing a `length` error.
     *
     * @param rfc The violated RFC
     * @param nid The namespace identifier
     * @return URNSyntaxError exception for throwing
     */
    public static URNSyntaxError lengthError(RFC rfc, String nid) {
        return new URNSyntaxError(
                String.format("Namespace Identifier '%s' is too long. Only 32 characters are allowed.", nid),
                ErrorTypes.LENGTH_ERROR,
                rfc);
    }

    /**
     * Return the type of the identified violation.
     *
     * @return The error type, or {@code null} if this error was created without one
     */
    public ErrorTypes getError() {
        return error;
    }

    /**
     * Return the RFC that has been violated.
     *
     * @return The violated RFC, or {@code null} if this error was created without one
     */
    public RFC getViolatedRfc() {
        return violatedRfc;
    }

    @Override
    public String toString() {
        if (error == null || violatedRfc == null) {
            return super.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s [Error %3d]\n\nThis is probably a violation of %s",
                getMessage(), error.getCode(), violatedRfc.name()));
        if (!error.getViolatedRfcSection().isEmpty()) {
            sb.append(", section ").append(error.getViolatedRfcSection());
        }
        sb.append("\nSee ").append(violatedRfc.url());
        return sb.toString();
    }

    /**
     * Types of URN syntax violations, each with a numeric error code and the RFC section that defines the violated
     * rule.
     */
    public enum ErrorTypes {
        /**
         * The Namespace Identifier is reserved and must not be used.
         */
        RESERVED(10, "2.1"),
        /**
         * The URN or one of its parts is syntactically invalid.
         */
        SYNTAX_ERROR(15, ""),
        /**
         * The Namespace Identifier exceeds the maximum length.
         */
        LENGTH_ERROR(20, "2");

        private final int    code;
        private final String violatedRfcSection;

        ErrorTypes(int code, String violatedRfcSection) {
            this.code = code;
            this.violatedRfcSection = violatedRfcSection;
        }

        /**
         * Return the numeric error code.
         *
         * @return Error code
         */
        public int getCode() {
            return code;
        }

        /**
         * Return the section of the RFC that defines the violated rule.
         *
         * @return RFC section number, or an empty string if no specific section applies
         */
        public String getViolatedRfcSection() {
            return violatedRfcSection;
        }

    }

}
