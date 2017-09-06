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

import java.net.URI;
import java.net.URISyntaxException;

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
 * Note that when using the factory methods, the passed namespace specific string has to be properly
 * encoded in respect to the rules described in RFC2141.
 *
 * The URN class itself is not designed to be extended. To represent URNs from other URN namespaces a specific class
 * should utilize this class to parse, serialize and validate proper Namespace Specific String encoding. You should use
 * the specific URN part classes {@link NamespaceIdentifier} and {@link NamespaceSpecificString}.
 *
 * URN instances are immutable, cloneable and comparable by using {@code equals()}.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
final public class URN {

    private static final String URN_SCHEME = "urn";

    private final NamespaceIdentifier namespaceIdentifier;
    private final NamespaceSpecificString namespaceSpecificString;

    private URN(NamespaceIdentifier namespaceIdentifier, NamespaceSpecificString namespaceSpecificString) {
        this.namespaceIdentifier = namespaceIdentifier;
        this.namespaceSpecificString = namespaceSpecificString;
    }

    /**
     * Create a new URN instance using another URN instance.
     *
     * @param urn URN to duplicate
     * @return New URN instance equal to the given URN instance
     */
    public static URN fromURN(URN urn) {
        return new URN(urn.namespaceIdentifier, urn.namespaceSpecificString);
    }

    /**
     * Create a new URN instance using a particular Namespace Identifier and Namespace Specific String.
     *
     * Syntax rules for URNs apply as described in RFC2141
     *
     * @param namespaceIdentifier     The URNs Namespace Identifier literal
     * @param namespaceSpecificString The URNs Namespace Specific String literal
     * @return A new URN instance
     * @throws URNSyntaxException If any of the syntax rules is violated or if passed parameter
     *                            strings a <pre>null</pre> or empty.
     */
    public static URN newInstance(String namespaceIdentifier, String namespaceSpecificString) throws URNSyntaxException {
        try {
            final NamespaceIdentifier nid = new NamespaceIdentifier(namespaceIdentifier);
            final NamespaceSpecificString nss = NamespaceSpecificString.fromRawString(namespaceSpecificString);
            return new URN(nid, nss);
        } catch (URNSyntaxException | IllegalArgumentException e) {
            throw new URNSyntaxException("Error creating URN instance", e);
        }
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

        final NamespaceIdentifier namespaceIdentifier = new NamespaceIdentifier(parts[1]);
        final String encodedNSSPart = urn.substring(urn.indexOf(parts[1]) + parts[1].length() + 1);
        final NamespaceSpecificString namespaceSpecificString = NamespaceSpecificString.fromEncoded(encodedNSSPart);

        return new URN(namespaceIdentifier, namespaceSpecificString);
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
                    String.format("Invalid scheme: `%s` - Given URI is not a URN", scheme));
        }

        final String schemeSpecificPart = uri.getSchemeSpecificPart();
        int colonPos = schemeSpecificPart.indexOf(':');
        if (colonPos > -1) {
            return new URN(
                    new NamespaceIdentifier(schemeSpecificPart.substring(0, colonPos)),
                    NamespaceSpecificString.fromEncoded(schemeSpecificPart.substring(colonPos + 1)));
        } else {
            throw new URNSyntaxException(
                    String.format("Invalid format: `%s` - Given schema specific part is not a URN part", schemeSpecificPart));
        }
    }

    /**
     * Creates a URN by parsing the given string.
     * <p>
     * This convenience factory method works as if by invoking the fromString(String) method; any URNSyntaxException
     * thrown by the method is caught and wrapped in a new IllegalArgumentException object, which is then thrown.
     * <p>
     * This method is provided for use in situations where it is known that the given string is a legal URN, for example
     * for URN constants declared within in a program, and so it would be considered a programming error for the string
     * not to parse as such. The methods, which throw URNSyntaxException directly, should be used situations where
     * a URN is being constructed from user input or from some other source that may be prone to errors.
     *
     * @param str String to be parsed into a URN
     * @return The new URN
     * @throws NullPointerException     If str is null
     * @throws IllegalArgumentException If the given string cannot be parsed into a URN
     */
    public static URN create(String str) {
        if (str == null) {
            throw new NullPointerException("Null is not allowed as an argument");
        }
        try {
            return fromString(str);
        } catch (URNSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void assertNotNullNotEmpty(String part, String s) throws URNSyntaxException {
        if ((s == null) || (s.isEmpty())) {
            throw new URNSyntaxException(
                    String.format("%s cannot be null or empty", part));
        }
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
        return namespaceSpecificString.toString();
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
        return String.format("%s:%s:%s", URN_SCHEME, namespaceIdentifier, namespaceSpecificString);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof URN)
                && namespaceIdentifier.equals(((URN) obj).namespaceIdentifier)
                && namespaceSpecificString.equals(((URN) obj).namespaceSpecificString);
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
