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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.function.IntPredicate;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Percent-encoding helpers according to RFC 3986, section 2.1.
 * <p>
 * Validation is implemented as a plain character loop instead of a regular expression, so that input of any
 * length can be checked without risking a {@link StackOverflowError}.
 */
final class PercentEncoding {

    private PercentEncoding() {
    }

    /**
     * Check whether a string consists only of allowed literal characters and well-formed percent-encoded octets.
     *
     * @param s                 String to check
     * @param allowedLiteral    Predicate telling whether a character may appear literally
     * @param allowNullOctet    Whether the percent-encoded octet {@code %00} is allowed
     * @return True if the string is valid
     */
    static boolean isValid(CharSequence s, IntPredicate allowedLiteral, boolean allowNullOctet) {
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            final char c = s.charAt(i);
            if (c == '%') {
                if (i + 2 >= length || !isHexDigit(s.charAt(i + 1)) || !isHexDigit(s.charAt(i + 2))) {
                    return false;
                }
                if (!allowNullOctet && s.charAt(i + 1) == '0' && s.charAt(i + 2) == '0') {
                    return false;
                }
                i += 2;
            } else if (!allowedLiteral.test(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lower-case the hex digits of all percent-encoded octets.
     *
     * @param s A string with well-formed percent-encoded octets
     * @return The string with lower-cased percent-encoded octets
     */
    static String lowerCaseOctets(String s) {
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            sb.append(c);
            if (c == '%' && i + 2 < s.length()) {
                sb.append(Character.toLowerCase(s.charAt(i + 1)))
                        .append(Character.toLowerCase(s.charAt(i + 2)));
                i += 2;
            }
        }
        return sb.toString();
    }

    /**
     * Decode all percent-encoded octets and interpret the resulting octet sequence as UTF-8.
     * <p>
     * Unlike {@link java.net.URLDecoder}, a {@code +} is not treated as a space and malformed UTF-8 is reported
     * instead of being replaced.
     *
     * @param s A string containing only ASCII characters and well-formed percent-encoded octets
     * @return The decoded string
     * @throws CharacterCodingException if the decoded octets are not valid UTF-8
     */
    static String decode(String s) throws CharacterCodingException {
        if (s.indexOf('%') < 0) {
            return s;
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '%') {
                bytes.write((Character.digit(s.charAt(i + 1), 16) << 4) | Character.digit(s.charAt(i + 2), 16));
                i += 2;
            } else {
                bytes.write(c);
            }
        }
        return UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes.toByteArray()))
                .toString();
    }

    /**
     * Percent-encode every code point matching the given predicate as a sequence of UTF-8 octets.
     *
     * @param s            The string to encode
     * @param mustBeEncoded Predicate telling whether a code point has to be percent-encoded
     * @return The encoded string
     * @throws CharacterCodingException if the string contains an unpaired surrogate, which has no UTF-8 encoding
     */
    static String encode(String s, IntPredicate mustBeEncoded) throws CharacterCodingException {
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            final int cp = s.codePointAt(i);
            if (cp >= Character.MIN_SURROGATE && cp <= Character.MAX_SURROGATE) {
                // codePointAt() returns an unpaired surrogate as is
                throw new CharacterCodingException();
            }
            if (mustBeEncoded.test(cp)) {
                for (byte b : new String(Character.toChars(cp)).getBytes(UTF_8)) {
                    sb.append('%')
                            .append(Character.forDigit((b >> 4) & 0xF, 16))
                            .append(Character.forDigit(b & 0xF, 16));
                }
            } else {
                sb.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
