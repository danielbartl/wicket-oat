# Changelog

## 0.2.0 (unreleased)

- Sidebar sections and badges: `MenuItem.group(label, items...)` renders a collapsible group of
  links in `OatAppLayout`'s sidebar, open while one of its pages is shown, and
  `MenuItem.withBadge(...)` shows a count or text after a link's label (hidden while `null`, empty
  or zero). The sidebar now has a markup id, so `target.add(sidebar)` updates its badges.
  `MenuItem` gains `badge` and `items` components; its three-argument constructor still works.
- Busy buttons, opt-in: `setBusyIndicator(true)` on `OatButton`, `OatSubmitButton` and `OatDialog`
  (its confirm button) shows a spinner (`aria-busy`) while the Ajax request runs and ignores
  further clicks, so a slow action can't run twice. `Oat.Behaviors.ajaxBusy()` adds the same to
  any Ajax link or button (registered by `WicketOats.install`).
- `OatConfirmDialog` asks before an action runs: `confirm.ask(target, header, message, action)`
  from any Ajax handler, one dialog for any number of buttons, the action kept on the server and
  run at most once. `OatDialog` gains `setHeader(...)`.
- `OatLazyLoadPanel` (`Oat.Components.lazyLoad(...)`): Wicket's `AjaxLazyLoadPanel` with Oat
  skeleton placeholders, loading right after the page.

- Input addons: fields that render a single input (`OatTextField`, `OatNumberField`,
  `OatEmailField`, `OatDateField`, ...) take `setPrefix(...)`/`setSuffix(...)`, shown with Oat's
  input group (`fieldset.group`). Without an addon a field renders as before.
  `Oat.Behaviors.inputGroup()` applies the group to your own `<fieldset>`.
- `OatMoneyField` and `OatPercentField` edit a `BigDecimal` in the user's locale (`1.234,50` /
  `1,234.50`). The money field takes its decimals and symbol from a `Currency` (or an
  `IModel<Currency>`), with the symbol before or after the amount as the locale puts it, and
  both take `setMin`/`setMax`.
- `OatFieldset`, a titled form section: a `Border` on a `<fieldset>` with a legend (looked up by
  id) and an optional description.
- Grid and stack behaviors for Oat's layout classes: `Oat.Behaviors.row()`, `col(span)`,
  `col(span, offset)`, `container()`, `hstack()` and `vstack()`.
- The library's own texts are translated into German, Spanish, French, Italian, Japanese, Dutch
  and Portuguese (`*_<lang>.utf8.properties`), following the component's locale. Apps can still
  override any key in their own properties.
- Single-input fields now extend the new `BaseOatInputField` (itself a `BaseOatField`); component
  paths such as `container:field` are unchanged.

- Two new built-in themes for line-of-business apps, `OatTheme.BUSINESS` and
  `OatTheme.BUSINESS_DARK`: neutral surfaces, a corporate-blue primary, status colors that
  meet WCAG AA contrast, tighter corner radii and tabular figures in tables.
- Compact density, independent of the theme: `OatSettings.setDensity(OatDensity.COMPACT)`
  tightens the spacing scale and body text app-wide (`OatAppLayout` sets `data-density` on
  `<html>`), and `Oat.Behaviors.density(OatDensity.COMPACT)` applies it to one component.
- A component pinned to `OatTheme.LIGHT` or `OatTheme.DARK` now really uses that theme inside
  a page with another theme (before, it kept the page's colors).

## 0.1.1

- Oat UI 0.8.1. An `OatSubmitButton` on an `<input type="submit">` now renders as a button
  rather than a full-width text field, also inside a `fieldset.group`, and `OatDialog`
  content taller than the screen scrolls instead of being cut off.
- `OatAppLayout` renders its top bar as `<header data-topnav>` instead of `<nav data-topnav>`,
  since it holds the app name and custom content, not just navigation. It looks the same;
  update any CSS of your own that targets `nav[data-topnav]`.

## 0.1.0

First public release, published to Maven Central as `dev.jbaby:wicket-oat-core`.

- Apache Wicket 10 components and behaviors for [Oat UI](https://github.com/knadh/oat) 0.8.0,
  created with `new` or through the `Oat.Components` / `Oat.Behaviors` factories.
- General: buttons, badges, avatars, alerts, cards, empty states, feedback panel and toasts.
- Navigation and layout: `OatAppLayout`, accordion, button group, breadcrumb, tabs
  (`OatTabbedPanel`, `OatTabs`), pagination and paging navigators.
- Overlays: `OatDialog` (opened from the server, validates its body) and `OatDropdown`.
- Data display: `OatDataTable`, progress, meter, skeleton and spinner.
- Form fields that behave like the Wicket components they wrap (`CompoundPropertyModel`,
  labels from `.properties` files, inline validation feedback, `aria-*` wiring), from text and
  HTML5 inputs to choices, switches, tag input and file upload.
- 17 built-in themes plus custom themes, persisted in a cookie or a pluggable `OatThemeStore`.
- Works under Wicket's default strict, nonce-based Content Security Policy.
- Requires Java 17+ and Wicket 10.8.0+.
