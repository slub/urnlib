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

import java.util.Map;

final public class URN_8141 extends URN {

    protected final RFC supportedRfc = RFC.RFC_8141;

    private final NID_RFC8141   namespaceIdentifier;
    private final NSS_RFC8141   namespaceSpecificString;
    private final RQFComponents rqfComponents;

    URN_8141(NID_RFC8141 namespaceIdentifier, NSS_RFC8141 namespaceSpecificString, RQFComponents rqfComponents) {
        assertNotNull(namespaceIdentifier, "Namespace identifier cannot be null");
        assertNotNull(namespaceSpecificString, "Namespace specific string cannot be null");
        assertNotNull(rqfComponents, "Resolution-Query-Fragment component cannot be null");
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
        this.rqfComponents = rqfComponents;
    }

    public Map<String, String> resolutionParameters() {
        return rqfComponents.resolutionParameters();
    }

    public Map<String, String> queryParameters() {
        return rqfComponents.queryParameters();
    }

    public String fragment() {
        return rqfComponents.fragment();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN_8141)
                && namespaceIdentifier.equals(((URN_8141) obj).namespaceIdentifier)
                && namespaceSpecificString.equals(((URN_8141) obj).namespaceSpecificString)
                && supportedRfc.equals(((URN_8141) obj).supportedRfc);
    }

    /**
     * Return a URN serialization in URN syntax:
     * <pre>&lt;URN&gt; ::= &quot;urn:&quot; &lt;NID&gt; &quot;:&quot; &lt;NSS&gt;</pre>
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return String.format("%s:%s:%s%s", SCHEME, namespaceIdentifier, namespaceSpecificString, rqfComponents);
    }

    @Override
    public String namespaceIdentifier() {
        return namespaceIdentifier.toString();
    }

    @Override
    public String namespaceSpecificString() {
        return namespaceSpecificString.toString();
    }

    @Override
    public RFC supportedRfc() {
        return supportedRfc;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new IllegalArgumentException(message);
        }
    }

}
