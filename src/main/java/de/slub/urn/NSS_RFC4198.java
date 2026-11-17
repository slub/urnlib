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

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Locale;

import static de.slub.urn.RFC.RFC_4198;
import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents the Namespace Specific String (NSS) of a Federated Content URN ({@code urn:fdc:}) according to RFC 4198.
 * <p>
 * RFC 4198 gives the Namespace Specific String the following structure:
 * <pre>
 * NSS        = ProviderId ":" DateId ":" ResourceId
 * ProviderId = 1*(label ".") toplabel
 * DateId     = CCYY [MM [DD]]
 * ResourceId = 1*(alphanum / other / ("%" hex hex))
 * </pre>
 * The ResourceId may contain colons, so only the first two colons delimit the fields. The one to three digit DateId
 * forms that RFC 4198 reserves for future use are rejected, as are DateIds that do not denote an existing calendar
 * date.
 * <p>
 * On top of the equivalence rules for every Namespace Specific String, the case of the ProviderId is ignored by
 * {@link #isEquivalentTo(NamespaceSpecificString)} and is lower case in the normal form, as defined by RFC 4198,
 * section 2. {@code equals()} compares the literal exactly, like for every Namespace Specific String.
 * <p>
 * {@code NSS_RFC4198} instances are immutable.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc4198">A Uniform Resource Name (URN) Namespace for Federated Content</a>
 */
public final class NSS_RFC4198 extends NamespaceSpecificString {

    private final String providerId;
    private final String dateId;
    private final String resourceId;

    /**
     * Creates a new {@code NamespaceSpecificString} instance according to RFC 4198.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxError Thrown if the given literal is not valid according to RFC 4198
     */
    public NSS_RFC4198(String nss, Encoding encoding) throws URNSyntaxError {
        super(nss, encoding);
        final String encoded = toString();
        final String[] fields = encoded.split(":", 3);
        if (fields.length != 3) {
            throw syntaxError(RFC_4198, String.format(
                    "Namespace Specific String '%s' must have the form <ProviderId>:<DateId>:<ResourceId>", encoded));
        }
        assertValidProviderId(fields[0]);
        assertValidDateId(fields[1]);
        assertValidResourceId(fields[2]);
        providerId = fields[0];
        dateId = fields[1];
        resourceId = fields[2];
    }

    /**
     * Create a new Namespace Specific String from its fields.
     * <p>
     * The ResourceId is taken as not encoded: characters that are not allowed literally get percent-encoded.
     *
     * @param providerId The ProviderId, a domain name
     * @param dateId     The DateId in ISO 8601 basic format ({@code CCYY}, {@code CCYYMM} or {@code CCYYMMDD})
     * @param resourceId The not encoded ResourceId
     * @return The new Namespace Specific String
     * @throws URNSyntaxError           if any of the fields is not valid according to RFC 4198
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public static NSS_RFC4198 of(String providerId, String dateId, String resourceId) throws URNSyntaxError {
        if (providerId == null || dateId == null || resourceId == null) {
            throw new IllegalArgumentException("ProviderId, DateId and ResourceId cannot be null");
        }
        // Validate ProviderId and DateId before encoding, so that they do not get percent-encoded silently
        assertValidProviderId(providerId);
        assertValidDateId(dateId);
        if (resourceId.isEmpty()) {
            throw syntaxError(RFC_4198, "ResourceId cannot be empty");
        }
        return new NSS_RFC4198(providerId + ":" + dateId + ":" + resourceId, Encoding.NOT_ENCODED);
    }

    private static void assertValidProviderId(String providerId) throws URNSyntaxError {
        // ProviderId = 1*(label ".") toplabel
        final String[] labels = providerId.split("\\.", -1);
        boolean valid = labels.length >= 2;
        for (int i = 0; valid && i < labels.length; i++) {
            valid = isValidLabel(labels[i], i == labels.length - 1);
        }
        if (!valid) {
            throw syntaxError(RFC_4198, String.format("ProviderId '%s' is not a valid domain name", providerId));
        }
    }

    // label    = alphanum / alphanum *(alphanum / "-") alphanum
    // toplabel = ALPHA / ALPHA *(alphanum / "-") alphanum
    private static boolean isValidLabel(String label, boolean isTopLabel) {
        if (label.isEmpty() || label.charAt(label.length() - 1) == '-') {
            return false;
        }
        final char first = label.charAt(0);
        if (isTopLabel ? !isAlpha(first) : !isAlphanum(first)) {
            return false;
        }
        for (int i = 1; i < label.length(); i++) {
            final char c = label.charAt(i);
            if (!isAlphanum(c) && c != '-') {
                return false;
            }
        }
        return true;
    }

    private static void assertValidDateId(String dateId) throws URNSyntaxError {
        // DateId = CCYY [MM [DD]], the 1 to 3 digit forms are reserved
        if (!isDigits(dateId)) {
            throw syntaxError(RFC_4198, String.format("DateId '%s' must consist of digits only", dateId));
        }
        final int length = dateId.length();
        if (length <= 3) {
            throw syntaxError(RFC_4198, String.format(
                    "DateId '%s' uses a 1 to 3 digit form that is reserved for future use", dateId));
        }
        if (length != 4 && length != 6 && length != 8) {
            throw syntaxError(RFC_4198, String.format(
                    "DateId '%s' must have the form CCYY, CCYYMM or CCYYMMDD", dateId));
        }
        try {
            toLocalDate(dateId);
        } catch (DateTimeException e) {
            throw syntaxError(RFC_4198, String.format("DateId '%s' is not a valid calendar date", dateId));
        }
    }

    private static void assertValidResourceId(String resourceId) throws URNSyntaxError {
        // ResourceId = 1*(alphanum / other / ("%" hex hex)), where alphanum and other are the RFC 2141 NSS literals
        if (resourceId.isEmpty() || !PercentEncoding.isValid(resourceId, Parsing::isRfc2141NssLiteral, false)) {
            throw syntaxError(RFC_4198,
                    String.format("ResourceId '%s' contains characters not allowed by RFC 4198", resourceId));
        }
    }

    private static LocalDate toLocalDate(String dateId) {
        // Missing month and day default to 01
        final int year = Integer.parseInt(dateId.substring(0, 4));
        final int month = dateId.length() >= 6 ? Integer.parseInt(dateId.substring(4, 6)) : 1;
        final int day = dateId.length() == 8 ? Integer.parseInt(dateId.substring(6, 8)) : 1;
        return LocalDate.of(year, month, day);
    }

    private static boolean isDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlphanum(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Return the ProviderId, the domain name of the organization that allocated the URN.
     *
     * @return The ProviderId as given, not case-normalized
     */
    public String providerId() {
        return providerId;
    }

    /**
     * Return the DateId literal.
     *
     * @return The DateId as given ({@code CCYY}, {@code CCYYMM} or {@code CCYYMMDD})
     */
    public String dateId() {
        return dateId;
    }

    /**
     * Return the DateId as date. A missing month or day defaults to {@code 01}, as defined by RFC 4198.
     *
     * @return The date denoted by the DateId
     */
    public LocalDate date() {
        return toLocalDate(dateId);
    }

    /**
     * Return the ResourceId in its percent-encoded form, as it appears in the Namespace Specific String.
     *
     * @return The encoded ResourceId
     */
    public String resourceId() {
        return resourceId;
    }

    /**
     * Check if a given URL encoded NSS literal consists of characters that are allowed in an RFC 8141 NSS. The
     * structure defined by RFC 4198 is checked on construction.
     *
     * @param encoded The URL encoded NSS literal
     * @return True, if the literal is syntactically valid.
     */
    @Override
    protected boolean isValidURLEncodedNamespaceSpecificString(String encoded) {
        return !encoded.startsWith("/")
                && PercentEncoding.isValid(encoded, c -> c == '/' || Parsing.isPchar(c), true);
    }

    /**
     * Compute the normal form: on top of the general rules, the ProviderId is lower case (RFC 4198, section 2).
     *
     * @see NamespaceSpecificString#normalForm(String)
     */
    @Override
    protected String normalForm(String encoded) {
        final String normalized = super.normalForm(encoded);
        final int endOfProviderId = normalized.indexOf(':');
        if (endOfProviderId < 0) {
            return normalized;
        }
        return normalized.substring(0, endOfProviderId).toLowerCase(Locale.ROOT)
                + normalized.substring(endOfProviderId);
    }

    /**
     * @see NamespaceSpecificString#normalize()
     */
    @Override
    public NSS_RFC4198 normalize() {
        try {
            return new NSS_RFC4198(normalizedLiteral(), Encoding.URL_ENCODED);
        } catch (URNSyntaxError e) {
            throw new IllegalStateException("Normal form of a valid Namespace Specific String is not valid", e);
        }
    }

    /**
     * @see NamespaceSpecificString#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC_4198;
    }

}
