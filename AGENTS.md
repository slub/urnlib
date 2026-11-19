# Guidance for agents working on urnlib

This file records the findings of a SOLID and API review of the code base (2026-11-19, at commit `0e2c8ab`). Read it
before changing public API or adding a URN type. Apart from the design position below, it records findings only; no
decisions have been made on them.

Findings name classes and methods instead of line numbers. Check that a finding still holds before acting on it, and
update or delete it once it is resolved.

## Design position

Set by the maintainer on 2026-11-19. Findings are measured against it.

- **URN syntax is closed.** The RFCs are normative, so it must be impossible to construct a URN type that does not
  conform to them, as with `java.net.URI`. A new URN syntax requires a new version of the library. A closed `RFC` enum
  and one factory per syntax RFC are therefore intended.
- **Namespaces are open, but outside the URN hierarchy.** Rules of a namespace must not add URN types the way
  RFC 4198 did.

## Strengths

- **Value types are immutable.** `toString()` returns a parsed literal exactly as it was given.
- **`equals()` is not equivalence.** `equals()`/`hashCode()` compare the same class and exact components.
  `isEquivalentTo()` implements the equivalence of the RFCs and is asymmetric across types by design.
  `URN.areEquivalent()` is the symmetric form. The difference is documented throughout.
- **Percent-encoded octets are never decoded for comparison.** Normalization only changes case.
- **No runtime dependencies.** The library targets Java 11.

## Core finding

The type hierarchy is organised by RFC (`URN_2141`, `URN_8141`, `NID_RFC…`, `NSS_RFC…`), but URNs vary by
**namespace**. RFC 4198 defines a namespace, not a syntax, and had to be forced into the RFC axis: `URN_4198` and
`NSS_RFC4198` duplicate large parts of their RFC 8141 counterparts. Every further namespace (`nbn`, `isbn`, `uuid`)
would pay the same cost.

## Findings

### Open/closed

- **The roots of the hierarchy are not sealed.** Only the leaves are `final`. `URN`, `NamespaceIdentifier` and
  `NamespaceSpecificString` are public and abstract, with accessible constructors and abstract validation hooks, as
  they were in v3.0.0. `ThirdPartyExtensionTest` shows the consequence: `CustomNSS` decides alone what a valid NSS is,
  `CustomURN.toString()` can return any string, and all three custom types claim `RFC.RFC_2141`. A `URN` instance is
  therefore not guaranteed to conform to an RFC. Java 11 has no `sealed`, so sealing means package-private
  constructors. That breaks subclasses written against 3.x and removes `ThirdPartyExtensionTest`. With sealed roots,
  the Liskov finding on base constructors and the Dependency inversion finding on package-private helpers concern
  internal code only.
- **RFC 4198 is a namespace inside the syntax hierarchy.** `URN_8141` and `NSS_RFC8141` are rightly `final`, so
  `URN_4198` re-implements the RQF field, `equals`, `hashCode`, `toString` and `assignedName`, and `NSS_RFC4198` copies
  the validity predicate of `NSS_RFC8141`. `URN.rfc4198()` puts a namespace next to the syntax factories. RFC 8141
  lets a namespace definition constrain the NSS (section 2.2) and add rules for URN-equivalence that never make
  equivalent URNs different (section 3.1). A type that holds a `URN_8141` instead of extending `URN` can express both.
- **Namespace errors depend on the closed `RFC` enum.** `NSS_RFC4198` reports its errors with `RFC.RFC_4198`, so each
  further namespace would add an enum constant. Namespace violations have no other way to name their specification.

### Correctness

- `NamespaceSpecificString.isReservedCharacter` is private, static and follows RFC 2141. With `NOT_ENCODED`,
  `NSS_RFC8141` encodes `/`, `~` and `&` although RFC 8141 allows them literally. `urn:example:a/b` cannot be built
  from an unencoded string: the result is `urn:example:a%2Fb`, which is a different URN because percent-encoded octets
  are never decoded for comparison.

### Liskov

- The base constructors of `NamespaceIdentifier` and `NamespaceSpecificString` call overridable methods
  (`supportedRFC()`, the validation hook, `normalForm()`) before subclass fields exist. Only `normalForm` documents it.
- `supports(RFC)` is exact equality. `URN_8141.supports(RFC_2141)` and `URN_4198.supports(RFC_8141)` are both false,
  although the README calls RFC 8141 backward compatible and `URN_4198` follows RFC 8141 syntax.
- Validation hooks differ: `validateNamespaceIdentifier` returns an error message or null,
  `isValidURLEncodedNamespaceSpecificString` returns a boolean. `NSS_RFC4198` therefore validates its structure after
  `super()`. `NID_RFC2141.validateNamespaceIdentifier` is widened to `public`.

### Interface segregation

- `URN_8141` and `URN_4198` both have `getRQFComponents()` but share no type. The README example needs
  `supports(RFC_8141)` plus a downcast, and that check fails for `URN_4198`.
- All three parsers implement `parse(URI)` as `parse(Parsing.literalOf(uri))`.
- `supports(RFC)` is implemented identically in `URN`, `NamespaceIdentifier` and `NamespaceSpecificString`.

### Dependency inversion

- `URN` depends on the concrete parsers, which depend on the concrete URN types.
- `Parsing`, `PercentEncoding` and `RQF_RFC8141.parse` are package-private. An extender cannot reuse percent-encoding
  validation, `pchar` checks, splitting, or r/q/f parsing.

### Usability

- Three naming schemes: `URN_8141`, `NID_RFC8141`, `RFC8141Parser`. `getRQFComponents()` sits next to
  `namespaceIdentifier()` and `fragment()`. `URNSyntaxError` and `URNResolvingError` are checked exceptions.
  `ErrorTypes` is a plural enum name.
- Every construction throws a checked exception, also for constants. There is no unchecked factory like
  `URI.create`.
- Building from parts is verbose. There is no `of(nid, nss)` and no `withFragment`/`withQuery`. The constructor of
  `RQF_RFC8141` takes an `Optional` parameter.
- `URN_2141` and `URN_8141` return the base type from `namespaceIdentifier()` and `namespaceSpecificString()`, so
  `NID_RFC8141.isFormal()` needs a cast. `URN_4198` narrows its NSS but not its NID.
- No parse method detects the most specific type; the caller chooses the RFC up front.
- `normalize()` repeats a `catch (URNSyntaxError) → IllegalStateException` block in five classes.
- `URNResolvingError.failedUrn` is not final, and no constructor takes message, URN and cause together.
