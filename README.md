# urnlib

![Build](https://github.com/slub/urnlib/actions/workflows/ci.yml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/slub/urnlib/badge.svg?branch=main)](https://coveralls.io/github/slub/urnlib?branch=main)
![Maven Central Version](https://img.shields.io/maven-central/v/de.slub-dresden/urnlib)


Java library for representing, parsing and encoding URNs as specified in [RFC 2141] and [RFC 8141].

The initial URN RFC 2141 of May 1997 was superseded by RFC 8141 in April 2017. RFC 8141 added support for path characters and optional request, query and fragment parts. While URNs look very similar to URIs and are somewhat related, different equality rules apply for URNs in regard to URN character encoding rules. Also some synactic rules about case-insensitivity are actually to be defined by the implementing system handling the URNs.

This library provides classes for representing, parsing and constructing an Uniform
Resource Name (URN) and its parts Namespace Identifier (NID), Namespace Specific String (NSS) and optional Resolution, Query and Fragment components (RQF). If you don't need RFC 2141 URNs explicitly, always use RFC 8141 URNs because that is the current valid specification and RFC 8141 is backward compatible to RFC 2141.

Further it defines and interface `de.slub.urn.URNResolver` for URN resolving implementations.

The library is compiled for Java 9.

## Usage

```java
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxError;
import de.slub.urn.URN_8141;

import static de.slub.urn.RFC.RFC_8141;

class Demo {
    public void foo() throws URNSyntaxError {
        // Create a RFC 2141 compliant URN (not recommended)
        URN urn1 = URN.rfc2141().parse("urn:examle:1234");

        // Create a RFC 8141 compliant URN (recommended)
        URN urn2 = URN.rfc8141().parse("urn:examle:foo/1234?=cq=cz#bar");

        // Access fragment part of a supposedly RFC 8141 compliant URN
        if (urn2.supports(RFC_8141)) {
            String fragment = ((URN_8141) urn2).getRQFComponents().fragment();
        }
    }
}
```

## Where to find releases?
The master branch of the repository contains the latest developments. Releases are tagged.
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
[GitHub Releases download section]: https://github.com/slub/urnlib/releases
[GNU Lesser General Public License, Version 3]: https://www.gnu.org/licenses/lgpl-3.0.html
