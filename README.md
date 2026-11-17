# urnlib

![Build](https://github.com/slub/urnlib/actions/workflows/ci.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/slub/urnlib/badge.svg?branch=main)](https://coveralls.io/github/slub/urnlib?branch=main)
![Maven Central Version](https://img.shields.io/maven-central/v/de.slub-dresden/urnlib)


Java library for representing, parsing and encoding URNs as specified in [RFC 2141] and [RFC 8141]. It also supports the Federated Content URNs of the `fdc` namespace as specified in [RFC 4198].

The initial URN RFC 2141 of May 1997 was superseded by RFC 8141 in April 2017. RFC 8141 added support for path characters and optional request, query and fragment parts. While URNs look very similar to URIs and are somewhat related, different equality rules apply for URNs in regard to URN character encoding rules. Also some syntactic rules about case-insensitivity are actually to be defined by the implementing system handling the URNs.

This library provides classes for representing, parsing and constructing a Uniform
Resource Name (URN) and its parts Namespace Identifier (NID), Namespace Specific String (NSS) and optional Resolution, Query and Fragment components (RQF). If you don't need RFC 2141 URNs explicitly, always use RFC 8141 URNs because that is the current valid specification and RFC 8141 is backward compatible to RFC 2141.

Further it defines an interface `de.slub.urn.URNResolver` for URN resolving implementations.

The library is compiled for Java 11.

## Usage

```java
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxError;
import de.slub.urn.URN_4198;
import de.slub.urn.URN_8141;

import java.util.Optional;

import static de.slub.urn.RFC.RFC_8141;

class Demo {
    public void foo() throws URNSyntaxError {
        // Create a RFC 2141 compliant URN (not recommended)
        URN urn1 = URN.rfc2141().parse("urn:example:1234");

        // Create a RFC 8141 compliant URN (recommended)
        URN urn2 = URN.rfc8141().parse("urn:example:foo/1234?=cq=cz#bar");

        // Access fragment part of a supposedly RFC 8141 compliant URN
        if (urn2.supports(RFC_8141)) {
            Optional<String> fragment = ((URN_8141) urn2).getRQFComponents().fragment();
        }

        // Create a RFC 4198 compliant Federated Content URN, see below
        URN_4198 urn3 = URN.rfc4198().parse("urn:fdc:example.net:200406:ivr:51089");
    }
}
```


### Normalization

`toString()` returns a parsed URN as it was given. `normalize()` returns the normal form according to the type of the URN, which is what equivalence compares. `assignedName()` drops everything but the Namespace Identifier and the Namespace Specific String.

```java
URN urn = URN.rfc8141().parse("URN:Example:a%2c?=q");
urn.toString();                             // urn:Example:a%2c?=q
urn.normalize().toString();                 // urn:example:a%2C?=q
urn.normalize().assignedName().toString();  // urn:example:a%2C

URN.rfc4198().parse("urn:fdc:Example.COM:2002:%ff").normalize().toString();  // urn:fdc:example.com:2002:%FF
```

Equivalent URNs of the same type have equal normalized assigned-names. Use `urn.normalize().assignedName()` as the key if a `HashMap` or `HashSet` is to treat equivalent URNs as the same.

### Federated Content URNs (RFC 4198)

[RFC 4198] registers the `fdc` namespace and structures its Namespace Specific String as `<ProviderId>:<DateId>:<ResourceId>`. `URN_4198` is a URN type of its own for this namespace. It validates the structure, gives access to the fields and knows the equivalence rule of the namespace: the case of the ProviderId does not matter.

```java
URN_4198 fdc = URN.rfc4198().parse("urn:fdc:example.net:200406:ivr:51089");
fdc.namespaceSpecificString().providerId();  // "example.net"
fdc.namespaceSpecificString().dateId();      // "200406"
fdc.namespaceSpecificString().date();        // 2004-06-01
fdc.namespaceSpecificString().resourceId();  // "ivr:51089"

// Convert a URN of another type
URN_4198 converted = URN_4198.from(URN.rfc8141().parse("urn:fdc:example.net:200406:ivr:51089"));

// Build a new one, the ResourceId gets percent-encoded where needed
URN_4198 built = URN_4198.of("example.org", "20010527", "img089322-038");
```

Resolution, query and fragment components of RFC 8141 are accepted and available via `getRQFComponents()`.

Parsing, converting and building throw a `URNSyntaxError` if the URN does not follow RFC 4198:

```java
URN.rfc4198().parse("urn:example:example.net:2004:a");    // Namespace Identifier is not fdc
URN.rfc4198().parse("urn:fdc:localhost:2004:a");          // ProviderId is not a domain name
URN.rfc4198().parse("urn:fdc:example.net:20040231:a");    // DateId is not a calendar date
URN.rfc4198().parse("urn:fdc:example.net:04:a");          // 1 to 3 digit DateIds are reserved
URN.rfc4198().parse("urn:fdc:example.net:2004");          // ResourceId is missing
```

The DateId has the form `CCYY`, `CCYYMM` or `CCYYMMDD`. `date()` fills in `01` for a missing month or day. The ResourceId may contain colons, only the first two colons of the Namespace Specific String separate the fields.

A `URN_8141` does not know the rules of the `fdc` namespace. So the answer to an equivalence check depends on which URN is asked, unless both are asked:

```java
URN_4198 fdc     = URN.rfc4198().parse("urn:fdc:Example.NET:200406:ivr:51089");
URN_8141 generic = URN.rfc8141().parse("urn:fdc:example.net:200406:ivr:51089");

fdc.equals(generic);               // false, different classes
fdc.isEquivalentTo(generic);       // true, by the rules of RFC 4198
generic.isEquivalentTo(fdc);       // false, by the general rules of RFC 8141
URN.areEquivalent(generic, fdc);   // true
```

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
## Licensing

urnlib is licensed under the [GNU Lesser General Public License, Version 3] (or later), as stated in the source file headers. See [COPYING.LESSER](COPYING.LESSER) and [COPYING](COPYING) for the full license texts.

You can use the library (e.g. the JAR artifact) in any project, open source or proprietary, under any license. If you distribute a modified version of urnlib itself, you have to make the source code of your modifications available under the same license.

[RFC 2141]: https://tools.ietf.org/html/rfc2141
[RFC 8141]: https://tools.ietf.org/html/rfc8141
[RFC 4198]: https://tools.ietf.org/html/rfc4198
[GitHub Releases download section]: https://github.com/slub/urnlib/releases
[GNU Lesser General Public License, Version 3]: https://www.gnu.org/licenses/lgpl-3.0.html
