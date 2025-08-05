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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Implements a parser for URNs according to RFC 8141.
 *
 * @author Ralf Claussnitzer
 * @see URN_8141
 */
public class RFC8141Parser implements URNParser<URN_8141> {

    /**
     * @see URNParser#parse(String)
     */
    @Override
    public URN_8141 parse(String urnLiteral) throws URNSyntaxError {
        if ((urnLiteral == null) || (urnLiteral.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = urnLiteral.split(":");

        if (parts.length < 3 || !URN.SCHEME.equalsIgnoreCase(parts[0])) {
            throw syntaxError(RFC.RFC_8141,
                    String.format("Invalid format `%s` is probably not a URN", urnLiteral));
        }

        // NID is the first part after "urn:"
        final NID_RFC8141 nid = new NID_RFC8141(parts[1]);

        // assemble rest of the parts for NSS parsing
        final String schemeSpecificPart = urnLiteral.substring(urnLiteral.indexOf(parts[1]) + parts[1].length() + 1);

        // copy NSS part by cutting out RQF components from NSS
        final String nssString = substringUntilAny(schemeSpecificPart, "?+", "?=", "#");
        final NSS_RFC8141 nss = new NSS_RFC8141(nssString, URL_ENCODED);

        final String rqfComponentsString = schemeSpecificPart.substring(nssString.length());
        final RQF_RFC8141 rqfComponents = parseRQFComponents(rqfComponentsString);

        return new URN_8141(nid, nss, rqfComponents);
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_8141 parse(URI uri) throws URNSyntaxError {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return parse(uri.toASCIIString());
    }

    private RQF_RFC8141 parseRQFComponents(String rqfComponents) {
        final String rComponent = substringUntilAny(substringFrom(rqfComponents, "?+"), "?=", "#");
        final String qComponent = substringUntilAny(substringFrom(rqfComponents, "?="), "#");
        final String fComponent = substringFrom(rqfComponents, "#");
        return new RQF_RFC8141(parseParameters(rComponent), parseParameters(qComponent), fComponent);
    }

    private String substringFrom(String str, String startDelimiter) {
        final int startIndex = str.indexOf(startDelimiter);
        if (startIndex < 0) {
            return ""; // Default to empty string if no occurrence of start delimiter is found.
        }
        return str.substring(startIndex + startDelimiter.length());
    }

    private String substringUntilAny(String str, String... endDelimiters) {
        int endIndex = str.length(); // Default to whole string if no occurrence of end delimiter is found.
        for (String endDelimiter : endDelimiters) {
            int currentEnd = str.indexOf(endDelimiter);
            if (currentEnd >= 0 && currentEnd < endIndex) {
                endIndex = currentEnd;
            }
        }
        return str.substring(0, endIndex);
    }

    private Map<String, String> parseParameters(String component) {
        final Map<String, String> parameters = new HashMap<>();
        if (!component.isEmpty()) {
            for (String rComponent : component.split("&")) {
                final String[] kv = rComponent.split("=");
                final String key = kv[0];
                final String val = (kv.length == 2) ? kv[1] : "";
                parameters.put(key, val);
            }
        }
        return parameters;
    }

}
