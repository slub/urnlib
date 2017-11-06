package de.slub.urn;

import java.net.URI;

import static de.slub.urn.NamespaceIdentifier.URN_SCHEME;
import static de.slub.urn.NamespaceSpecificString.NssEncoding.NOT_ENCODED;
import static de.slub.urn.NamespaceSpecificString.NssEncoding.URL_ENCODED;

public class URNFactory {

    /**
     * Create a new URN instance using a particular Namespace Identifier and Namespace Specific String.
     * <p>
     * Syntax rules for URNs apply as described in RFC2141
     *
     * @param namespaceIdentifier     The URNs Namespace Identifier literal
     * @param namespaceSpecificString The URNs Namespace Specific String literal
     * @return A new URN instance
     * @throws IllegalArgumentException If the given string cannot be parsed into a URN
     */
    public URN create(String namespaceIdentifier, String namespaceSpecificString) {
        try {
            final NamespaceIdentifier nid = getNidInstance(namespaceIdentifier);
            final NamespaceSpecificString nss = getNssInstance(namespaceSpecificString, NOT_ENCODED);
            return new URN(nid, nss, URN.RFC.RFC_2141);
        } catch (URNSyntaxException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Error creating URN instance", e);
        }
    }

    private NamespaceIdentifier getNidInstance(String namespaceIdentifier) throws URNSyntaxException {
        return new NID_RFC2141(namespaceIdentifier);
    }

    private NamespaceSpecificString getNssInstance(String namespaceSpecificString, NamespaceSpecificString.NssEncoding encoding) throws URNSyntaxException {
        return new NSS_RFC2141(namespaceSpecificString, encoding);
    }

    /**
     * Create a new URN instance by parsing a URN serialisation.
     *
     * @param urn String to be parsed into an URN instance with
     * @return New URN instance
     * @throws IllegalArgumentException If the given string cannot be parsed into a URN
     */
    public URN create(String urn) {
        if ((urn == null) || (urn.isEmpty())) {
            throw new IllegalArgumentException("URN cannot be null or empty");
        }

        final String[] parts = urn.split(":");

        try {
            if (parts.length < 3 || !URN_SCHEME.equalsIgnoreCase(parts[0])) {
                throw new URNSyntaxException(
                        String.format("Invalid format `%s` is probably not a URN", urn));
            }

            final NamespaceIdentifier namespaceIdentifier = getNidInstance(parts[1]);
            final String encodedNSSPart = urn.substring(urn.indexOf(parts[1]) + parts[1].length() + 1);
            final NamespaceSpecificString namespaceSpecificString = getNssInstance(encodedNSSPart, URL_ENCODED);

            return new URN(namespaceIdentifier, namespaceSpecificString, URN.RFC.RFC_2141);
        } catch (URNSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Constructing a new URN instance by parsing URN parts from a URI instance.
     *
     * @param uri The URI to be parsed into a URN
     * @return A new URN instance
     * @throws IllegalArgumentException If the given URI cannot be parsed into a URN
     */
    public URN create(URI uri) {
        final String scheme = uri.getScheme();
        try {
            if (!URN_SCHEME.equalsIgnoreCase(scheme)) {
                throw new URNSyntaxException(
                        String.format("Invalid scheme: `%s` - Given URI is not a URN", scheme));
            }

            final String schemeSpecificPart = uri.getSchemeSpecificPart();
            int colonPos = schemeSpecificPart.indexOf(':');
            if (colonPos > -1) {
                return new URN(
                        getNidInstance(schemeSpecificPart.substring(0, colonPos)),
                        getNssInstance(schemeSpecificPart.substring(colonPos + 1), URL_ENCODED),
                        URN.RFC.RFC_2141);
            } else {
                throw new URNSyntaxException(
                        String.format("Invalid format: `%s` - Given schema specific part is not a URN part", schemeSpecificPart));
            }
        } catch (URNSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
