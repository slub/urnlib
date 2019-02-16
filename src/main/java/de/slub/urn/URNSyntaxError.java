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
 * An Exception which is thrown if a URN or any part of a URN cannot be parsed due to violations of the URN syntax.
 * <p>
 * It supports three different syntax errors and stores a reference to the RFC that has been violated.
 *
 * @author Ralf Claussnitzer
 */
public class URNSyntaxError extends Exception {
    private ErrorTypes error;
    private RFC        violatedRfc;

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
     * @see Exception#Exception(String)
     */
    public URNSyntaxError(String msg) {
        super(msg);
    }

    /**
     * @see Exception#Exception(String, Throwable)
     */
    public URNSyntaxError(String msg, Throwable t) {
        super(msg, t);
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

    public ErrorTypes getError() {
        return error;
    }

    public RFC getViolatedRfc() {
        return violatedRfc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s [Error %3d]\n\nThis is probably a violation of %s",
                getMessage(), error.getCode(), violatedRfc.name()));
        if (!error.getViolatedRfcSection().isEmpty()) {
            sb.append(", section ").append(error.getViolatedRfcSection());
        }
        sb.append("\nSee ").append(violatedRfc.url());
        return sb.toString();
    }

    public enum ErrorTypes {
        RESERVED(10, "2.1"),
        SYNTAX_ERROR(15, ""),
        LENGTH_ERROR(20, "2");

        private final int    code;
        private final String violatedRfcSection;

        ErrorTypes(int code, String violatedRfcSection) {
            this.code = code;
            this.violatedRfcSection = violatedRfcSection;
        }

        public int getCode() {
            return code;
        }

        public String getViolatedRfcSection() {
            return violatedRfcSection;
        }

    }

}
