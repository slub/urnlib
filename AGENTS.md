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
- **The roots of the hierarchy are sealed.** `URN`, `NamespaceIdentifier` and `NamespaceSpecificString` have
  package-private constructors and hooks (Java 11 has no `sealed`), and all leaves are `final`. `SealedHierarchyTest`
  guards this. Code outside the package cannot add a URN type, so the Liskov finding on base constructors concerns
  internal code only.
- **Namespaces are definitions, not URN types.** `Namespace<T>` is the only extension point: a NID rule, an NSS rule
  that yields a typed name, and an optional normal form. The library connects it to a `URN_8141` in the final
  `NamespaceURN<T>` (`URN.in(namespace)`, `urn.as(namespace)`). The library validates the syntax before it asks the
  namespace, and the literal always comes from the held `URN_8141`. The normal form of a namespace is applied on top
  of the RFC normal form, so a namespace can add equivalences but never separate RFC-equivalent URNs (RFC 8141,
  section 3.1). RFC 4198 is built this way in `de.slub.urn.fdc`, with public API only. `CustomNamespaceTest` checks
  that a faulty namespace cannot yield an invalid URN.
- **Errors name any specification.** `URNSyntaxError` refers to a `Specification`. `RFC` implements it for the
  syntaxes and stays closed; a namespace supplies its own.
- **Entry points follow `java.net.URI`.** `URN.parse`, `URN.create` (unchecked) and `URN.of` produce RFC 8141 URNs.
  `URN.rfc2141()` and `URN.rfc8141()` remain for an explicit choice of syntax.

## Findings

### Open/closed

- **The contract of `Namespace.normalForm` is only partly enforced.** `NamespaceURN.normalize()` validates the result
  and throws `IllegalStateException` if it is no valid URN of the namespace. `isEquivalentTo()` trusts it: a normal
  form that throws, returns null or is not idempotent makes equivalence checks fail or give inconsistent answers.
- **`NamespaceURN.equals()` depends on `Namespace.equals()`.** Two instances of the same namespace class yield
  unequal URNs unless the class implements `equals()`. Javadoc and README say so, nothing enforces it.
- **A `NamespaceURN` always follows RFC 8141.** `as(namespace)` parses a `URN_2141` again as RFC 8141. A namespace
  cannot be connected to the RFC 2141 syntax.

### Correctness

- `NamespaceSpecificString.isReservedCharacter` is private, static and follows RFC 2141. With `NOT_ENCODED`,
  `NSS_RFC8141` encodes `/`, `~` and `&` although RFC 8141 allows them literally. `urn:example:a/b` cannot be built
  from an unencoded string: the result is `urn:example:a%2Fb`, which is a different URN because percent-encoded octets
  are never decoded for comparison.

### Liskov

- The base constructors of `NamespaceIdentifier` and `NamespaceSpecificString` call overridable methods
  (`supportedRFC()`, the validation hook) before subclass fields exist. This is not documented.
- `supports(RFC)` is exact equality. `URN_8141.supports(RFC_2141)` is false, although the README calls RFC 8141
  backward compatible.
- Validation hooks differ: `validateNamespaceIdentifier` returns an error message or null,
  `isValidURLEncodedNamespaceSpecificString` returns a boolean. `NID_RFC2141.validateNamespaceIdentifier` is widened
  to `public`.

### Interface segregation

- `URN_8141` and `NamespaceURN` both have `getRQFComponents()` but share no type. Code that holds a plain `URN`
  needs `instanceof` checks for both to reach the r-, q- and f-components.
- `RFC2141Parser` and `RFC8141Parser` both implement `parse(URI)` as `parse(Parsing.literalOf(uri))`.
- `supports(RFC)` is implemented identically in `URN`, `NamespaceIdentifier` and `NamespaceSpecificString`.

### Dependency inversion

- `URN` depends on the concrete parsers, which depend on the concrete URN types.
- `Parsing` and `PercentEncoding` are package-private. A `Namespace` outside the package cannot reuse
  percent-encoding validation or `pchar` checks. `de.slub.urn.fdc` gets by because the ResourceId of RFC 4198 has
  exactly the character set of an RFC 2141 NSS, so it validates and encodes through `NSS_RFC2141`. A namespace with
  another character set has to write its own checks.

### Usability

- Three naming schemes: `URN_8141`, `NID_RFC8141`, `RFC8141Parser`. `getRQFComponents()` sits next to
  `namespaceIdentifier()` and `fragment()`. `URNSyntaxError` and `URNResolvingError` are checked exceptions.
  `ErrorTypes` is a plural enum name.
- Only `URN.create` is unchecked. The constructors of the NID and NSS types, `URN.of(String, String)` and the
  parsers of `URN.in(namespace)` throw a checked exception, also for constants.
- There is no `withFragment`/`withQuery`, and `URN.of` takes no r-, q- or f-components. The constructor of
  `RQF_RFC8141` takes an `Optional` parameter.
- `URN.of(String, String)` takes the NSS URL encoded only. Building from an unencoded NSS needs
  `new NSS_RFC8141(nss, NOT_ENCODED)`, which is subject to the Correctness finding.
- No parse method detects the namespace of a URN. The caller chooses it up front with `URN.in(namespace)`; there is
  no registry of namespaces.
- `NamespaceURN<FederatedContentName>` is long to write, and `FederatedContent` offers no `parse` shortcut.
- `normalize()` repeats a `catch (URNSyntaxError) → IllegalStateException` block in the four NID and NSS classes.
- `URNResolvingError.failedUrn` is not final, and no constructor takes message, URN and cause together.
