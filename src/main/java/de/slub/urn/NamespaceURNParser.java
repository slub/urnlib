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

import java.net.URI;

/**
 * Implements a parser for the URNs of a {@link Namespace}: a literal is parsed according to RFC 8141 and then
 * validated against the rules of the namespace.
 * <p>
 * Returned by {@link URN#in(Namespace)}, which is why the class is not public. Parsing is delegated to
 * {@link RFC8141Parser} and the connection to the namespace to {@code NamespaceURN.connect()}, the same method that
 * {@link URN#as(Namespace)} uses. So both ways to obtain a {@link NamespaceURN} apply the same checks.
 */
final class NamespaceURNParser<T> implements URNParser<NamespaceURN<T>> {

    private final Namespace<T> namespace;

    NamespaceURNParser(Namespace<T> namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace cannot be null");
        }
        this.namespace = namespace;
    }

    @Override
    public NamespaceURN<T> parse(String urnLiteral) throws URNSyntaxError {
        return NamespaceURN.connect(URN.rfc8141().parse(urnLiteral), namespace);
    }

    @Override
    public NamespaceURN<T> parse(URI uri) throws URNSyntaxError {
        return NamespaceURN.connect(URN.rfc8141().parse(uri), namespace);
    }

}
