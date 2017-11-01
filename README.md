# urnlib

[![Build Status](https://travis-ci.org/slub/urnlib.png?branch=master)](https://travis-ci.org/slub/urnlib)
[![Coverage Status](https://coveralls.io/repos/github/slub/urnlib/badge.svg?branch=master)](https://coveralls.io/github/slub/urnlib?branch=master)
[![Maven
Central](https://maven-badges.herokuapp.com/maven-central/de.slub-dresden/urnlib/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.slub-dresden/urnlib)

Java library for representing, parsing and encoding URNs as in [RFC 2141].

This library implements the class `de.slub.urn.URN`, representing an Uniform
Resource Name (URN) and the class `de.slub.urn.URNSyntaxException`.

Further it defines and interface `de.slub.urn.URNResolver` for URN resolving implementations.

[RFC 2141]: https://tools.ietf.org/html/rfc2141
