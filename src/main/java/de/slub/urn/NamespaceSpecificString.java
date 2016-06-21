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
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static java.lang.Character.forDigit;
import static java.lang.Character.toLowerCase;
import static java.nio.charset.StandardCharsets.UTF_8;

public class NamespaceSpecificString {

    private static final Pattern allowedCharacters = Pattern.compile("^([0-9a-zA-Z()+,-.:=@;$_!*']|(%[0-9a-fA-F]{2}))+$");
    private final String encoded;
    private final String raw;

    private NamespaceSpecificString(String encoded, String raw) {
        this.encoded = encoded;
        this.raw = raw;
    }

    private NamespaceSpecificString(NamespaceSpecificString instanceForCloning) {
        this.encoded = instanceForCloning.encoded;
        this.raw = instanceForCloning.raw;
    }

    public static NamespaceSpecificString fromRawString(String raw) throws URNSyntaxException {
        assertNotNullNotEmpty(raw);
        return new NamespaceSpecificString(encode(raw), raw);
    }

    public static NamespaceSpecificString fromEncoded(String encoded) throws URNSyntaxException {
        assertNotNullNotEmpty(encoded);
        validateNamespaceSpecificString(encoded);
        final String lcop = lowerCaseOctedPairs(encoded);
        return new NamespaceSpecificString(lcop, decode(lcop));
    }

    private static void validateNamespaceSpecificString(String nss) throws URNSyntaxException {
        if (!allowedCharacters.matcher(nss).matches()) {
            throw new URNSyntaxException(
                    String.format("Not allowed characters in Namespace Specific String '%s'", nss));
        }
    }

    private static String encode(String s) throws URNSyntaxException {
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

    private static String lowerCaseOctedPairs(String s) {
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

    private static String decode(String s) throws URNSyntaxException {
        try {
            return URLDecoder.decode(s, UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            // Both Exceptions cannot happen because:
            //  1. the character set is hard-coded and always known
            //  2. the hex encoding has been checked by pattern matching before
            //
            // Should something be wrong with the above mentioned check, throwing
            // a URNSyntaxException is in order.
            throw new URNSyntaxException("Error decoding Namespace Specific String part", e);
        }
    }

    private static void assertNotNullNotEmpty(String s) throws IllegalArgumentException {
        if ((s == null) || (s.isEmpty())) {
            throw new IllegalArgumentException("Namespace Specific String part cannot be null or empty");
        }
    }

    public String raw() {
        return this.raw;
    }

    @Override
    public String toString() {
        return this.encoded;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceSpecificString
                && this.encoded.equals(((NamespaceSpecificString) obj).encoded);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new NamespaceSpecificString(this);
    }

    @Override
    public int hashCode() {
        return encoded.hashCode();
    }

}
