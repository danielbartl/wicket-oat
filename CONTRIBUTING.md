# Contributing to Wicket Oat

## Building and testing

```bash
./mvnw test
```

This builds both `wicket-oat-core` (the library) and `wicket-oat-examples`
(the demo app) and runs their test suites.

## Running the examples app

See the "Running the Examples" section of the [README](README.md#running-the-examples).

## Pull requests

- Keep PRs focused on a single change.
- Include tests for new or changed behavior — most components are tested via
  `WicketTester` in `wicket-oat-core/src/test/java`; follow the style of the
  existing tests in the same package.
- Make sure `./mvnw test` passes before opening a PR.
