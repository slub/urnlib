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

/**
 * Java library for representing, parsing and encoding URNs as specified in RFC 2141 and RFC 8141.
 * <p>
 * All types of the library are in this package. They are listed here by their role, in the order in which they are
 * best read.
 *
 * <h2>Start here: URNs</h2>
 * <ul>
 *     <li>{@link de.slub.urn.URN} is the entry point and the common type of all URNs. Its static methods
 *     {@link de.slub.urn.URN#parse(String) parse}, {@link de.slub.urn.URN#create(String) create} and
 *     {@link de.slub.urn.URN#of(String, String) of} create URNs.</li>
 *     <li>{@link de.slub.urn.URN_8141} is a URN according to RFC 8141, the current URN syntax. Use it unless there is
 *     a reason not to.</li>
 *     <li>{@link de.slub.urn.URN_2141} is a URN according to RFC 2141, the syntax that RFC 8141 replaced.</li>
 * </ul>
 *
 * <h2>The parts of a URN</h2>
 * <ul>
 *     <li>{@link de.slub.urn.NamespaceIdentifier} (NID) names the namespace, like {@code isbn} in
 *     {@code urn:isbn:0451450523}. {@link de.slub.urn.NID_RFC8141} and {@link de.slub.urn.NID_RFC2141} are its two
 *     forms.</li>
 *     <li>{@link de.slub.urn.NamespaceSpecificString} (NSS) is the name within the namespace.
 *     {@link de.slub.urn.NSS_RFC8141} and {@link de.slub.urn.NSS_RFC2141} are its two forms.</li>
 *     <li>{@link de.slub.urn.RQF_RFC8141} holds the optional resolution, query and fragment components that only
 *     RFC 8141 knows. {@link de.slub.urn.RQComponent} gives access to the parameters of an r- or q-component.</li>
 * </ul>
 *
 * <h2>Parsing</h2>
 * <ul>
 *     <li>{@link de.slub.urn.URNParser} reads a URN from a string or a {@link java.net.URI}.</li>
 *     <li>{@link de.slub.urn.RFC8141Parser} and {@link de.slub.urn.RFC2141Parser} implement it for the two
 *     syntaxes. They are obtained via {@link de.slub.urn.URN#rfc8141()} and {@link de.slub.urn.URN#rfc2141()}.</li>
 * </ul>
 *
 * <h2>Namespaces</h2>
 * <ul>
 *     <li>{@link de.slub.urn.Namespace} defines the rules of one URN namespace. Implementing it is the way to extend
 *     the library.</li>
 *     <li>{@link de.slub.urn.NamespaceURN} is a URN that is valid in a namespace and gives access to the name that the
 *     namespace has read from it. It is obtained via {@link de.slub.urn.URN#in(Namespace)} or
 *     {@link de.slub.urn.URN#as(Namespace)}.</li>
 *     <li>The package {@link de.slub.urn.fdc} defines the namespace of RFC 4198 and serves as an example.</li>
 * </ul>
 *
 * <h2>Specifications and errors</h2>
 * <ul>
 *     <li>{@link de.slub.urn.URNSyntaxError} is thrown for every literal that is not a valid URN or part of a
 *     URN.</li>
 *     <li>{@link de.slub.urn.Specification} is a document that defines rules for URNs. A syntax error refers to the
 *     one that was violated.</li>
 *     <li>{@link de.slub.urn.RFC} lists the URN syntaxes that the library supports. {@link de.slub.urn.RFCSupport}
 *     is implemented by the types that follow one of them.</li>
 * </ul>
 *
 * <h2>Rules that hold for all types</h2>
 * <p>
 * <b>Values are immutable and keep their literal.</b> {@code toString()} returns a parsed literal exactly as it was
 * given.
 * <p>
 * <b>Equality is not equivalence.</b> {@code equals()} and {@code hashCode()} compare the class and the exact
 * components. {@code isEquivalentTo()} implements the equivalence of the RFCs, which for example ignores the case of
 * the Namespace Identifier. {@code normalize()} returns the normal form that equivalence compares. Percent-encoded
 * octets are never decoded for a comparison.
 * <p>
 * <b>The URN types are closed.</b> The RFCs define what a URN is, so it is impossible to create a URN type outside of
 * this library: {@link de.slub.urn.URN}, {@link de.slub.urn.NamespaceIdentifier} and
 * {@link de.slub.urn.NamespaceSpecificString} have no accessible constructor, and their subclasses are final. Each
 * of them has one subclass per syntax, with the RFC in its name. This is also why all types share one package. The
 * rules of a namespace are added by implementing {@link de.slub.urn.Namespace}, not by subclassing.
 *
 * @author Ralf Claussnitzer
 * @see <a href="https://tools.ietf.org/html/rfc2141">URN Syntax</a>
 * @see <a href="https://tools.ietf.org/html/rfc8141">URN Syntax (superseding RFC 2141)</a>
 * @see <a href="https://tools.ietf.org/html/rfc1737">Functional Requirements for Uniform Resource Names</a>
 * @see <a href="http://www.iana.org/assignments/urn-namespaces/urn-namespaces.xhtml">Official IANA Registry of URN Namespaces</a>
 */
package de.slub.urn;