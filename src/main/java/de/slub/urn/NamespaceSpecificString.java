/*
 * Copyright (C) 2016 Saxon State and University Library Dresden (SLUB)
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

public class NamespaceSpecificString {

    private static final Pattern allowedNSS = Pattern.compile("^([0-9a-zA-Z()+,-.:=@;$_!*']|(%[0-9a-fA-F]{2}))+$");

    public static void validateNamespaceSpecificString(String nss) throws URNSyntaxException {
        if (!allowedNSS.matcher(nss).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Specific String '%s'", nss));
        }
    }

}
