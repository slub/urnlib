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

package de.slub.urn.fdc;

import de.slub.urn.NSS_RFC2141;
import de.slub.urn.URNSyntaxError;

import java.time.DateTimeException;
import java.time.LocalDate;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static de.slub.urn.URNSyntaxError.syntaxError;
import static de.slub.urn.fdc.FederatedContent.SPECIFICATION;

/**
 * Represents the name of a Federated Content URN ({@code urn:fdc:}), which RFC 4198 structures as follows:
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
 * The fields are kept as given. {@code FederatedContentName} instances are immutable.
 *
 * @author Ralf Claussnitzer
 * @see FederatedContent
 * @see <a href="https://tools.ietf.org/html/rfc4198">A Uniform Resource Name (URN) Namespace for Federated Content</a>
 */
public final class FederatedContentName {

    private final String providerId;
    private final String dateId;
    private final String resourceId;

    private FederatedContentName(String providerId, String dateId, String resourceId) {
        this.providerId = providerId;
        this.dateId = dateId;
        this.resourceId = resourceId;
    }

    /**
     * Read the fields of a name from a URL encoded Namespace Specific String literal.
     */
    static FederatedContentName parse(String encodedNss) throws URNSyntaxError {
        final String[] fields = encodedNss.split(":", 3);
        if (fields.length != 3) {
            throw syntaxError(SPECIFICATION, String.format(
                    "Namespace Specific String '%s' must have the form <ProviderId>:<DateId>:<ResourceId>",
                    encodedNss));
        }
        assertValidProviderId(fields[0]);
        assertValidDateId(fields[1]);
        assertValidResourceId(fields[2]);
        return new FederatedContentName(fields[0], fields[1], fields[2]);
    }

    static void assertValidProviderId(String providerId) throws URNSyntaxError {
        // ProviderId = 1*(label ".") toplabel
        final String[] labels = providerId.split("\\.", -1);
        boolean valid = labels.length >= 2;
        for (int i = 0; valid && i < labels.length; i++) {
            valid = isValidLabel(labels[i], i == labels.length - 1);
        }
        if (!valid) {
            throw syntaxError(SPECIFICATION, String.format("ProviderId '%s' is not a valid domain name", providerId));
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

    static void assertValidDateId(String dateId) throws URNSyntaxError {
        // DateId = CCYY [MM [DD]], the 1 to 3 digit forms are reserved
        if (!isDigits(dateId)) {
            throw syntaxError(SPECIFICATION, String.format("DateId '%s' must consist of digits only", dateId));
        }
        final int length = dateId.length();
        if (length <= 3) {
            throw syntaxError(SPECIFICATION, String.format(
                    "DateId '%s' uses a 1 to 3 digit form that is reserved for future use", dateId));
        }
        if (length != 4 && length != 6 && length != 8) {
            throw syntaxError(SPECIFICATION, String.format(
                    "DateId '%s' must have the form CCYY, CCYYMM or CCYYMMDD", dateId));
        }
        try {
            toLocalDate(dateId);
        } catch (DateTimeException e) {
            throw syntaxError(SPECIFICATION, String.format("DateId '%s' is not a valid calendar date", dateId));
        }
    }

    private static void assertValidResourceId(String resourceId) throws URNSyntaxError {
        // ResourceId = 1*(alphanum / other / ("%" hex hex)), which is what RFC 2141 allows in a Namespace Specific
        // String. So let the RFC 2141 type decide.
        boolean valid = !resourceId.isEmpty();
        if (valid) {
            try {
                new NSS_RFC2141(resourceId, URL_ENCODED);
            } catch (URNSyntaxError e) {
                valid = false;
            }
        }
        if (!valid) {
            throw syntaxError(SPECIFICATION,
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
     * Checks for equality with a given object. Two names are equal if their fields are equal, exactly as given.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is a {@code FederatedContentName} with equal fields.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FederatedContentName) && toString().equals(obj.toString());
    }

    /**
     * Calculates hash code based on the fields, consistent with {@link #equals(Object)}.
     *
     * @return Hash code of this name
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Return the name as it appears in the Namespace Specific String of a URN.
     *
     * @return {@code <ProviderId>:<DateId>:<ResourceId>}
     */
    @Override
    public String toString() {
        return providerId + ":" + dateId + ":" + resourceId;
    }

}
