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

package de.slub.urn.fdc;

import de.slub.urn.NSS_RFC2141;
import de.slub.urn.Namespace;
import de.slub.urn.NamespaceIdentifier;
import de.slub.urn.NamespaceSpecificString;
import de.slub.urn.NamespaceURN;
import de.slub.urn.Specification;
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxError;

import java.util.Locale;

import static de.slub.urn.NamespaceSpecificString.Encoding.NOT_ENCODED;
import static de.slub.urn.URNSyntaxError.syntaxError;

/**
 * The namespace of Federated Content URNs ({@code urn:fdc:}) according to RFC 4198.
 * <p>
 * RFC 4198 registers the Namespace Identifier {@code fdc}, structures the Namespace Specific String as described by
 * {@link FederatedContentName} and defines that the case of the ProviderId does not matter for equivalence. Apart
 * from that, a Federated Content URN follows the URN syntax of RFC 8141. It may have resolution, query and fragment
 * components, even though RFC 4198 predates them.
 * <pre>
 * NamespaceURN&lt;FederatedContentName&gt; urn =
 *         URN.in(FederatedContent.NAMESPACE).parse("urn:fdc:example.net:200406:ivr:51089");
 * urn.name().providerId();  // example.net
 * </pre>
 * Use {@link URN#as(Namespace)} to convert a URN of another type and {@link #of(String, String, String)} to build a
 * new one from its fields.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc4198">A Uniform Resource Name (URN) Namespace for Federated Content</a>
 */
public final class FederatedContent {

    /**
     * The Namespace Identifier registered by RFC 4198.
     */
    public static final String NID = "fdc";

    /**
     * RFC 4198, A Uniform Resource Name (URN) Namespace for Federated Content (November 2005).
     */
    public static final Specification SPECIFICATION = new Specification() {
        @Override
        public String name() {
            return "RFC 4198";
        }

        @Override
        public String url() {
            return "https://tools.ietf.org/html/rfc4198";
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /**
     * The namespace of Federated Content URNs.
     */
    public static final Namespace<FederatedContentName> NAMESPACE = new Namespace<>() {
        @Override
        public boolean accepts(NamespaceIdentifier nid) {
            return NID.equalsIgnoreCase(nid.toString());
        }

        @Override
        public FederatedContentName read(NamespaceSpecificString nss) throws URNSyntaxError {
            return FederatedContentName.parse(nss.toString());
        }

        // The ProviderId is case-insensitive (RFC 4198, section 2)
        @Override
        public String normalForm(String rfcNormalForm) {
            final int endOfProviderId = rfcNormalForm.indexOf(':');
            if (endOfProviderId < 0) {
                return rfcNormalForm;
            }
            return rfcNormalForm.substring(0, endOfProviderId).toLowerCase(Locale.ROOT)
                    + rfcNormalForm.substring(endOfProviderId);
        }

        @Override
        public Specification specification() {
            return SPECIFICATION;
        }

        @Override
        public String toString() {
            return "urn:" + NID;
        }
    };

    private FederatedContent() {
    }

    /**
     * Build a new Federated Content URN from its fields.
     * <p>
     * The ResourceId is taken as not encoded: characters that are not allowed literally get percent-encoded.
     *
     * @param providerId The ProviderId, a domain name
     * @param dateId     The DateId in ISO 8601 basic format ({@code CCYY}, {@code CCYYMM} or {@code CCYYMMDD})
     * @param resourceId The not encoded ResourceId
     * @return The new Federated Content URN, without resolution, query or fragment components
     * @throws URNSyntaxError           if any of the fields is not valid according to RFC 4198
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public static NamespaceURN<FederatedContentName> of(String providerId, String dateId, String resourceId)
            throws URNSyntaxError {
        if (providerId == null || dateId == null || resourceId == null) {
            throw new IllegalArgumentException("ProviderId, DateId and ResourceId cannot be null");
        }
        // Validate ProviderId and DateId before building the URN, to report them by their name
        FederatedContentName.assertValidProviderId(providerId);
        FederatedContentName.assertValidDateId(dateId);
        if (resourceId.isEmpty()) {
            throw syntaxError(SPECIFICATION, "ResourceId cannot be empty");
        }
        return URN.of(NID, providerId + ":" + dateId + ":" + encode(resourceId)).as(NAMESPACE);
    }

    // The ResourceId consists of what RFC 2141 allows in a Namespace Specific String, so that type knows which
    // characters have to be percent-encoded
    private static String encode(String resourceId) throws URNSyntaxError {
        try {
            return new NSS_RFC2141(resourceId, NOT_ENCODED).toString();
        } catch (URNSyntaxError e) {
            throw syntaxError(SPECIFICATION, e.getMessage());
        }
    }

}
