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

import java.util.regex.Pattern;

import static de.slub.urn.RFC.RFC_8141;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class NID_RFC8141 extends NamespaceIdentifier {

    private static final Pattern allowedNID         = Pattern.compile("^[0-9a-z][0-9a-z-]{0,30}[0-9a-z]$", CASE_INSENSITIVE);
    private static final Pattern formalExclusionNID = Pattern.compile("^([a-z]{2}-{1,2}|X-).*", CASE_INSENSITIVE);
    private static final Pattern informalNID        = Pattern.compile("^(urn-[1-9][0-9]*).*", CASE_INSENSITIVE);

    public NID_RFC8141(String nid) throws URNSyntaxException {
        super(nid);
    }

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

    @Override
    protected boolean isValidNamespaceIdentifier(String nid) {
        return allowedNID.matcher(nid).matches();
    }

    @Override
    protected RFC supportedRFC() {
        return RFC_8141;
    }

}
