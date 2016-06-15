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

import static java.lang.Character.forDigit;
import static java.lang.Character.toLowerCase;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a Uniform Resource Name (URN).
 *
 * Creating a URN instance is done by using one of static factory methods, e.g.:
 * <pre>
 *     {@code
 *      URN urn = URN.newInstance("isbn", "0451450523");
 *      // or...
 *      URN urn = URN.fromString("urn:isbn:0451450523");
 * }
 * </pre>
 *
 * One can also use URN instances or URIs to create new a URN instance, e.g.:
 * <pre>
 *     {@code
 *      URN urn = URN.fromURI(new URI("urn:isbn:0451450523"));
 *      // or
 *      URN urn = URN.fromURN(URN.newInstance("isbn", "0451450523"));
 * }
 * </pre>
 *
 * The URN class itself is not designed to be extended. To represent URNs from other URN namespaces a specific class
 * should utilize this class to parse, serialize and validate proper Namespace Specific String encoding.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
final public class URN {

    private static final String URN_SCHEME = "urn";

    private final String encodedNamespaceSpecificString;
    private final NamespaceIdentifier namespaceIdentifier;
    private final String namespaceSpecificString;


    private URN(NamespaceIdentifier namespaceIdentifier, String namespaceSpecificString, String encodedNamespaceSpecificString) {
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
        this.encodedNamespaceSpecificString = encodedNamespaceSpecificString;
    }

    /**
     * Create a new URN instance using another URN instance.
     *
     * @param urn URN to duplicate
     * @return New URN instance equal to the given URN instance
     */
    public static URN fromURN(URN urn) {
        return new URN(urn.namespaceIdentifier, urn.namespaceSpecificString, urn.encodedNamespaceSpecificString);
    }

    /**
     * Create a new URN instance using a particular Namespace Identifier and Namespace Specific String.
     *
     * Syntax rules for URNs apply as described in RFC2141
     *
     * @param namespaceIdentifier     The URNs Namespace Identifier
     * @param namespaceSpecificString The URNs Namespace Specific String
     * @return A new URN instance
     * @throws URNSyntaxException If any of the syntax rules is violated or if passed parameter
     *                            strings a <pre>null</pre> or empty.
     */
    public static URN newInstance(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        final NamespaceIdentifier nid = new NamespaceIdentifier(namespaceIdentifier);
        assertNotNullNotEmpty("Namespace Specific String", namespaceSpecificString);
        return new URN(nid, namespaceSpecificString, utf8encode(namespaceSpecificString));
    }

    /**
     * Create a new URN instance by parsing a URN serialisation.
     *
     * @param urn String to be parsed into an URN instance with
     * @return New URN instance
     * @throws URNSyntaxException If any of the syntax rules is violated or if passed string is <pre>null</pre> or empty.
     */
    public static URN fromString(String urn) throws URNSyntaxException {
        assertNotNullNotEmpty("URN", urn);
        final String[] parts = urn.split(":");

        if (parts.length < 3 || !URN_SCHEME.equalsIgnoreCase(parts[0])) {
            throw new URNSyntaxException(
                    String.format("Invalid format `%s` is probably not a URN", urn));
        }

        final String encodedNSSPart = urn.substring(urn.indexOf(parts[1]) + parts[1].length() + 1);
        NamespaceSpecificString.validateNamespaceSpecificString(encodedNSSPart);

        final NamespaceIdentifier namespaceIdentifier = new NamespaceIdentifier(parts[1]);

        final String namespaceSpecificString = utf8decode(encodedNSSPart);
        final String encodedNamespaceSpecificString = normalizeOctedPairs(encodedNSSPart);

        return new URN(namespaceIdentifier, namespaceSpecificString, encodedNamespaceSpecificString);
    }

    /**
     * Constructing a new URN instance by parsing URN parts from a URI instance.
     *
     * @param uri The URI to be parsed into a URN
     * @return A new URN instance
     * @throws URNSyntaxException If the URI scheme is not <pre>urn</pre> or the scheme specific part cannot be
     *                            parsed into Namespace Identifier and Namespace Specific String.
     */
    public static URN fromURI(URI uri) throws URNSyntaxException {
        final String scheme = uri.getScheme();
        if (!URN_SCHEME.equalsIgnoreCase(scheme)) {
            throw new URNSyntaxException(
                    String.format("Invalid scheme `%s` Given URI is not a URN", scheme));
        }

        final String schemeSpecificPart = uri.getSchemeSpecificPart();
        int colonPos = schemeSpecificPart.indexOf(':');
        NamespaceIdentifier nid = null;
        String nss = null;
        if (colonPos > -1) {
            nid = new NamespaceIdentifier(schemeSpecificPart.substring(0, colonPos));
            nss = schemeSpecificPart.substring(colonPos + 1);
        }

        return new URN(nid, nss, utf8encode(nss));
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

    private static String normalizeOctedPairs(String s) {
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

    /**
     * Return the URNs Namespace Identifier part.
     *
     * @return The Namespace Identifier
     */
    public String getNamespaceIdentifier() {
        return namespaceIdentifier.toString();
    }

    /**
     * Return the URNs Namespace Specific String part.
     *
     * @return The URL decoded Namespace Specific String
     */
    public String getNamespaceSpecificString() {
        return namespaceSpecificString;
    }

    /**
     * Return a URI instance for this URN.
     *
     * @return URI instance
     * @throws URISyntaxException If a URI couldn't be constructed
     */
    public URI toURI() throws URISyntaxException {
        return new URI(this.toString());
    }

    /**
     * Return a URN serialization in URN syntax: <pre>&lt;URN&gt; ::= &quot;urn:&quot; &lt;NID&gt; &quot;:&quot; &lt;NSS&gt;</pre>
     *
     * @return URN serialization for this URN instance
     */
    @Override
    public String toString() {
        return String.format("%s:%s:%s", URN_SCHEME, namespaceIdentifier, encodedNamespaceSpecificString);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && namespaceIdentifier.equals(((URN) obj).namespaceIdentifier)
                && encodedNamespaceSpecificString.equals(((URN) obj).encodedNamespaceSpecificString);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    protected Object clone() throws CloneNotSupportedException {
        return fromURN(this);
    }
}
