/*
 * Copyright (C) 2025 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.slub.urn;

import java.nio.charset.CharacterCodingException;

import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * Represents a Namespace Specific String (NSS) part of a Uniform Resource Identifier (URN).
 * <p>
 * {@code NamespaceSpecificString} instances are immutable and comparable by using {@code equals()}.
 * <p>
 * A URL encoded literal is kept as given, so that {@code toString()} returns it unchanged, and {@code equals()}
 * compares it exactly: {@code %2f} and {@code %2F} are different literals, and percent-encoded octets are never
 * decoded for comparison. A literal that is not URL encoded is percent-encoded with upper-case hex digits.
 * <p>
 * Percent-encoded octets do not have to form valid UTF-8, e.g. when a namespace carries binary data. Such a string
 * can't be decoded, see {@link #unencoded()}.
 * <p>
 * Equality is not URN-equivalence. To find out whether two Namespace Specific Strings are equivalent as defined by
 * RFC 8141, section 3, use {@link #isEquivalentTo(NamespaceSpecificString)}. To get the
 * normal form that equivalence is based on, use {@link #normalize()}.
 * <p>
 * This class is not open for extension: it has no accessible constructor, so all of its subclasses are part of this
 * library and conform to the RFC they report by {@link #supportedRFC()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
abstract public class NamespaceSpecificString implements RFCSupport {

    private final String encoded;
    // Normal form of the encoded literal, defines equivalence but not equality
    private final String normalized;
    // Null if the percent-encoded octets are not valid UTF-8
    private final String raw;

    /**
     * Creates a new {@code NamespaceSpecificString} instance.
     * <p>
     * A URL encoded literal is validated by {@link #isValidURLEncodedNamespaceSpecificString(String)} and kept as
     * given. A literal that is not encoded is percent-encoded here, by the same rules for all subclasses, and not
     * validated by the subclass.
     * <p>
     * Note for subclasses: this constructor calls {@link #isValidURLEncodedNamespaceSpecificString(String)} and
     * {@link #supportedRFC()} before the constructor of the subclass has run. Both must not use instance fields of
     * the subclass, which are not initialized yet. Static fields are fine.
     *
     * @param nss      The namespace specific string literal
     * @param encoding Telling whether the given literal is URL encoded or not
     * @throws URNSyntaxError           Thrown if the given literal is not valid according to
     *                                  {@link #isValidURLEncodedNamespaceSpecificString(String)}, or cannot be
     *                                  encoded
     * @throws IllegalArgumentException if the literal is null or empty
     */
    NamespaceSpecificString(String nss, Encoding encoding) throws URNSyntaxError {
        if ((nss == null) || (nss.isEmpty())) {
            throw new IllegalArgumentException("Namespace Specific String part cannot be null or empty");
        }
        if (encoding == Encoding.URL_ENCODED) {
            if (!isValidURLEncodedNamespaceSpecificString(nss)) {
                throw syntaxError(supportedRFC(),
                        String.format("Not allowed characters in Namespace Specific String '%s'", nss));
            }
            encoded = nss;
            raw = decodeOrNull(nss);
        } else {
            encoded = encode(nss);
            raw = nss;
        }
        normalized = normalForm(encoded);
    }

    // The normal form defines URN-equivalence: the hex digits of percent-encoded octets are upper case (RFC 8141,
    // section 3). Rules of a particular namespace are applied on top of it, see Namespace#normalForm(String).
    private static String normalForm(String encoded) {
        return PercentEncoding.upperCaseOctets(encoded);
    }

    private static String decodeOrNull(String encoded) {
        try {
            return PercentEncoding.decode(encoded);
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    /**
     * Create a new {@code NamespaceSpecificString} instance that is an exact copy of the given instance.
     * <p>
     * The literal and its normal form are taken over without validating or computing them again. So the given
     * instance has to be of the same class as the new one, otherwise the copy could hold a literal that its own class
     * does not accept, or a normal form that follows the rules of another class.
     *
     * @param instanceForCopying Base instance for copying
     * @throws IllegalArgumentException if the parameter is null or of another class than the new instance
     */
    NamespaceSpecificString(NamespaceSpecificString instanceForCopying) {
        if (instanceForCopying == null) {
            throw new IllegalArgumentException("Namespace Specific String cannot be null");
        }
        if (instanceForCopying.getClass() != getClass()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot copy a Namespace Specific String of type %s into a %s",
                    instanceForCopying.getClass().getSimpleName(), getClass().getSimpleName()));
        }
        this.encoded = instanceForCopying.encoded;
        this.normalized = instanceForCopying.normalized;
        this.raw = instanceForCopying.raw;
    }

    /**
     * Check if a given URL encoded NSS literal is valid.
     * <p>
     * Implementations must validate according to the RFC they represent.
     *
     * @param encoded The URL encoded NSS literal
     * @return True, if the literal is valid according to the RFC.
     */
    abstract boolean isValidURLEncodedNamespaceSpecificString(String encoded);

    /**
     * URL Encode all NSS reserved characters of a given string literal, using upper-case hex digits.
     * <p>
     * Uses {@code isReservedCharacter()} to determine whether a character is reserved according to the RFC.
     *
     * @param s A string
     * @return URL encoded string according to RFC
     * @throws URNSyntaxError Thrown if the string contains illegal character `0`.
     */
    private String encode(String s) throws URNSyntaxError {
        if (s.indexOf(0) >= 0) {
            throw syntaxError(supportedRFC(), "Illegal character `0` found");
        }
        try {
            return PercentEncoding.encode(s, NamespaceSpecificString::isReservedCharacter);
        } catch (CharacterCodingException e) {
            throw syntaxError(supportedRFC(),
                    "Namespace Specific String contains an unpaired surrogate character that cannot be encoded as UTF-8");
        }
    }

    // Encode reserved character set (RFC 2141, 2.3) and explicitly excluded characters (RFC 2141, 2.4),
    // as well as all non-ASCII characters (RFC 2141, 2.2; RFC 8141, 2.2)
    private static boolean isReservedCharacter(int c) {
        return (c <= 0x20) || (c >= 0x7F)
                || c == '%' || c == '/' || c == '?' || c == '#' || c == '<' || c == '"' || c == '&' || c == '\\'
                || c == '>' || c == '[' || c == ']' || c == '^' || c == '`' || c == '{' || c == '|'
                || c == '}' || c == '~';
    }

    /**
     * Return the decoded Namespace Specific String literal.
     * <p>
     * Use {@link NamespaceSpecificString#toString()} to get an encoded string representation. Use
     * {@link #isDecodable()} to check beforehand whether decoding is possible.
     *
     * @return Decoded Namespace Specific String literal
     * @throws IllegalStateException if the percent-encoded octets are not valid UTF-8 and therefore cannot be decoded
     *                               without loss
     */
    public String unencoded() {
        if (raw == null) {
            throw new IllegalStateException(String.format(
                    "Percent-encoded octets in Namespace Specific String '%s' are not valid UTF-8", encoded));
        }
        return this.raw;
    }

    /**
     * Tell whether the percent-encoded octets of this Namespace Specific String are valid UTF-8, so that
     * {@link #unencoded()} can decode it.
     *
     * @return True, if {@link #unencoded()} will not throw
     */
    public boolean isDecodable() {
        return raw != null;
    }

    /**
     * Return this Namespace Specific String in its normal form.
     * <p>
     * The hex digits of all percent-encoded octets are upper case (RFC 8141,
     * section 3 and RFC 3986, section 6.2.2.1), percent-encoded octets are not decoded and literal characters keep
     * their case.
     * <p>
     * A Namespace Specific String is {@linkplain #isEquivalentTo(NamespaceSpecificString) equivalent} to its normal
     * form, but only {@linkplain #equals(Object) equal} to it if it already is in normal form.
     *
     * @return A Namespace Specific String of the same type in normal form
     */
    public abstract NamespaceSpecificString normalize();

    /**
     * Return the URL encoded literal of the normal form, for use by implementations of {@link #normalize()}.
     *
     * @return The URL encoded literal in normal form
     */
    final String normalizedLiteral() {
        return normalized;
    }

    /**
     * Tell whether another Namespace Specific String is equivalent to this one.
     * <p>
     * Two Namespace Specific Strings are equivalent if their normal forms are identical, i.e. if they differ in the
     * case of the hex digits of percent-encoded octets at most (RFC 8141, section 3). Unlike {@link #equals(Object)},
     * this works across types: the same literal is always equivalent to itself, no matter which classes represent
     * it.
     * <p>
     * The equivalence rules of a particular namespace are not applied here, see {@link URN#isEquivalentTo(URN)}.
     *
     * @param other The Namespace Specific String to compare to
     * @return True, if the other Namespace Specific String is not null and equivalent to this one
     */
    public final boolean isEquivalentTo(NamespaceSpecificString other) {
        return other != null && normalized.equals(other.normalized);
    }

    /**
     * Calculates hash code based on the encoded literal, consistent with {@link #equals(Object)}.
     *
     * @return This namespace specific strings hash code
     */
    @Override
    public int hashCode() {
        return encoded.hashCode();
    }

    /**
     * Checks for equality with a given object.
     * <p>
     * Two Namespace Specific Strings are equal if they are of the same class and have the same encoded literal, as
     * returned by {@link #toString()}. The comparison is exact, including the case of hex digits in percent-encoded
     * octets.
     *
     * @param obj Object to check equality with
     * @return True, if the given object is of the same class and has the same encoded literal as this instance.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NamespaceSpecificString
                && getClass() == obj.getClass()
                && this.encoded.equals(((NamespaceSpecificString) obj).encoded);
    }

    /**
     * Return the encoded Namespace Specific String literal.
     * <p>
     * A URL encoded literal is returned exactly as it was given, including the case of hex digits in percent-encoded
     * octets.
     *
     * @return Encoded Namespace Specific String literal
     */
    @Override
    public String toString() {
        return this.encoded;
    }

    /**
     * @see RFCSupport#supports(RFC)
     */
    @Override
    public boolean supports(RFC rfc) {
        return supportedRFC().equals(rfc);
    }

    /**
     * Represents the state of encoding for a string literal.
     * <p>
     * {@link Encoding#URL_ENCODED} means that a string is URL encoded.
     * {@link Encoding#NOT_ENCODED} means that a string is <b>not</b> URL encoded and might contain characters that are
     * not allowed in URIs.
     */
    public enum Encoding {
        /**
         * The string is URL encoded (percent-encoded).
         */
        URL_ENCODED,
        /**
         * The string is not URL encoded and may contain characters that need encoding.
         */
        NOT_ENCODED
    }

}
