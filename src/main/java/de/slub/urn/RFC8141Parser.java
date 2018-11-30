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

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.slub.urn.NamespaceSpecificString.Encoding.URL_ENCODED;
import static java.util.Collections.EMPTY_LIST;

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
    public URN_8141 parse(String urnLiteral) throws URNSyntaxException {
        if ((urnLiteral == null) || (urnLiteral.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = urnLiteral.split(":");

        if (parts.length < 3 || !URN.SCHEME.equalsIgnoreCase(parts[0])) {
            throw new URNSyntaxException(
                    String.format("Invalid format `%s` is probably not a URN", urnLiteral));
        }

        // NID is the first part after "urn:"
        final NID_RFC8141 nid = new NID_RFC8141(parts[1]);

        // assemble rest of the parts for NSS parsing
        String schemeSpecificPart = urnLiteral.substring(urnLiteral.indexOf(parts[1]) + parts[1].length() + 1);

        // copy NSS part by cutting out RQF components from NSS
        int rComponentIndex = schemeSpecificPart.indexOf("?+");
        int qComponentIndex = schemeSpecificPart.indexOf("?=");
        int fragmentIndex   = schemeSpecificPart.indexOf("#");

        // find end of NSS (just before RQF part)
        int endOfNSS = schemeSpecificPart.length();
        if (rComponentIndex > 0 && rComponentIndex < endOfNSS) {
            endOfNSS = rComponentIndex;
        }
        if (qComponentIndex > 0 && qComponentIndex < endOfNSS) {
            endOfNSS = qComponentIndex;
        }
        if (fragmentIndex > 0 && fragmentIndex < endOfNSS) {
            endOfNSS = fragmentIndex;
        }

        final NSS_RFC8141 nss = new NSS_RFC8141(schemeSpecificPart.substring(0, endOfNSS), URL_ENCODED);

        // remove trailing # if necessary
        if (fragmentIndex == schemeSpecificPart.length() - 1) {
            schemeSpecificPart = schemeSpecificPart.substring(0, schemeSpecificPart.length() - 2);
        }

        final RQF_RFC8141 rqfComponents = parseRQFComponents(schemeSpecificPart);

        return new URN_8141(nid, nss, rqfComponents);
    }

    /**
     * @see URNParser#parse(URI)
     */
    @Override
    public URN_8141 parse(URI uri) throws URNSyntaxException {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return parse(uri.toASCIIString());
    }

    private RQF_RFC8141 parseRQFComponents(String s) {
        return new RQF_RFC8141(
                parseResolutionParameters(s),
                parseQueryParameters(s),
                parseFragment(s));
    }

    private Map<String, String> parseResolutionParameters(String s) {
        final Pattern rComponentPattern = Pattern.compile("\\?\\+([^?#]*)");
        return parseComponentsFromMatcher(rComponentPattern.matcher(s));
    }

    private Map<String, String> parseQueryParameters(String s) {
        final Pattern qComponentPattern = Pattern.compile("\\?=([^?#]*)");
        return parseComponentsFromMatcher(qComponentPattern.matcher(s));
    }

    private String parseFragment(String s) {
        final Pattern fragment = Pattern.compile("#(.*)$");
        Matcher       matcher  = fragment.matcher(s);
        return matcher.find() ? matcher.group(1) : "";
    }

    private Map<String, String> parseComponentsFromMatcher(Matcher rComponentMatcher) {
        List<String> components = EMPTY_LIST;
        if (rComponentMatcher.find()) {
            final String params = rComponentMatcher.toMatchResult().group(1);
            components = Arrays.asList(params.split("&"));
        }

        Map<String, String> parameters = new HashMap<>();
        for (String rComponent : components) {
            final String[] kv  = rComponent.split("=");
            final String   key = kv[0];
            final String   val = (kv.length == 2) ? kv[1] : "";
            parameters.put(key, val);
        }

        return parameters;
    }

}
