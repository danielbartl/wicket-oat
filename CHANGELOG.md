# Changelog

## 0.4.0 (unreleased)

- `OatSteps`: where a process stands, as numbered steps joined by a line - done,
  current and still to come - such as an order from "Ordered" to "Delivered". The
  steps already done can be links back (`setOnSelect`). `OatWizard` now draws its
  steps with it, so in a test a step's link is at `"steps:list:items:0:link"` instead of
  `"steps:0:link"`.
- `OatList`: rows with a title and a line under it, something in front (an avatar,
  an icon) and at the end (a badge, a button), as phone apps list things. A row can
  be a link (`setOnClick`), and `setEmptyState` shows a component while there are
  no rows. The time-off demo's Today tab uses it.
- `OatIndicator`: a count or a status dot on a corner of what it wraps - unread
  messages on a link, a person's presence on their avatar. `OatStatus` is the dot on
  its own, colored by an `OatVariant` and named for screen readers.
- `OatRatingField`: a rating chosen by clicking a star, one to five or up to ten
  (`setMax`). The stars are radio buttons, so the arrow keys move between them,
  screen readers read "3 stars", and a test sets `"rating:input"` to `"4"`.
  `OatRating` shows a rating as stars, rounded to half a star, read as "4.5 out of 5
  stars".
- `OatRadialProgress`: a ring that fills up, with the percentage or a text of yours
  in the middle, in a variant's color; a `progressbar` for screen readers.
- `<kbd>` is styled as a key to press, and `oat-divider` draws a line with a word in
  the middle, such as "or" between two ways to sign in.

- Built and tested with Wicket 10.11.0 (was 10.8.0), which fixes a thread leak in
  Wicket's file cleaner (WICKET-7186) and autocomplete fields (WICKET-7182).
- `OatSelectionColumn` selecting by key can load the selected rows by their keys:
  `new OatSelectionColumn<>(selectedIds, Invoice::getId, ids -> invoices.findAllById(ids))`,
  with any `Iterable` the loader returns, as Spring Data's repositories do. Bulk actions
  then get every selected row, also those a search hides, without reading the whole
  table. Without the loader, the rows are looked up through the data provider as before,
  which finds only those it returns at the time.
- `OatNavigationToolbar` and `OatHeadersToolbar`, the paging row and the `aria-sort`
  column headers of `OatDataTable` and `OatTableTree`, over Ajax, for a `DataTable` of
  your own.
- `NestedFormButtonBehavior` renders an Ajax submit button as `type="button"` while its
  form is nested in another, so Enter in a field of the outer form can't click it.
  `OatSubmitButton` has it, and so do the buttons of `OatDialog`, `OatWizard` and
  `OatEditableColumn`.
- `OatConfirmDialog` ignores clicks on Confirm while an answer is being handled (its busy
  indicator is on by default), so a double-click can't also answer a question the
  action asks next.

- Fix: with a provider that can't sort, `OatTableTree` paged only on the server and the
  browser kept showing the first page, because nothing gave the table the markup id
  the Ajax update replaces it by.
- Fix: a dialog opened again in `onConfirm` - `OatConfirmDialog.ask(...)` asking a
  second question, `OatCrud.edit(...)` for the next record in `setOnSave` - was closed
  right after, and `onClose` dropped what had just been set up.
- Fix: an `OatDialog` closed and opened again in one request, e.g. by `onClose`, was
  closed in the browser while the server took it as open.
- Fix: an `OatDialog` on `<wicket:container>` couldn't be opened from the server in
  deployment mode, which leaves that tag out: `open` now re-renders the `<dialog>`
  inside it there.
- Fix: in a dialog, wizard, editable cell, message input or login form inside a page's
  form, Enter in a page field could click a hidden button of theirs, e.g. open a closed
  dialog or run its `onConfirm`. Enter in their own fields still works as before, and
  `OatLoginForm` now signs in on Enter also when it is nested.
- Fix: the primary item of `OatBottomNav` got `aria-current="page"` on its own page,
  though it is an action, not a tab.
- Fix: `OatMenuBar` marked every link to the current page's class as current, also
  those with other page parameters; it now uses the sidebar's rule.
- `OatDataTable.addTopToolbar` adds toolbars below the column headers, not above as
  its documentation said.
- Examples: searching products re-rendered the search field with the rows, losing
  keystrokes; the "Time off" demo counted a person with overlapping absences twice
  and took "today" from the server's time zone instead of Vienna's.

## 0.3.0 (2026-10-13)

A release that makes Wicket Oat work the way Wicket users expect: dialogs like Wicket's
`ModalDialog`, data tables sorting and paging over Ajax, form fields with short paths for
`FormTester`, lambda factories on the component classes like `AjaxLink.onClick(...)`, and
`newXxx` hooks. It also brings a bottom tab bar and a layout for phone apps, a skip link and
the page's language in the layouts, and fixes for signing in and for dialogs inside forms.
Some of these changes break existing code; the notes below say what to change.

### Upgrading from 0.2.x

This release changes form fields, dialogs and data tables to work more like the Wicket
components they wrap, which breaks some code and tests:

- A form field's wrapped form component is now its child `input`, so its path is
  `"email:input"` instead of `"email:container:field"`, in `FormTester.setValue(...)` and
  `get(...)`, and the input's `name` is `email:input`. The label and feedback are children
  of the field too (`"email:label"`, `"email:feedback"`).
- `BaseOatField.getField()` is now `getInput()`, and the factory method subclasses
  implement, `createFormComponent(id, model)`, is now `newInput(id, model)`. The protected
  `container` field is gone; a subclass adds its components to the field itself.
- `OatDialog` follows Wicket's `ModalDialog`: `setBody(...)` and `BODY_ID` are now
  `setContent(...)` and `CONTENT_ID`, so a form field in it is at `"content:name:input"`.
  `open`, `close` and `OatConfirmDialog.ask` take an `AjaxRequestTarget` instead of an
  `IPartialPageRequestHandler`. The dialog's own tag is now rendered, with the class
  `oat-dialog`, which `wicket-oat.css` gives no box of its own, so the layout doesn't
  change; in a test, `getTagByWicketId(...)` with the dialog's id now finds that tag.
- `Oat.Components` keeps only the factories for components driven by a callback: `button`,
  `submitButton`, `accordion`, `tabs`, `dropdown`, `buttonGroup`, `breadcrumb`, `pagination`,
  `avatarGroup`, `wizard`, `loginForm` and `lazyLoad`. The other 225 methods only repeated a
  constructor (`alert`, `badge`, `dialog`, `dataTable`, every form field, ...); call the
  constructor with the same arguments instead, e.g. `new OatAlert(...)` or
  `new OatTextField<>(...)`.
- Hooks follow Wicket's `newXxx(id)` naming: `OatAppLayout`'s `createTopNavExtra` and
  `createFooter` are now `newTopNavExtra` and `newSidebarFooter`, and `OatMobileLayout`'s
  `createBottomNav` is `newBottomNav`. Their `topNav()`, `sidebar()` and `sidebarMenu()`
  build steps are private, and the `sidebar` and `bottomNav` fields are reached with
  `getSidebar()` and `getBottomNav()`.
- Setters taking a callback no longer look like Wicket's overridable `onXxx` methods:
  `OatCrud.onSave`/`onDelete`, `OatEditableColumn.onSave`, `OatTree.onSelect`,
  `OatTableTree.onSelect` and `OatCookieConsent.onAnswer` are now `setOnSave`,
  `setOnDelete`, `setOnSelect` and `setOnAnswer`.
- `OatDataTable` and `OatTableTree` sort and page over Ajax, re-rendering only the table,
  with plain links as the fallback without JavaScript. Their navigation toolbar holds an
  `OatAjaxPagingNavigator` instead of an `OatPagingNavigator`. A test clicking a page or
  sort link now gets an Ajax response.

### Changes

- `OatBottomNav`: a tab bar fixed to the bottom of a phone's screen, three to five tabs
  of an icon above a label (`BottomNavItem`), with the current page's tab marked
  (`aria-current="page"`, or a rule of your own with `withActive`), badges, and one
  primary item as a round Oat button. Icons come from your SVG sprite, inline SVG or
  `OatIcon`. Its rules in `wicket-oat.css` use Oat's variables only and keep it clear
  of the iPhone's home indicator. It shows up to 768px unless `setAlwaysVisible(true)`.
  The `<nav>` is named by `OatBottomNav.label`, translated.
- `OatMobileLayout`: a base page for phone-first apps, with the content in `<main>` and
  an `OatBottomNav` below it, from `bottomNavItemsModel()`.
- `OatAppLayout` sets the page's `lang` on `<html>` from its locale, and starts with a
  "Skip to content" link (`OatAppLayout.skipToContent`, translated), shown only while it has
  keyboard focus, that moves focus to `<main id="oat-main">`.
- `OatHtml`: the `<html>` tag of `OatAppLayout` and `OatMobileLayout`, now public for a base
  page of your own (`add(new OatHtml("html"))`). It sets the theme, the density and the
  page's language.
- `OatAppLayout.newMenuLink(id, item)` makes a sidebar entry's link, e.g. an `AjaxLink`
  instead of the default `BookmarkablePageLink`, as `OatBottomNav.newLink` does for tabs.
- `OatDialog` keeps its open state on the server (`isOpen()`). Cancel, the Escape key and
  a click on the backdrop tell the server when they close it, and the new `onClose(target)`
  runs once whenever it closes, also after a confirm or `close(target)`. `target.add(dialog)`
  now works, and keeps an open dialog open. `open(content, target)` sets content that is
  removed again on close; `OatCrud` uses it for its editor, and forgets the record once it
  closes. `OatConfirmDialog` forgets its action when the user cancels.
- `OatDataTable` is pageable like a `DataTable` (`IPageableItems`): `setCurrentPage(...)`,
  `getPageCount()`, `setItemsPerPage(...)`, and a paging navigator of your own can page it.
  `addTopToolbar(...)` and `addBottomToolbar(...)` add toolbars to it.
- `OatSelectionColumn` and `OatRowDetailsColumn` can keep rows by key, e.g. an entity's id,
  so the page no longer serializes the entities: `new OatSelectionColumn<>(selectedIds,
  Invoice::getId)`. Bulk actions then get the selected rows looked up through the data
  provider, current and in the table's sort order (`getSelectedRows(provider)`).
  `getSelection()` and `getExpanded()` return the models as given, with rows or keys.
- `OatDataTable`'s empty state is a toolbar of its own instead of Wicket's `NoRecordsToolbar`
  with its message replaced through Wicket's internal component ids. Its cell spans the
  columns the user hasn't hidden, and keeps the `norecords-tr`/`norecords-td` classes.
- Form fields: `addToInput(...)` adds a behavior to the input rather than the field, e.g.
  `addToInput(AttributeModifier.replace("autocomplete", "email"))`, and `setPlaceholder`
  also takes a `String`. `add(...)` still sends validators and Ajax form-component behaviors
  to the input, as they only work there.
- Components driven by a callback have static factories taking a lambda instead of a
  subclass, like Wicket's `AjaxLink.onClick(...)`: `OatButton.onClick`,
  `OatSubmitButton.onSubmit`, `OatWizard.onFinish`, `OatLoginForm.signIn`, and `of(...)` on
  `OatAccordion`, `OatTabs`, `OatDropdown`, `OatButtonGroup`, `OatBreadcrumb`,
  `OatPagination`, `OatAvatarGroup` and `OatLazyLoadPanel`. `Oat.Components` delegates to
  them. New constructors `OatAlert(id)`, `OatAlert(id, variant)` and
  `OatEmptyState(id, title, message)` cover what the removed factories offered.
- `OatThemeSwitcher` uses stateless links, so a page showing it, e.g. a sign-in page with
  the switcher in its top bar, stays stateless.
- Fix: after signing in, `OatLoginForm` now continues to the page the user was going to.
  It replaced the session against session fixation, which also cleared the page Wicket
  had remembered, so users always landed on the home page. It now only changes the
  session's id, keeping the session's metadata.
- Fix: an `OatDialog` (or `OatCrud`'s editor) inside a page's form no longer blocks that
  form's submit. Its own form was submitted with the page's, so a required field in the
  closed dialog failed validation. Submitting the page's form now leaves it out.
- Fix: `Oat.Behaviors.density(OatDensity.DEFAULT)` now really pins a component to Oat's own
  spacing inside a compact app (`data-density="default"`); before, it inherited the compact
  density. A component pinned to a density also gets that density's body text size.
- The examples app has a "Time off" demo for phones: Today, Capacity and Team tabs, a
  badge updated over Ajax, and an "Add absence" form of Oat markup.
- The examples app looks like a business app by default: the Business theme at compact
  density, with the current theme shown in the top navigation. On Halloween and from
  10 to 31 December it defaults to the Halloween and Christmas themes, through its own
  `OatThemeStore`. Its Getting Started page shows version 0.3.0.
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
