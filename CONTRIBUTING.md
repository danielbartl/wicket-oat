# Contributing to Wicket Oat

## Building and testing

```bash
./mvnw test
```

This builds both `wicket-oat-core` (the library) and `wicket-oat-examples`
(the demo app) and runs their test suites.

## The showcase and the browser checks

The examples app's `/showcase` page shows every component as the website's component
showcase does, rendered by the library: `/showcase?theme=nord` in one theme,
`?density=compact` compact, `?scenario=button,badge` only those. Its scenarios are in
`Scenarios` (in `wicket-oat-examples`): markup with `wicket:id`s and the components that
fill it.

Two kinds of test use it:

- `ShowcasePreviewsTest`, part of `./mvnw test`, renders every scenario and writes their
  markup to `wicket-oat-examples/target/showcase/previews.json`. With
  `-Dshowcase.site=../wicket-oat-site/index.html` (a checkout of the `gh-pages` branch) it
  puts each preview into the website's card with the same `data-preview` attribute.
- `ShowcaseBrowserTest` opens the page in Chromium with Playwright. It fails on any
  WCAG 2.2 AA violation axe-core finds, in every built-in theme, on any console error
  (such as a style or script the Content Security Policy blocked), and on a control
  that shows no focus when tabbed to, in the light and dark themes and with forced
  colors. It runs in CI, or with

  ```bash
  ./mvnw install -DskipTests -pl wicket-oat-core -am
  ./mvnw test -pl wicket-oat-examples -Pbrowser
  ```

  Playwright downloads its Chromium the first time; set `BROWSER_EXECUTABLE` to use
  another one. Themes in its `CONTRAST_TO_FIX` list don't meet the contrast yet: their
  contrast violations are printed rather than failed, until their colors are fixed.

A new component gets a scenario, so the checks cover it.

## Running the examples app

See the "Running the Examples" section of the [README](README.md#running-the-examples).

## Pull requests

- Keep PRs focused on a single change.
- Include tests for new or changed behavior — most components are tested via
  `WicketTester` in `wicket-oat-core/src/test/java`; follow the style of the
  existing tests in the same package.
- Make sure `./mvnw test` passes before opening a PR.
- Texts a component renders itself are resources in a `.properties` file next to
  it, with a `_<lang>.utf8.properties` translation for each supported language
  (`de`, `es`, `fr`, `it`, `ja`, `nl`, `pt`). `TranslationsTest` fails if a key
  or a file is missing, so add a translation with every new text.

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
   update the version in the README, the examples' Getting Started page
   (`GettingStartedPage.html`) and `CHANGELOG.md`, and commit.
2. Tag and push: `git tag vX.Y.Z && git push origin vX.Y.Z`. The workflow checks
   that the tag matches the project version, publishes `wicket-oat-parent` and
   `wicket-oat-core`, and creates the GitHub release.
3. The deployment waits on [central.sonatype.com](https://central.sonatype.com/publishing/deployments)
   until you click **Publish**.
4. Set the next development version (e.g. `X.Y+1.0-SNAPSHOT`) and commit.
