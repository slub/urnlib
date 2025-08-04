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

import java.util.regex.Pattern;

import static de.slub.urn.RFC.RFC_8141;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Represents a Namespace Identifier (NID) part of a Uniform Resource Identifier (URN) according to RFC 8141.
 *
 * {@code NamespaceIdentifier} instances are immutable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
public final class NID_RFC8141 extends NamespaceIdentifier {

    private static final Pattern allowedNID         = Pattern.compile("^[0-9a-z][0-9a-z-]{0,30}[0-9a-z]$", CASE_INSENSITIVE);
    private static final Pattern formalExclusionNID = Pattern.compile("^([a-z]{2}-{1,2}|X-).*", CASE_INSENSITIVE);
    private static final Pattern informalNID        = Pattern.compile("^(urn-[1-9][0-9]*).*", CASE_INSENSITIVE);

    /**
     * Creates a new {@link NID_RFC8141} instance.
     *
     * @param nid The Namespace Identifier literal
     * @throws URNSyntaxError if the given value is <pre>null</pre>, empty or invalid according to RFC8141.
     */
    public NID_RFC8141(String nid) throws URNSyntaxError {
        super(nid);
    }

    /**
     * @see NamespaceIdentifier#NamespaceIdentifier(NamespaceIdentifier)
     */
    public NID_RFC8141(NamespaceIdentifier instanceForCopying) {
        super(instanceForCopying);
    }

    /**
     * Return whether the NID is considered formal according to sections 5.1 and 5.2
     * of RFC 8141.
     * <p>
     * Note! Due to the definition of formal being based on exclusion, there are
     * NIDs which are both, not formal and not informal. For example the NID "xn--"
     * is not formal according to section 5.1 but yet it is not informal according to
     * section 5.2.
     *
     * @return True, if the NID is formal. False, otherwise.
     */
    public boolean isFormal() {
        return !formalExclusionNID.matcher(toString()).matches() && !isInformal();
    }

    /**
     * Return whether the NID is considered informal according to sections 5.1 and 5.2
     * of RFC 8141.
     * <p>
     * Note! Due to the definition of formal being based on exclusion, there are
     * NIDs which are both, not formal and not informal. For example the NID "xn--"
     * is not formal according to section 5.1 but yet it is not informal according to
     * section 5.2.
     *
     * @return True, if the NID is informal. False, otherwise.
     */
    public boolean isInformal() {
        return informalNID.matcher(toString()).matches();
    }

    /**
     * @see RFCSupport#supportedRFC()
     */
    @Override
    public RFC supportedRFC() {
        return RFC_8141;
    }

    /**
     * Check if a given literal is a valid namespace identifier according to RFC 8141
     *
     * @param nid Namespace identifier literal
     * @return Error message, if the given string violates the rules for valid namespace identifiers. Null, if not.
     */
    @Override
    protected String validateNamespaceIdentifier(String nid) {
        if (nid.length() < 2) {
            return String.format("Namespace Identifier '%s' is too short.", nid);
        }
        if (!allowedNID.matcher(nid).matches()) {
            return String.format("Not allowed characters in Namespace Identifier '%s'", nid);
        }
        return null;
    }

}
