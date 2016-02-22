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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static java.lang.Character.forDigit;
import static java.lang.Character.toLowerCase;
import static java.nio.charset.StandardCharsets.UTF_8;

public class URN {

    static final private Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]+[0-9a-zA-Z-]{1,31}$");
    private String encodedNamespaceSpecificString;
    private String namespaceIdentifier;
    private String namespaceSpecificString;

    public URN(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        init(namespaceIdentifier, namespaceSpecificString);
    }

    public URN(URI uri) throws URNSyntaxException {
        init(uri);
    }

    public URN(String urn) throws URNSyntaxException {
        init(urn);
    }

    public String getNamespaceIdentifier() {
        return namespaceIdentifier;
    }

    public String getNamespaceSpecificString() {
        return namespaceSpecificString;
    }

    public URI toURI() throws URISyntaxException {
        return new URI(this.toString());
    }

    @Override
    public String toString() {
        return String.format("urn:%s:%s", namespaceIdentifier, encodedNamespaceSpecificString);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof URN) {
            URN comparee = (URN) obj;
            return namespaceIdentifier.equalsIgnoreCase(comparee.namespaceIdentifier)
                    && encodedNamespaceSpecificString.equals(comparee.encodedNamespaceSpecificString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    private void init(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        this.namespaceIdentifier = assertValidNID(namespaceIdentifier);
        this.namespaceSpecificString = assertValidNSS(namespaceSpecificString);
        this.encodedNamespaceSpecificString = utf8encode(this.namespaceSpecificString);
    }

    private void init(String urn) throws URNSyntaxException {
        assertNotNullNotEmpty("URN", urn);
        final String[] parts = urn.split(":");

        if (parts.length < 3 || !"urn".equalsIgnoreCase(parts[0])) {
            throw new URNSyntaxException(
                    String.format("Invalid format `%s` is probably not a URN", urn));
        }

        int thirdPartIndex = urn.indexOf(parts[1]) + parts[1].length() + 1;

        this.namespaceIdentifier = assertValidNID(parts[1]);
        this.encodedNamespaceSpecificString = normalizeOctedPairs(assertValidNSS(urn.substring(thirdPartIndex)));
        this.namespaceSpecificString = utf8decode(this.encodedNamespaceSpecificString);
    }

    private void init(URI uri) throws URNSyntaxException {
        final String scheme = uri.getScheme();
        if (!"urn".equalsIgnoreCase(scheme)) {
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

    private void assertNotNullNotEmpty(String part, String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException(part + " cannot be null or empty");
        }
    }

    // http://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/21178195#21178195
    // http://www.utf8-chartable.de/
    // http://www.ascii-code.com/
    private String utf8encode(String s) throws URNSyntaxException {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == 0) {
                throw new URNSyntaxException("Illegal character `0` found");
            }
            if (isReservedCharacter(c)) {
                appendEncoded(sb, c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Encode reserved character set (RFC 2141, 2.3) and explicitly excluded characters (RFC 2141, 2.4)
    private boolean isReservedCharacter(char c) {
        return (c > 0x80)
                || ((c >= 0x01) && (c <= 0x20) || ((c >= 0x7F)) && (c <= 0xFF))
                || c == '%' || c == '/' || c == '?' || c == '#' || c == '<' || c == '"' || c == '&' || c == '\\'
                || c == '>' || c == '[' || c == ']' || c == '^' || c == '`' || c == '{' || c == '|'
                || c == '}' || c == '~';
    }

    private void appendEncoded(StringBuilder sb, char c) {
        for (byte b : String.valueOf(c).getBytes(UTF_8)) {
            sb.append('%');
            sb.append(toLowerCase(forDigit((b >> 4) & 0xF, 16)));
            sb.append(toLowerCase(forDigit((b & 0xF), 16)));
        }
    }

    private String utf8decode(String s) throws URNSyntaxException {
        try {
            return URLDecoder.decode(s, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new URNSyntaxException("Error parsing URN", e);
        }
    }

    private String normalizeOctedPairs(String s) throws URNSyntaxException {
        StringBuilder sb = new StringBuilder(s.length());
        try (StringReader sr = new StringReader(s)) {
            int i;
            while ((i = sr.read()) != -1) {
                char c = (char) i;

                if (c == 0) {
                    throw new URNSyntaxException("Illegal character `0` found");
                }

                if (c == '%') {
                    sb.append('%')
                            .append(toLowerCase((char) sr.read()))
                            .append(toLowerCase((char) sr.read()));
                } else {
                    sb.append(c);
                }
            }
        } catch (IOException e) {
            throw new URNSyntaxException("Error parsing URN", e);
        }
        return sb.toString().trim();
    }
}
