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

import de.slub.urn.NamespaceSpecificString.Encoding;

import java.net.URI;

import static de.slub.urn.NamespaceIdentifier.URN_SCHEME;
import static de.slub.urn.NamespaceSpecificString.Encoding.NOT_ENCODED;
import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;

public class URNFactory {

    private final RFC supportedRFC;

    public URNFactory(RFC supportedRFC) {
        this.supportedRFC = supportedRFC;
    }

    /**
     * Create a new URN instance using a particular Namespace Identifier and Namespace Specific String.
     * <p>
     * Syntax rules for URNs apply as described in RFC2141
     *
     * @param namespaceIdentifier     The URNs Namespace Identifier literal
     * @param namespaceSpecificString The URNs Namespace Specific String literal
     * @return A new URN instance
     * @throws IllegalArgumentException If the given string cannot be parsed into a URN
     */
    public URN create(String namespaceIdentifier, String namespaceSpecificString) {
        try {
            final NamespaceIdentifier     nid = getNidInstance(namespaceIdentifier);
            final NamespaceSpecificString nss = getNssInstance(namespaceSpecificString, NOT_ENCODED);
            return new URN(nid, nss);
        } catch (URNSyntaxException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Error creating URN instance", e);
        }
    }

    private NamespaceIdentifier getNidInstance(String namespaceIdentifier) throws URNSyntaxException {
        switch (supportedRFC) {
            case RFC_2141:
                return new NID_RFC2141(namespaceIdentifier);
            case RFC_8141:
                return new NID_RFC8141(namespaceIdentifier);
            default:
                throw new IllegalStateException("URN factory initialized with unsupported RFC: " + supportedRFC);
        }
    }

    private NamespaceSpecificString getNssInstance(String namespaceSpecificString, Encoding encoding) throws URNSyntaxException {
        switch (supportedRFC) {
            case RFC_2141:
                return new NSS_RFC2141(namespaceSpecificString, encoding);
            case RFC_8141:
                return new NSS_RFC8141(namespaceSpecificString, encoding);
            default:
                throw new IllegalStateException("URN factory initialized with unsupported RFC: " + supportedRFC);
        }
    }

    /**
     * Create a new URN instance by parsing a URN serialisation.
     *
     * @param urn String to be parsed into an URN instance with
     * @return New URN instance
     * @throws IllegalArgumentException If the given string cannot be parsed into a URN
     */
    public URN create(String urn) {
        if ((urn == null) || (urn.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = urn.split(":");

        try {
            if (parts.length < 3 || !URN_SCHEME.equalsIgnoreCase(parts[0])) {
                throw new URNSyntaxException(
                        String.format("Invalid format `%s` is probably not a URN", urn));
            }

            final NamespaceIdentifier     namespaceIdentifier     = getNidInstance(parts[1]);
            final String                  encodedNSSPart          = urn.substring(urn.indexOf(parts[1]) + parts[1].length() + 1);
            final NamespaceSpecificString namespaceSpecificString = getNssInstance(encodedNSSPart, URL_ENCODED);

            return new URN(namespaceIdentifier, namespaceSpecificString);
        } catch (URNSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Constructing a new URN instance by parsing URN parts from a URI instance.
     *
     * @param uri The URI to be parsed into a URN
     * @return A new URN instance
     * @throws IllegalArgumentException If the given URI cannot be parsed into a URN
     */
    public URN create(URI uri) {
        final String scheme = uri.getScheme();
        try {
            if (!URN_SCHEME.equalsIgnoreCase(scheme)) {
                throw new URNSyntaxException(
                        String.format("Invalid scheme: `%s` - Given URI is not a URN", scheme));
            }

            final String schemeSpecificPart = uri.getSchemeSpecificPart();
            int          colonPos           = schemeSpecificPart.indexOf(':');
            if (colonPos > -1) {
                return new URN(
                        getNidInstance(schemeSpecificPart.substring(0, colonPos)),
                        getNssInstance(schemeSpecificPart.substring(colonPos + 1), URL_ENCODED));
            } else {
                throw new URNSyntaxException(
                        String.format("Invalid format: `%s` - Given schema specific part is not a URN part", schemeSpecificPart));
            }
        } catch (URNSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
