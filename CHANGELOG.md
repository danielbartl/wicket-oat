# Changelog

## 0.3.0 (unreleased)

- The examples app no longer needs MongoDB or Docker, and a `Dockerfile` and `railway.toml`
  deploy it as a public demo (deployment mode, `PORT`, HTTPS behind a proxy, 5 MB uploads).
  Its upload demo has an Upload button and shows what was received.

## 0.2.0 (2026-10-10)

A large release for business applications: CRUD, trees, data table selection, row actions
and editing in place, page building blocks, a wizard, charts, icons, menus, new form fields,
the Business themes, compact density, and translations of every text into seven languages.
See the [user guide](https://danielbartl.github.io/wicket-oat/guide.html) for all of it.

### Upgrading from 0.1.x

No public API was removed, so existing code compiles unchanged. A few changes can affect
an app's own CSS or subclasses:

- `OatDataTable` renders each row in a `<tbody>` of its own (to keep a row and its details
  row together). Component paths are unchanged; CSS selectors such as `tbody > tr:nth-child(...)`
  written for the old structure may need adjusting.
- `wicket-oat.css` hides Wicket's Ajax placeholders (`[data-wicket-placeholder] { display: none }`),
  as Oat's display rules for buttons could otherwise show them as empty buttons.
- Single-input fields extend a new `BaseOatInputField` (itself a `BaseOatField`). Subclasses of
  `BaseOatField` keep working; component paths such as `container:field` are unchanged.
- `OatStatCard`'s figure uses proportional digits instead of tabular ones.
- Oat UI 0.8.3 (was 0.8.1) changes two things: alerts get a thick left border in their
  variant's color, and a tooltip is now a popover. While it shows, Oat inserts a
  `<span class="ot-tooltip">` right after the element, which sibling selectors such as
  `:last-child` in your own CSS can notice.

### Changes

- Oat UI 0.8.3: tooltips are no longer cut off by scrolling containers or hidden behind
  dialogs, and alerts have a colored left border.
- `OatCrud`: a table of records with New, Edit and Delete, an edit dialog whose fields
  bind through a `CompoundPropertyModel` and change the record only on a valid save, and
  a confirmation before deleting.
- `OatTree` and `OatTableTree`: Wicket's nested tree and tree grid with Oat styling instead
  of the image themes, keyboard-accessible expand buttons (`aria-expanded`, named per node),
  icons and selection.
- Data table extras: `OatRowDetailsColumn` (a full-width details panel under a row),
  `OatEditableColumn` (a cell edited in place, converted and validated, Enter to save,
  Escape to cancel), `setColumnChooser(true)` and `setCsvExport("file.csv")`
  (`OatCsvExport`: every row in the current sort order, machine-readable values, formula
  injection defused). `OatDataTable` now renders each row in a `<tbody>` of its own.
- `OatMultiSelectField`: several values chosen from server-side suggestions, shown as chips.
- Fix: `OatAutoCompleteField` and `OatMultiSelectField` work inside an `OatDialog` or
  `OatPopover`. Wicket's suggestion list opened behind the modal dialog; `wicket-oat.js` now
  moves it into the dialog and shows it in the top layer above it, and Escape closes the
  list before the dialog.
- `ShowsErrorsInline`: containers that show a component's error themselves are skipped by
  `NotShownInlineFilter`, so a toast doesn't repeat it.
- `SerializableSupplier` and `ObjectModel` (a model for objects that aren't `Serializable`
  by type) in `dev.jbaby.wicket.oat.util`.

- Charts in `dev.jbaby.wicket.oat.components.chart`: `OatBarChart`, `OatColumnChart`,
  `OatLineChart` (up to three series), `OatDonutChart` (up to six parts, the rest as "Other")
  and `OatSparkline`, rendered as SVG on the server. No JavaScript; a colorblind-safe palette
  stepped for light and dark themes (`--oat-chart-1` to `--oat-chart-8`); values on hover;
  and every chart's numbers in a "Data" table for screen readers. `OatStatCard.setTrend(...)`
  draws a sparkline under the figure.
- `contextMenu()` (`ContextMenuBehavior`): a menu of actions on right-click, Shift+F10 or the
  Menu key, navigable with the arrow keys.
- `OatMenuBar`: a row of commands, made of buttons, links and menus.
- `OatSplitLayout`: two panes side by side or stacked, the first resizable by dragging its corner.
- `OatLoadMoreList`: a long list that appends a batch at a time with "Load more", optionally
  as the user scrolls.
- `OatIcon`: 64 inline-SVG icons from Lucide (ISC license), plus your own with `register`.
- `OatCookieConsent`: a banner asking for consent to optional cookies, remembered for a year.
- `wicket-oat.js`: a small script of the library's own, rendered only by the context menu
  and a load-on-scroll list. It reads `data-` attributes, so the strict CSP still holds.
- Fix: a hidden component's Ajax placeholder no longer shows as an empty button. Oat's
  display rules had overridden the browser's `[hidden]`.
- Fix: `OatStatCard` shows its figure with proportional digits, so large numbers keep their
  natural spacing.

- `OatLoginForm`: a sign-in form with password-manager hints, an optional remember-me
  checkbox and forgot-password link, a failure message that doesn't say which part was wrong,
  and a new session id after signing in. Sign-in is a callback, so it works with any session
  class or security framework.
- `OatMasterDetail`: a list beside the selected item's details, one at a time on phones.
- `OatMessageList` and `OatMessageInput`: comments or notes with avatar, author, time and
  text, and a box to add one.
- `OatCustomField`: an Oat label, hint, inline errors and aria wiring for an input of your
  own made of several inputs.

- `OatWizard`: a form over several steps, with numbered steps, Next validating the current step,
  Back and finished steps' numbers going back, and `onNext`/`onFinish` hooks.
- `OatAutoCompleteField`: Wicket's `AutoCompleteTextField` with an Oat-styled suggestion list,
  looking up objects on the server and storing the chosen one (or free text, if allowed).
- `OatDateRangeField` with `DateRange`: two date inputs, open ends allowed, the order checked.
- `OatTimeline`: events in order with a colored marker, title, localized time and description.
- `OatDescriptionList` and `OatStatCard` show `Instant`s in the server's zone.
- Input groups (field addons, `OatDateRangeField`) shrink with a narrow column instead of
  overflowing it.

- Page building blocks: `OatPageHeader` (a `<header>` border with breadcrumb, title, subtitle and
  your actions), `OatStatCard` (a key figure with its change as a colored badge), `OatDescriptionList`
  (a record's fields as label/value pairs on a `<dl>`), `OatSplitButton` (the usual action plus a
  menu of related ones) and `OatPopover` (a button opening a panel with any content).
- Single-input fields take `setSuggestions(...)`, offering values from a native `<datalist>`.
- `wicket-oat.css` lays out all of Wicket's own tags in development mode (`<wicket:panel>`,
  `<wicket:container>`, ...) as if absent, as in production, so grid and flex layouts match.

- Fix: `OatAppLayout` pages now have a viewport meta tag. Without it, phones rendered them at
  desktop width and Oat's responsive layout (the collapsing sidebar) never applied.

- Data table columns in `dev.jbaby.wicket.oat.components.table`: `OatLinkColumn` (to a page or an
  Ajax action), `OatBadgeColumn` (e.g. a status, colored per row), `OatNumberColumn` (locale
  formatted, right-aligned, optionally an amount in a fixed or per-row currency), `OatDateColumn`
  (any `java.time` value in the locale's style), `OatBooleanColumn` (a read-only checkbox) and
  `OatActionsColumn` (a "⋯" menu of actions per row, with danger actions and per-row visibility).
- Row selection: `OatSelectionColumn` adds checkboxes and a "select all on this page" box, keeping
  the selection in your model across pages and sorting. `OatDataTable` then shows a bar with the
  count and its bulk actions (`addBulkAction(...)`), and `clearSelection(target)`.
- `OatDataTable`: a toolbar slot above the table (`setToolbar(...)`, e.g. for search), sort arrows
  and `aria-sort` on sortable headers, and a markup id so `target.add(table)` re-renders it.
  `OatDropdown` gains `getTrigger()`. The new texts are translated like the others.

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
