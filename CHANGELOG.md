# Changelog

## 0.7.0 (unreleased)

### Upgrading from 0.6.x

- In development mode, and so in `WicketTester` tests, a button without an accessible
  name fails to render. Give an icon-only button an `aria-label`, or its `OatIcon` a
  label. `OatSettings.setAccessibilityChecks(false)` turns the check off.

### Changes

- New: in development mode, a button styled by Oat (`OatButton`, `OatSubmitButton`,
  `Oat.Behaviors.button()`) without an accessible name fails to render, with a message
  saying how to name it. That is a button with no text, no `aria-label` and no labelled
  icon, which a screen reader announces only as "button". Usually it is an icon-only
  button. Deployment mode doesn't check; `OatSettings.setAccessibilityChecks(boolean)`
  turns the check on or off in either mode.
- New: printed pages show the page alone. The app shell (`OatAppLayout`'s top bar,
  sidebar and skip link, `OatMobileLayout`'s tab bar) and what floats over the page
  (toasts, `OatCookieConsent`, a context menu) are left out of print, and so is
  anything with the `oat-screen-only` class. `OatAppLayout`'s main area and
  `OatSplitLayout`'s panes print in full, one pane below the other, and charts and
  table rows aren't split across sheets.
- Fix: the text of tinted badges and alerts (success, warning, danger) had less than
  WCAG AA's 4.5:1 contrast with their tint, even in the light and dark themes (4.0 to
  4.4). It is pulled a quarter of the way towards the theme's text color now, which keeps
  its hue. Found by the new browser checks of the examples app's showcase page.
- Fix: a page in a dark theme printed its light text pale on white paper, as browsers
  leave out backgrounds when printing, and the cookie banner and skip link printed
  over the content of every sheet. The dark themes now print in Oat's light palette.

## 0.6.0 (2026-10-23)

A release of fixes, from a review of the whole library: a multi-select field that saved
one record's values into another, a sign-out that another site's page could send, a date
column and a donut chart that could fail the page or count wrong, a lazy-load panel that
polled ten times a second, and fields and menus that didn't work inside dialogs or for
screen readers. Nothing is removed; the notes below say what an app may notice.

### Upgrading from 0.5.x

- `OatLazyLoadPanel.getUpdateInterval()` is a second again, as in Wicket's
  `AjaxLazyLoadPanel` (it was 0.1 seconds). The content still loads 0.1 seconds after
  the page, now really; a panel waiting on `isContentReady()` checks every second.
- `OatTagInput`'s label, hint, error and `setPlaceholder(...)` belong to the text box
  the user types in, which is a component now: `"tags:taginput:text"` in a test. The
  tags are still submitted in the hidden `getInput()` (`"tags:input"`).
- `OatUserMenu`'s "Sign out" answers 403 Forbidden when the browser says another site's
  page sent it (`Sec-Fetch-Site`).
- `WicketOats.install` leaves a Content Security Policy the application turned off off.
- An `OatActionsColumn` action is checked against `setVisibleWhen` again when it is
  clicked; that needs a row model that loads the row again, not `Model.of(row)`. Its
  cells and buttons have markup ids.
- `OatMultiSelectField` reads the model in each request; chips added or removed but not
  submitted stay until `form.clearInput()`, like typed input.
- New for fields of your own: `BaseOatField.getLabelTarget()`, what the `<label>` is for.

### Changes

- Fix: `OatMultiSelectField` kept the chips it read from the model the first time it
  rendered. A form or an `OatDialog` reused for another record showed the first record's
  chips and saved them into the other one, and a cancelled dialog showed the cancelled
  chips again when it opened. It now shows the model's current value. Chips added or
  removed are like typed input: they reach the model when the form is submitted, and
  `form.clearInput()` drops them, as an `OatDialog` does when it opens or is cancelled.
- Fix: `WicketOats.install` turned a Content Security Policy the application had turned
  off (`getCspSettings().blocking().disabled()`) back on, as `img-src data:` alone,
  which blocked every other image. It adds `data:` only to a policy that is set.
- Fix: `OatCookieConsent` failed with `NoSuchMethodError` on a Servlet 5 container such
  as Tomcat 10.0 or Jetty 11, which Wicket 10 runs on, so the answer was never stored
  and the banner kept coming back. It sets `SameSite=Lax` through Wicket's `CookieUtils`,
  on Servlet 6 only, as Wicket does for its own cookies.
- Fix: a context menu (`Oat.Behaviors.contextMenu()`) in an open `OatDialog` or
  `OatDrawer` couldn't be used: its entries took neither focus nor clicks. In an
  `OatPopover`, choosing an entry closed the popover. The menu now opens inside them.
- Fix: an `OatLoadMoreList` with `setLoadOnScroll(true)` loaded only one more batch on
  scroll when an Oat form field or an `OatAvatar` picture came before it on the page.
  Their script, `wicket-oat.js`, then loaded before Wicket's Ajax script and missed the
  "Load more" button re-rendered after each batch. It now waits for the page to load.
- Fix: `OatTagInput` gave its label, hint or error and required state to the hidden input
  that holds the tags. The box the user types in had no accessible name besides its
  placeholder, screen readers never heard that it was required or invalid, and a click on
  the label focused nothing. They are on the text box now, and so is `setPlaceholder(...)`.
  For a field of your own whose input is hidden behind a widget, `BaseOatField` has
  `getLabelTarget()`.
- Fix: text typed in an `OatTagInput` without pressing Enter or a comma was dropped on
  submit. It is submitted as one more tag.
- Fix: an `OatActionsColumn` action ran when clicked even if the row no longer met its
  `setVisibleWhen`, e.g. "Mark as paid" on an invoice someone else had paid since the
  page was rendered. It is checked again with the row the row model loads then: an
  action the row no longer shows doesn't run, and the row's menu is rendered again with
  its current actions. This needs a row model that loads the row again, such as a
  `LoadableDetachableModel`; `Model.of(row)` keeps the row as it was rendered.
- Fix: the error an `OatFileUpload` or `OatFileDropzone` showed after a submit went away
  on the next Ajax request, whatever it was, e.g. a click on a link elsewhere on the
  page. Wicket sets a file input's model to null as it detaches it, after the field, and
  the field took that for an update in the next request.
- Fix: `OatDateColumn` failed the page with a `DateTimeException` for several values:
  a `LocalDateTime` or `OffsetDateTime` with a `LONG` or `FULL` time style
  (`setStyle(...)`), a `LocalTime` with one, an `OffsetTime` in any style, and a
  `YearMonth` or another value with neither a date nor a time of day. It now looks at
  what a value has. A `LONG` or `FULL` time shows the zone, an `OffsetDateTime`'s
  offset, or `MEDIUM` for a value without one. An `OffsetTime` is shown as a time, and a
  `YearMonth` as its `toString()` (`2026-10`) unless a pattern is set.
- Fix: a page on another site could sign the user out with a form posting to
  `OatUserMenu`'s "Sign out" in Firefox and Safari, which send the session cookie along
  unless it is `SameSite`. Only Chromium treats a cookie without `SameSite` as `Lax`,
  which the documentation had relied on. "Sign out" is now refused with 403 Forbidden
  when the browser says another site's page sent it (`Sec-Fetch-Site`, which current
  browsers send), using Wicket's `FetchMetadataResourceIsolationPolicy`. For all of an
  application's requests, add Wicket's `ResourceIsolationRequestCycleListener`.
- Fix: an `OatDonutChart` with more than six parts, some of them with the same name and
  value, kept every such part as a segment when one of them was among the five largest,
  and counted the others again in "Other". It showed more than six segments, and the
  total and every share were too high. Each part is now either kept or in "Other".
- Fix: an `OatLazyLoadPanel` waiting on `isContentReady()` made its page poll the server
  every 0.1 seconds, ten requests a second, each holding the page's lock while the user
  clicked. It also made Wicket's own `AjaxLazyLoadPanel`s on the page poll that often.
  All of a page's lazy-load panels share one timer, at the shortest `getUpdateInterval()`
  of those still waiting, and this one's was 0.1 seconds. It is a second now, as in
  Wicket. The 0.1 seconds were meant for the first load, which still came after a
  second: the panel now starts the page's timer at 0.1 seconds instead.
- Fix: `OatPagination` named its `<nav>` "Pagination" in English in every language, and
  added it to an `aria-label` of the markup's own ("Seiten Pagination"). It is the
  translated `OatPagination.label` now, and an `aria-label` in the markup is kept as it is.
- `OatSelectionColumn`'s "select all" box no longer asks the data provider for the
  table's current page again each time it renders, which with a database meant one more
  query on every render and every click on a row's box. It takes the rows the table
  shows, which are also those a click on it selects.

## 0.5.0 (2026-10-22)

A release for the frame around an app's pages and the forms in them: a user menu for the
top bar, drawers that slide in from an edge, filter chips above a list, and breadcrumbs
that fold their middle levels into a menu. Form fields now show the errors of any Ajax
submit by themselves, and a form's own messages can show next to it, once. `OatBreadcrumb`
brings its own markup now; the notes below say what else can change for an app.

### Upgrading from 0.4.x

- `OatFeedbackPanel` extends Wicket's `FencedFeedbackPanel` (which extends
  `FeedbackPanel`). `new OatFeedbackPanel(id, null)` no longer compiles, as `null` could
  be a filter or a fence; write `(IFeedbackMessageFilter) null` for all messages.
- `OatBreadcrumb` brings its own markup, like other Oat components: write
  `<nav wicket:id="breadcrumb"></nav>`. Markup inside the tag is no longer allowed;
  Wicket reports "Close tag not found" for it. Its separators look like `OatPageHeader`'s.
  The levels are numbered by their index among all of them, also after a "…" menu:
  `"breadcrumb:list:items:0:link"`, and `"breadcrumb:list:tail:4:link"` for the fifth
  level of a long trail. A trail without levels is no longer shown.
- `OatPageHeader`'s breadcrumb is an `OatBreadcrumb`, so a test reaches its parts at
  `"header:breadcrumb:list:items:0:link"` (was `"header:breadcrumb:crumbs:0:link"`) and
  `"header:breadcrumb:list:currentItem:current"` (was `"header:breadcrumb:current"`). It is
  named by `OatBreadcrumb.label`; Wicket Oat no longer ships `OatPageHeader.breadcrumb`,
  but still uses it if your application defines it.
- `BreadcrumbBehavior` is deprecated, as `OatBreadcrumb` no longer uses it: write the
  `aria-label` of a `<nav>` of your own in its markup.
- An open `OatDialog` is a flex column whose content scrolls between its header and its
  buttons; CSS of your own on the dialog's parts may notice that.
- A dialog closed in the browser (Cancel, Escape, the backdrop) renders its form again
  once it is out of sight, from the models: input left in it, e.g. by a failed confirm,
  is gone the next time it opens.

### Changes

- Oat form fields show the errors of an Ajax request by themselves. A submit button
  without an `onError` that adds the form, such as `OatSubmitButton.onSubmit(id, label,
  onSubmit)`, used to show nothing for an invalid form: the fields weren't re-rendered,
  and `OatFeedbackPanel` and the feedback toasts leave out the errors fields show. Now a
  field validated in an Ajax request whose error appeared, changed or went away updates
  its message and error state in place, without re-rendering its input, so the input
  keeps its focus and what the user typed, a chosen file or a password. Other Ajax
  requests leave the errors shown alone. `target.add(field)` and `target.add(form)`
  work as before.
- `OatFeedbackPanel` can show one form's messages next to it, such as the form's own
  errors from `form.error(...)`: `new OatFeedbackPanel("formFeedback", form)`. As with
  Wicket's `FencedFeedbackPanel`, which it now extends, the form is its fence: a panel
  without a fence and `Oat.Behaviors.feedbackToasts()` leave out the messages from inside
  it, so each message shows once. A panel without messages takes no space, so it can sit
  at the top of a stacked form without leaving a gap.
- A field's error message is shown in the danger color, as Oat shows errors, instead of
  the grey of a hint.
- `OatUserMenu`: the signed-in user's menu for the top bar of an `OatAppLayout`. Their
  picture, or initials, and name open a menu with their name, a second line such as their
  email, links of yours with icons, and "Sign out". While nobody is signed in it is a
  "Sign in" link, hidden on the sign-in page. "Sign out" invalidates the session and shows
  the home page; `signOut()` and `onSignedOut()` change that, e.g. for Spring Security. It
  is a POST, so another site can't sign users out, and it works on a page still open
  after the session expired. Someone is signed in while the name isn't `null`, or as
  `isSignedIn()` says. It keeps a page stateless, reads its models once per request,
  shows only the picture on phones, and its button is named "Account: " and the name for
  screen readers. The examples' sign-in page now signs in a demo user, whose menu appears
  in the top bar.
- `OatAvatar` reads its models once on every render, so it follows them, e.g. in a reused
  list row or after a sign-in. With a picture and initials, the initials show under the
  picture while it loads and if it can't be loaded, instead of a broken image, and the
  picture's `alt` is empty, as the initials name it. A blank picture URL counts as none.
  `OatAvatar.initials(id, model)` makes an avatar from initials in a model
  (`new OatAvatar(id, model)` takes a picture URL), and `OatAvatar.initialsOf(name)` turns
  "Ada Lovelace" into "AL", also for names apart by an ideographic space. Initials are no
  longer underlined.
- `OatIcon.ofNullable(id, name)` makes an icon, or for a `null` name a hidden placeholder.
- A long breadcrumb can leave levels out. `OatBreadcrumb.setMaxItems(n)` and
  `OatPageHeader.setBreadcrumbMaxItems(n)` show at most n levels, counting the current
  page: the first, a "…" button whose menu has the levels in between, and the last ones.
  `OatBreadcrumb.setCurrent(model)` ends a trail with the current page as text, as
  `OatPageHeader`, which now uses `OatBreadcrumb`, does with its title. The trail reads its
  levels once per request, and its `<nav>` is named by the translated
  `OatBreadcrumb.label` resource instead of an English "Breadcrumb"; `setLabel(...)` or an
  `aria-label` in the markup names it otherwise, e.g. a second trail on a page.
- `OatDropdown`'s button says it opens a menu (`aria-haspopup="menu"`, and
  `aria-expanded="false"` before it first opens), and the menu is a `role="menu"`.
- `OatDrawer`: a side panel that slides in from an edge of the screen over the page, for
  filters, a record's details or a form to edit a row. It is an `OatDialog`, opened,
  confirmed and closed the same way, as tall as the screen at its end, or at its start
  or bottom (`setSide`); start and end follow the text direction. An "×" in its corner
  closes it, its content scrolls between header and footer, and with
  `setFooterVisible(false)` it leaves out Cancel and Confirm. The examples' dialog page
  has a filters drawer and one with a person's details.
- `OatDialog.setCloseButton(true)` adds the "×" to a dialog (named by the new
  `OatDialog.close` resource, in all 7 languages), and `setFooterVisible(false)` leaves out
  its footer with Cancel and Confirm; Enter in a field then submits nothing. The "×" comes
  first, as it is drawn, and opening focuses the first field after it.
- An `OatDialog`'s content scrolls between its header and its buttons, which stay in view,
  as does the "×".
- A dialog closed in the browser (Cancel, "×", Escape, the backdrop) shows its models
  again the next time, without what was changed but not confirmed, or the errors of a
  failed confirm. The server hears of the close once the dialog is out of sight, so this,
  and content set with `open(content, target)` going away, doesn't show while it closes.
- An open dialog rendered again, e.g. with `target.add(dialog)` or opened again for the
  next record, shows at once instead of sliding or fading in again.
- A `null` trigger label renders no trigger button with the `String` constructors too, and
  `getDialogElement()` lets a subclass style the `<dialog>`.
- `OatFilter`: a row of chips above a list or table choosing one value, such as a category
  or a status, the chosen chip filled. Choosing one sets the model and runs `onChange`
  over Ajax (`OatFilter.onChange(id, model, choices, target -> ...)`). `setNullValid(true)`
  adds a first "All" chip for no value, translated in all 7 languages; without it, a value
  must be chosen. The chips are the radio buttons of a Wicket `RadioChoice` in a named
  `<fieldset>`, so the arrow keys move between them, and an `IChoiceRenderer` gives their
  text and ids: strings, numbers and enums are their own ids, so a chip stands for its
  value also when the choices change. The choices are read once per request, and without
  a model the filter binds by its id to a `CompoundPropertyModel`. The examples' products
  page filters by category.

## 0.4.0 (2026-10-19)

A release with components for where things stand: steps, lists like a phone app's, counts
and status dots on a corner, star ratings and progress rings. It moves to Oat UI 1.0, with
a neutral look for Light and Dark and toasts that stack, and to Wicket 10.11, and fixes
dialogs and the Enter key in forms inside a page's form. Existing code compiles unchanged;
the notes below say what can still change for an app.

### Upgrading from 0.3.x

- Wicket 10.11.0 or newer is required.
- `OatWizard` draws its steps with `OatSteps`, so in a test a finished step's link is at
  `"steps:list:items:0:link"` instead of `"steps:0:link"`.
- Oat UI 1.0 changes the looks: Light and Dark get a neutral palette, and every theme
  smaller corners, new shadows and slower animations. Toasts stack while the pointer or
  focus isn't on them; your own CSS for `.toast` may notice that their container is then a
  grid and the older toasts' content hidden. The Oat UI entry below has the details.
- `Oat.Behaviors.feedbackToasts()` shows a request's messages by level, the most severe
  last, instead of in the order they were reported.
- An `OatSubmitButton` in a form nested in another renders as `type="button"`, so Enter in
  the outer form can't click it. For Enter in its own form's fields, make it that form's
  default button (`form.setDefaultButton(button)`).

### Changes

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
- Oat UI 1.0.0 (was 0.8.3). Its script is unchanged, so nothing changes for your code, but
  the CSS gives everything a new look:
  - Light and Dark, and pages without a theme, get Oat's new neutral palette: buttons are
    near-black instead of brown in Light and light grey instead of white in Dark, text is
    a little softer, and borders and inputs are subtler. A component pinned to Light or
    Dark (`Oat.Behaviors.theme(...)`) follows.
  - Every theme gets smaller corners (`--radius-medium` .25rem, `--radius-large` .5rem;
    Business keeps its own), new shadows (Business light keeps its own), slower and
    smoother animations (`--transition` 300ms), tighter badges, a 3px focus ring, and no
    shadow under the sidebar and the top bar. In a dark theme a button darkens on hover
    instead of lightening.
  - Toasts stack: several show as one, the newest in front and the others peeking out
    behind it, until the pointer or focus is on them. While stacked, Oat's toast container
    is `display: grid` and the older toasts' content is hidden, which your own CSS for
    `.toast` may notice.
  - Fix: a tooltip on an item in an `OatDropdown` or `OatPopover` (also an
    `OatSplitButton`'s menu) no longer hides the item. Oat 0.8.3 also styled the tooltip
    as a menu, a white box over the item it was about.
  - `OatDropdown`, `OatSplitButton` and `OatPopover` open without the slight zoom Oat 1.0
    gives them. Oat places a menu while it is still zoomed out, so a menu aligned to the
    end of its button stuck out past it, and one that only just fit opened past the
    window's edge.
- `Oat.Behaviors.feedbackToasts()` shows a request's messages in order of their level, the
  most severe last, so it is the one of them in front of Oat's stack.
- The suggestion list of `OatAutoCompleteField` and the menu of `ContextMenuBehavior` look
  like Oat 1.0's dropdown menu: the card's background and a stronger shadow.
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
