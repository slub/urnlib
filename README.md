# urnlib

![Build](https://github.com/slub/urnlib/actions/workflows/ci.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/slub/urnlib/badge.svg?branch=main)](https://coveralls.io/github/slub/urnlib?branch=main)
![Maven Central Version](https://img.shields.io/maven-central/v/de.slub-dresden/urnlib)


Java library for representing, parsing and encoding URNs as specified in [RFC 2141] and [RFC 8141]. It also supports the Federated Content URNs of the `fdc` namespace as specified in [RFC 4198].

The initial URN RFC 2141 of May 1997 was superseded by RFC 8141 in April 2017. RFC 8141 added support for path characters and optional resolution, query and fragment parts. While URNs look very similar to URIs and are somewhat related, different equality rules apply for URNs in regard to URN character encoding rules. Also some syntactic rules about case-insensitivity are actually to be defined by the implementing system handling the URNs.

This library provides classes for representing, parsing and constructing a Uniform
Resource Name (URN) and its parts Namespace Identifier (NID), Namespace Specific String (NSS) and optional Resolution, Query and Fragment components (RQF). If you don't need RFC 2141 URNs explicitly, always use RFC 8141 URNs because that is the current valid specification and RFC 8141 is backward compatible to RFC 2141.

The library is compiled for Java 11.

This README describes the main branch, which is the upcoming version 4. The API of the released version is described in the [README of version 3.0.0].

- [Usage](#usage) shows how to parse, build and compare URNs.
- [Development](#development) is for those who want to change the library.
- The Javadoc of the package `de.slub.urn` lists all classes by their role.

## Usage

```java
import de.slub.urn.NamespaceURN;
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxError;
import de.slub.urn.URN_2141;
import de.slub.urn.URN_8141;
import de.slub.urn.fdc.FederatedContent;
import de.slub.urn.fdc.FederatedContentName;

import java.util.Optional;

class Demo {
    // A constant: an invalid literal is a programming error and throws an IllegalArgumentException
    static final URN_8141 EXAMPLE = URN.create("urn:example:foo/1234");

    public void foo(String input) throws URNSyntaxError {
        // Parse a URN according to RFC 8141, the current URN syntax
        URN_8141 urn1 = URN.parse(input);
        Optional<String> fragment = urn1.getRQFComponents().fragment();

        // Build a URN from its parts
        URN_8141 urn2 = URN.of("example", "foo/1234");

        // Parse a URN according to RFC 2141 explicitly (not recommended)
        URN_2141 urn3 = URN.rfc2141().parse("urn:example:1234");

        // Parse a URN of a particular namespace, here the Federated Content URNs of RFC 4198, see below
        NamespaceURN<FederatedContentName> urn4 =
                URN.in(FederatedContent.NAMESPACE).parse("urn:fdc:example.net:200406:ivr:51089");
    }
}
```

`URN.parse()`, `URN.create()` and `URN.of()` produce RFC 8141 URNs. `URN.rfc8141()` and `URN.rfc2141()` return a parser for an explicitly chosen syntax.


### Parts of a URN

A URN gives access to its Namespace Identifier (NID) and Namespace Specific String (NSS). A URN according to RFC 8141 may also have resolution, query and fragment components, which are not part of the name.

```java
URN_8141 urn = URN.parse("urn:example:a%20b?+ttl=60&lang=en?=page=2#top");

urn.namespaceIdentifier().toString();       // example
urn.namespaceSpecificString().toString();   // a%20b
urn.namespaceSpecificString().unencoded();  // a b

RQF_RFC8141 rqf = urn.getRQFComponents();
rqf.resolutionParameters();  // {ttl=60, lang=en}
rqf.queryParameters();       // {page=2}
rqf.fragment();              // Optional[top]
```

`URN.of("example", "a%20b")` takes the NSS percent-encoded. To build a URN from an unencoded string or with optional components, create the parts and pass them to the constructor:

```java
NID_RFC8141 nid = new NID_RFC8141("example");
NSS_RFC8141 nss = new NSS_RFC8141("a b", NamespaceSpecificString.Encoding.NOT_ENCODED);  // a%20b
RQF_RFC8141 rqf = new RQF_RFC8141(
        RQComponent.builder().add("ttl", "60").build(),
        RQComponent.EMPTY,
        "top");

new URN_8141(nid, nss, rqf).toString();  // urn:example:a%20b?+ttl=60#top
```

With `NOT_ENCODED`, the characters `/`, `~` and `&` get percent-encoded too, although RFC 8141 allows them literally. `urn:example:a/b` and `urn:example:a%2Fb` are different URNs, so pass a string with such characters already encoded, as `URL_ENCODED`.

### Errors

A literal that is not a valid URN causes a `URNSyntaxError`. This is a checked exception. It tells what kind of error it is and which specification has been violated:

```java
try {
    URN.parse("urn:urn:1234");
} catch (URNSyntaxError e) {
    e.getError();                   // RESERVED, others are SYNTAX_ERROR and LENGTH_ERROR
    e.getViolatedSpecification();   // RFC.RFC_8141
    e.getMessage();                 // Namespace identifier can not be 'urn'
}
```

`URN.create()` is meant for literals in the source code. It throws an `IllegalArgumentException` instead, so it can initialize a constant. `null` and empty strings always cause an `IllegalArgumentException`.

### Normalization

`toString()` returns a parsed URN as it was given. `normalize()` returns the normal form, which is what equivalence compares. A URN of a particular namespace applies the rules of the namespace on top of the general ones. `assignedName()` drops everything but the Namespace Identifier and the Namespace Specific String.

```java
URN urn = URN.rfc8141().parse("URN:Example:a%2c?=q");
urn.toString();                             // urn:Example:a%2c?=q
urn.normalize().toString();                 // urn:example:a%2C?=q
urn.normalize().assignedName().toString();  // urn:example:a%2C

URN.in(FederatedContent.NAMESPACE).parse("urn:fdc:Example.COM:2002:%ff").normalize().toString();  // urn:fdc:example.com:2002:%FF
```

Equivalent URNs of the same type have equal normalized assigned-names. Use `urn.normalize().assignedName()` as the key if a `HashMap` or `HashSet` is to treat equivalent URNs as the same.

### Namespaces

The URN syntax leaves it to a namespace to constrain the Namespace Specific String and to declare more URNs equivalent than the general rules do. A `de.slub.urn.Namespace` defines such rules. `URN.in(namespace)` returns a parser for the URNs of a namespace and `urn.as(namespace)` converts a URN. Both yield a `NamespaceURN`. This is a URN according to RFC 8141 that is also valid in the namespace, gives access to the name the namespace has read from it and knows the equivalence rules of the namespace.

#### Federated Content URNs (RFC 4198)

[RFC 4198] registers the `fdc` namespace and structures its Namespace Specific String as `<ProviderId>:<DateId>:<ResourceId>`. `de.slub.urn.fdc.FederatedContent.NAMESPACE` validates the structure, gives access to the fields and knows the equivalence rule of the namespace: the case of the ProviderId does not matter.

```java
NamespaceURN<FederatedContentName> fdc =
        URN.in(FederatedContent.NAMESPACE).parse("urn:fdc:example.net:200406:ivr:51089");
fdc.name().providerId();  // "example.net"
fdc.name().dateId();      // "200406"
fdc.name().date();        // 2004-06-01
fdc.name().resourceId();  // "ivr:51089"
fdc.urn();                // the underlying URN_8141

// Convert a URN of another type
NamespaceURN<FederatedContentName> converted =
        URN.parse("urn:fdc:example.net:200406:ivr:51089").as(FederatedContent.NAMESPACE);

// Build a new one, the ResourceId gets percent-encoded where needed
NamespaceURN<FederatedContentName> built = FederatedContent.of("example.org", "20010527", "img089322-038");
```

Resolution, query and fragment components of RFC 8141 are accepted and available via `getRQFComponents()`.

Parsing, converting and building throw a `URNSyntaxError` if the URN does not follow RFC 4198. Its `getViolatedSpecification()` then is `FederatedContent.SPECIFICATION`:

```java
URNParser<NamespaceURN<FederatedContentName>> parser = URN.in(FederatedContent.NAMESPACE);
parser.parse("urn:example:example.net:2004:a");    // Namespace Identifier is not fdc
parser.parse("urn:fdc:localhost:2004:a");          // ProviderId is not a domain name
parser.parse("urn:fdc:example.net:20040231:a");    // DateId is not a calendar date
parser.parse("urn:fdc:example.net:04:a");          // 1 to 3 digit DateIds are reserved
parser.parse("urn:fdc:example.net:2004");          // ResourceId is missing
```

The DateId has the form `CCYY`, `CCYYMM` or `CCYYMMDD`. `date()` fills in `01` for a missing month or day. The ResourceId may contain colons, only the first two colons of the Namespace Specific String separate the fields.

A `URN_8141` does not know the rules of the `fdc` namespace. So the answer to an equivalence check depends on which URN is asked, unless both are asked:

```java
NamespaceURN<FederatedContentName> fdc =
        URN.in(FederatedContent.NAMESPACE).parse("urn:fdc:Example.NET:200406:ivr:51089");
URN_8141 generic = URN.parse("urn:fdc:example.net:200406:ivr:51089");

fdc.equals(generic);               // false, different classes
fdc.isEquivalentTo(generic);       // true, by the rules of RFC 4198
generic.isEquivalentTo(fdc);       // false, by the general rules of RFC 8141
URN.areEquivalent(generic, fdc);   // true
```

#### Defining a namespace

The URN types of the library cannot be extended. To support another namespace, implement `Namespace<T>`, where `T` is the type of the names of the namespace:

```java
// urn:ticket:<queue>-<number>, the queue is case-insensitive
class Tickets implements Namespace<Integer> {
    public boolean accepts(NamespaceIdentifier nid) {
        return "ticket".equalsIgnoreCase(nid.toString());
    }

    public Integer read(NamespaceSpecificString nss) throws URNSyntaxError {
        if (!nss.toString().matches("[A-Za-z]+-[0-9]+")) {
            throw URNSyntaxError.syntaxError(specification(), "Not a ticket: " + nss);
        }
        return Integer.valueOf(nss.toString().split("-")[1]);
    }

    public String normalForm(String rfcNormalForm) {
        return rfcNormalForm.toLowerCase(Locale.ROOT);
    }

    public Specification specification() { /* name and URL of the document that defines the namespace */ }
}

NamespaceURN<Integer> ticket = URN.in(new Tickets()).parse("urn:ticket:Support-42");
ticket.name();  // 42
```

The library parses and validates a URN according to RFC 8141 before it asks the namespace. A namespace can therefore only reject URNs and add equivalences. It cannot make a URN valid that violates the syntax, and it cannot tell URNs apart that are equivalent by RFC 8141. Use one instance of a namespace, e.g. a constant: URNs of different instances are not equal unless the namespace implements `equals()`.

## Where to find releases?
The main branch of the repository contains the latest developments. Releases are tagged.
There are two ways of getting releases of this library. Binaries are obtained through Maven dependencies. Sources via the [GitHub Releases download section].

The recommended way is by declaring a dependency, for example in your projects POM file:
```xml
<dependency>
  <groupId>de.slub-dresden</groupId>
  <artifactId>urnlib</artifactId>
  <version>3.0.0</version>
</dependency>
```
## Development

Build and test with Maven and a JDK 11 or later. The CI runs the same command:

```
mvn verify
```

### Design

- **The URN syntax is closed.** The RFCs define what a URN is, so it must be impossible to create a URN that does not conform to one of them. `URN`, `NamespaceIdentifier` and `NamespaceSpecificString` have package-private constructors and validation hooks, and their subclasses are `final`. Java 11 has no `sealed` classes, so this only works as long as these classes share a package. That is why `de.slub.urn` is not divided into sub-packages. `SealedHierarchyTest` fails if a class gets opened.
- **Namespaces are open, but they are not URN types.** A `Namespace` constrains and interprets URNs that the library has validated already. `NamespaceURN` connects the two. `CustomNamespaceTest` checks that a faulty namespace cannot yield an invalid URN.
- **Values are immutable and keep their literal.** `toString()` returns what was parsed. Normalization is explicit and only changes case.
- **`equals()` is not equivalence.** `equals()` and `hashCode()` compare the class and the exact components, `isEquivalentTo()` implements the rules of the RFCs. Percent-encoded octets are never decoded for a comparison.
- **No runtime dependencies.**

### Finding your way

The Javadoc of the package `de.slub.urn` (`package-info.java`) lists the public classes by role. Each of the three roots has one subclass per syntax, with the RFC in its name:

| Root | RFC 2141 | RFC 8141 |
|---|---|---|
| `URN` | `URN_2141` | `URN_8141` |
| `NamespaceIdentifier` | `NID_RFC2141` | `NID_RFC8141` |
| `NamespaceSpecificString` | `NSS_RFC2141` | `NSS_RFC8141` |
| `URNParser` | `RFC2141Parser` | `RFC8141Parser` |

Two classes are internal: `Parsing` splits a URN literal and defines the character classes of the RFCs, `PercentEncoding` validates, encodes and decodes percent-encoded octets.

A literal passes through these steps: a parser splits it with `Parsing`, the constructors of the NID and NSS classes validate their part, and the URN class combines them. For a namespace, `NamespaceURN` then asks the `Namespace` whether it accepts the NID and can read the NSS.

### Where to change what

- **Support another namespace:** implement `Namespace`, do not add a URN type. `de.slub.urn.fdc` is the example. It uses the public API only, and so should any other namespace.
- **Change what a syntax accepts:** the rules are in the validation hook of the NID or NSS class (`validateNamespaceIdentifier`, `isValidURLEncodedNamespaceSpecificString`), in `RQComponent` and `RQF_RFC8141`, and in the character classes of `Parsing`.
- **Support another URN syntax RFC:** this is rare and takes a new version of the library. Add a constant to `RFC`, a subclass of each of the three roots, a parser and a factory method in `URN`, and list the new classes in `SealedHierarchyTest`.

The constructors of `NamespaceIdentifier` and `NamespaceSpecificString` call the validation hook of the subclass. A hook must therefore not use instance fields of its class.

### Tests

There is one test class per class, with test names that read as sentences (`Root_has_no_protected_methods`). `RealWorldURNTest` holds URNs found in the wild, `URNEntryPointsTest` the static methods of `URN`. Tests in `de.slub.urn.extension` and `de.slub.urn.fdc` are outside of the main package on purpose: they prove that something works with the public API.

## Licensing

urnlib is licensed under the [GNU Lesser General Public License, Version 3] (or later), as stated in the source file headers. See [COPYING.LESSER](COPYING.LESSER) and [COPYING](COPYING) for the full license texts.

You can use the library (e.g. the JAR artifact) in any project, open source or proprietary, under any license. If you distribute a modified version of urnlib itself, you have to make the source code of your modifications available under the same license.

[RFC 2141]: https://tools.ietf.org/html/rfc2141
[RFC 8141]: https://tools.ietf.org/html/rfc8141
[RFC 4198]: https://tools.ietf.org/html/rfc4198
[README of version 3.0.0]: https://github.com/slub/urnlib/blob/v3.0.0/README.md
[GitHub Releases download section]: https://github.com/slub/urnlib/releases
[GNU Lesser General Public License, Version 3]: https://www.gnu.org/licenses/lgpl-3.0.html
