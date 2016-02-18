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

package de.slub;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static java.lang.Character.forDigit;
import static java.lang.Character.toUpperCase;

public class URN {

    static final private Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]+[0-9a-zA-Z-]{1,31}$");

    private String namespaceIdentifier;
    private String namespaceSpecificString;

    public URN(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        init(namespaceIdentifier, namespaceSpecificString);
    }

    public URN(URI uri) throws URNSyntaxException {
        init(uri);
    }

    public URN(String urn) throws URNSyntaxException {
        try {
            init(new URI(assertNotNullNotEmpty("URN", urn)));
        } catch (URISyntaxException e) {
            throw new URNSyntaxException(
                    String.format("Invalid format `%s` is probably not a URN", urn));
        }
    }

    public URI toURI() throws URISyntaxException, URNSyntaxException {
        return new URI(String.format("urn:%s:%s",
                namespaceIdentifier, utf8encode(namespaceSpecificString)));
    }

    public String getNamespaceIdentifier() {
        return namespaceIdentifier;
    }

    public String getNamespaceSpecificString() {
        return namespaceSpecificString;
    }

    private void init(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        this.namespaceIdentifier = assertValidNID(namespaceIdentifier);
        this.namespaceSpecificString = assertValidNSS(namespaceSpecificString);
    }

    private void init(URI uri) throws URNSyntaxException {
        final String scheme = uri.getScheme();
        if (!"urn".equals(scheme)) {
            throw new URNSyntaxException(
                    String.format("Invalid scheme `%s` Given URI is not a URN", scheme));
        }

        final String schemeSpecificPart = uri.getSchemeSpecificPart();
        int colonPos = schemeSpecificPart.indexOf(':');
        String nid = null;
        String nss = null;
        if (colonPos > -1) {
            nid = schemeSpecificPart.substring(0, colonPos);
            nss = schemeSpecificPart.substring(colonPos + 1);
        }

        init(nid, nss);
    }

    private String assertValidNID(String namespaceIdentifier) throws URNSyntaxException {
        assertNotNullNotEmpty("Namespace Identifier", namespaceIdentifier);

        if ("urn".equalsIgnoreCase(namespaceIdentifier)) {
            throw new URNSyntaxException("Namespace identifier can not be 'urn'");
        }

        if (!allowedNID.matcher(namespaceIdentifier).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Identifier '%s'", namespaceIdentifier));
        }

        return namespaceIdentifier;
    }

    private String assertValidNSS(String namespaceSpecificString) throws URNSyntaxException {
        assertNotNullNotEmpty("Namespace Specific String", namespaceSpecificString);
        return namespaceSpecificString;
    }

    private String assertNotNullNotEmpty(String part, String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException(part + " cannot be null or empty");
        }
        return s;
    }

    // http://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/21178195#21178195
    // http://www.utf8-chartable.de/
    private String utf8encode(String s) throws URNSyntaxException {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                // Characters here are in Unicode so we can easily switch-case them
                case 0:
                    throw new URNSyntaxException("Illegal character `0` found");
                case '%':   // Encode reserved character set (RFC 2141, 2.3)
                case '/':
                case '?':
                case '#':
                case '\\':   // Encode explicitly excluded characters (RFC 2141, 2.4)
                case '"':
                case '&':
                case '<':
                case '>':
                case '[':
                case ']':
                case '^':
                case '`':
                case '{':
                case '|':
                case '}':
                case '~':
                    appendEncoded(sb, c);
                    break;
                default:
                    // URN encoding requires UTF-8, so transform Unicode character to UTF-8 bytes and encode them
                    for (byte b : String.valueOf(c).getBytes(StandardCharsets.UTF_8)) {
                        if ((b >= 0x01) && (b <= 0x20) || ((b >= 0x7F))) {
                            // Encode range 0x1 to 0x20 hex and 0x7f to 0xff (RFC 2141, 2.4)
                            appendEncoded(sb, c);
                        } else {
                            // no need for encoding, just append and skip to next character
                            sb.append(c);
                            break;
                        }
                    }
            }
        }
        return sb.toString();
    }

    private void appendEncoded(StringBuilder sb, char c) {
        sb.append('%');
        sb.append(toUpperCase(forDigit((c >> 4) & 0xF, 16)));
        sb.append(toUpperCase(forDigit((c & 0xF), 16)));
    }

}
