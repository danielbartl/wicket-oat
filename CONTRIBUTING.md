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

## Releasing

Releases go to Maven Central (namespace `dev.jbaby`) from the `Release`
workflow, which runs when a `v*` tag is pushed. It needs these repository
secrets:

- `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD`: a user token generated
  on [central.sonatype.com](https://central.sonatype.com/account)
- `MAVEN_GPG_KEY`: the ASCII-armored private signing key
  (`gpg --armor --export-secret-keys <key-id>`), whose public key is on a
  keyserver such as `keys.openpgp.org`
- `MAVEN_GPG_PASSPHRASE`: that key's passphrase

To release:

1. Set the release version (`./mvnw versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false`),
   update the version in the README and `CHANGELOG.md`, and commit.
2. Tag and push: `git tag vX.Y.Z && git push origin vX.Y.Z`. The workflow checks
   that the tag matches the project version, publishes `wicket-oat-parent` and
   `wicket-oat-core`, and creates the GitHub release.
3. The deployment waits on [central.sonatype.com](https://central.sonatype.com/publishing/deployments)
   until you click **Publish**.
4. Set the next development version (e.g. `X.Y+1.0-SNAPSHOT`) and commit.
