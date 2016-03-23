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

final public class URN {

    private static final Pattern allowedNID = Pattern.compile("^[0-9a-zA-Z]+[0-9a-zA-Z-]{0,31}$");
    private static final Pattern allowedNSS = Pattern.compile("^([0-9a-zA-Z()+,-.:=@;$_!*']|(%[0-9a-fA-F]{2}))+$");
    private static final String URN_SCHEME = "urn";

    private final String encodedNamespaceSpecificString;
    private final String namespaceIdentifier;
    private final String namespaceSpecificString;

    private URN(String namespaceIdentifier, String namespaceSpecificString, String encodedNamespaceSpecificString) {
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
        this.encodedNamespaceSpecificString = encodedNamespaceSpecificString;
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
        return String.format("%s:%s:%s", URN_SCHEME, namespaceIdentifier, encodedNamespaceSpecificString);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && namespaceIdentifier.equalsIgnoreCase(((URN) obj).namespaceIdentifier)
                && encodedNamespaceSpecificString.equals(((URN) obj).encodedNamespaceSpecificString);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public static URN fromURN(URN urn) {
        return new URN(urn.namespaceIdentifier, urn.namespaceSpecificString, urn.encodedNamespaceSpecificString);
    }

    public static URN newInstance(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        assertNotNullNotEmpty("Namespace Identifier", namespaceIdentifier);
        assertNotNullNotEmpty("Namespace Specific String", namespaceSpecificString);
        validateNID(namespaceIdentifier);
        return new URN(namespaceIdentifier, namespaceSpecificString, utf8encode(namespaceSpecificString));
    }

    public static URN fromString(String urn) throws URNSyntaxException {
        assertNotNullNotEmpty("URN", urn);
        final String[] parts = urn.split(":");

        if (parts.length < 3 || !URN_SCHEME.equalsIgnoreCase(parts[0])) {
            throw new URNSyntaxException(
                    String.format("Invalid format `%s` is probably not a URN", urn));
        }

        final String encodedNSSPart = urn.substring(urn.indexOf(parts[1]) + parts[1].length() + 1);
        validateEncodedNSS(encodedNSSPart);

        final String namespaceIdentifier = parts[1];
        validateNID(namespaceIdentifier);

        final String namespaceSpecificString = utf8decode(encodedNSSPart);
        final String encodedNamespaceSpecificString = normalizeOctedPairs(encodedNSSPart);

        return new URN(namespaceIdentifier, namespaceSpecificString, encodedNamespaceSpecificString);
    }

    public static URN fromURI(URI uri) throws URNSyntaxException {
        final String scheme = uri.getScheme();
        if (!URN_SCHEME.equalsIgnoreCase(scheme)) {
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

        validateNID(nid);

        return new URN(nid, nss, utf8encode(nss));
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    protected Object clone() throws CloneNotSupportedException {
        return fromURN(this);
    }

    private static void validateNID(String namespaceIdentifier) throws URNSyntaxException {
        if (URN_SCHEME.equalsIgnoreCase(namespaceIdentifier)) {
            throw new URNSyntaxException(
                    String.format("Namespace identifier can not be '%s'", URN_SCHEME));
        }

        if (!allowedNID.matcher(namespaceIdentifier).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Identifier '%s'", namespaceIdentifier));
        }
    }

    static private void validateEncodedNSS(String namespaceSpecificString) throws URNSyntaxException {
        if (!allowedNSS.matcher(namespaceSpecificString).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Specific String '%s'", namespaceSpecificString));
        }
    }

    private static void assertNotNullNotEmpty(String part, String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException(
                    String.format("%s cannot be null or empty", part));
        }
    }

    private static String utf8encode(String s) throws URNSyntaxException {
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
    private static boolean isReservedCharacter(char c) {
        return (c > 0x80)
                || ((c >= 0x01) && (c <= 0x20) || ((c >= 0x7F)) && (c <= 0xFF))
                || c == '%' || c == '/' || c == '?' || c == '#' || c == '<' || c == '"' || c == '&' || c == '\\'
                || c == '>' || c == '[' || c == ']' || c == '^' || c == '`' || c == '{' || c == '|'
                || c == '}' || c == '~';
    }

    // http://stackoverflow.com/questions/2817752/java-code-to-convert-byte-to-hexadecimal/21178195#21178195
    // http://www.utf8-chartable.de/
    // http://www.ascii-code.com/
    private static void appendEncoded(StringBuilder sb, char c) {
        for (byte b : String.valueOf(c).getBytes(UTF_8)) {
            sb.append('%');
            sb.append(toLowerCase(forDigit((b >> 4) & 0xF, 16)));
            sb.append(toLowerCase(forDigit((b & 0xF), 16)));
        }
    }

    private static String utf8decode(String s) throws URNSyntaxException {
        try {
            return URLDecoder.decode(s, UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            // Both Exceptions cannot happen because:
            //  1. the character set is hard-coded and always known
            //  2. the hex encoding has been checked by pattern matching before
            //
            // Should something be wrong with the above mentioned check, throwing
            // a URNSyntaxException is in order.
            throw new URNSyntaxException("Error parsing URN", e);
        }
    }

    private static String normalizeOctedPairs(String s) throws URNSyntaxException {
        StringBuilder sb = new StringBuilder(s.length());
        try (StringReader sr = new StringReader(s)) {
            int i;
            while ((i = sr.read()) != -1) {
                char c = (char) i;
                if (c == '%') {
                    sb.append('%')
                            .append(toLowerCase((char) sr.read()))
                            .append(toLowerCase((char) sr.read()));
                } else {
                    sb.append(c);
                }
            }
        } catch (IOException ignored) {
        }
        return sb.toString();
    }
}
