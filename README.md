# Wicket Oat

[![Build](https://github.com/danielbartl/wicket-oat/actions/workflows/build.yml/badge.svg)](https://github.com/danielbartl/wicket-oat/actions/workflows/build.yml)

Wicket Oat is a modern, lightweight, and themeable UI component library for [Apache Wicket](https://wicket.apache.org/). It provides a set of high-quality components and behaviors built on top of a sleek, modern design system.

**[Live demo & docs](https://danielbartl.github.io/wicket-oat/)** &middot; **[Step-by-step tutorial](https://danielbartl.github.io/wicket-oat/tutorial.html)**

## Features

- **Fluent API:** Easily create components and behaviors using the `Oat` factory class.
- **Modern Design:** Beautifully designed components like Buttons, Cards, Modals, and more.
- **Themeable:** Built-in support for multiple themes (Light, Dark, Midnight, Nord, etc.) that can be switched dynamically.
- **Wicket 10+:** Fully compatible with Apache Wicket 10, on Java 17 or newer.
- **Spring Boot Integration:** Seamless integration with Spring Boot applications.
- **Comprehensive Component Set:** Includes everything from basic buttons to complex data tables.
- **Accessible Forms by Default:** Every form field automatically wires `aria-describedby` (linking the field to its feedback/hint message) and toggles `aria-invalid` when validation fails — no extra markup or wiring required.
- **Wicket-Style Forms:** Form fields work with `CompoundPropertyModel`, take their labels from `.properties` files, and re-render via Ajax like any Wicket component (see [Forms](#forms)).
- **Translated:** The library's own texts (dialog buttons, menu toggle, upload hints, validation messages, ...) come in English, German, Spanish, French, Italian, Japanese, Dutch and Portuguese, following the component's locale like Wicket's own messages (see [Translations](#translations)).

## Quick Start

### 1. Add Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>wicket-oat-core</artifactId>
    <version>0.1.1</version>
</dependency>
```

### 2. Install Wicket Oat

In your Wicket `WebApplication` class, call `WicketOats.install(this)` in the `init()` method:

```java
public class MyWicketApplication extends WebApplication {
    @Override
    protected void init() {
        super.init();
        
        // Install Wicket Oat components and styles
        WicketOats.install(this);
    }
}
```

### 3. Use Components

Use the `Oat` factory class to create components in your pages:

```java
// Create a button with an Ajax click handler via Components factory
add(Oat.Components.button("myButton", "Click Me", target -> {
    Oat.toast(target, "Hello from Wicket Oat!", OatVariant.SUCCESS);
}));

// Create an alert via Components factory
add(Oat.Components.alert("myAlert", "Your changes were saved.", OatVariant.SUCCESS));

// Create a badge via Components factory
add(Oat.Components.badge("myBadge", "New", OatVariant.SUCCESS));
```

Every Oat component is also a plain Wicket component, so for components that
don't need a callback or a list of items, a regular constructor works just as
well:

```java
// The alert and badge above, built directly with their constructors
add(new OatAlert("myAlert", "Your changes were saved.", OatVariant.SUCCESS));
add(new OatBadge("myBadge", "New", OatVariant.SUCCESS));
```

`OatButton` above is one of the components whose constructor requires
subclassing (see [Usage Strategies](#usage-strategies) below), so the factory
is the more natural choice for it.

## Available Components

- **General:** `OatButton`, `OatSplitButton`, `OatBadge`, `OatAvatar`, `OatAvatarGroup`, `OatAlert`, `OatCard`, `OatStatCard`, `OatEmptyState`, `OatFeedbackPanel`, `OatIcon`, `OatCookieConsent`
- **Navigation/Layout:** `OatAppLayout`, `OatPageHeader`, `OatMenuBar`, `OatWizard`, `OatMasterDetail`, `OatSplitLayout`, `OatAccordion`, `OatButtonGroup`, `OatBreadcrumb`, `OatPagingNavigator`, `OatAjaxPagingNavigator`, `OatPagination`
- **Overlays:** `OatDialog`, `OatConfirmDialog`, `OatDropdown`, `OatPopover`, `OatTabbedPanel`, `OatTabs`
- **Charts:** `OatBarChart`, `OatColumnChart`, `OatLineChart`, `OatDonutChart`, `OatSparkline`
- **Data Display:** `OatDataTable`, `OatDescriptionList`, `OatTimeline`, `OatMessageList`, `OatLoadMoreList`, `OatLazyLoadPanel`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`
- **Records & Trees:** `OatCrud`, `OatTree`, `OatTableTree`; data table columns `OatRowDetailsColumn`, `OatEditableColumn`
- **Forms:** `OatMultiSelectField`, `OatTextField`, `OatCheckBox`, `OatDropdownChoice`, `OatRadioChoice`, `OatCheckBoxMultipleChoice`, `OatListMultipleChoice`, `OatTextArea`, `OatSwitch`, `OatTagInput`, `OatFileUpload`, `OatFileDropzone`, `OatMoneyField`, `OatPercentField`, `OatDateRangeField`, `OatAutoCompleteField`, `OatCustomField`, `OatLoginForm`, `OatMessageInput`, `OatFieldset`, and more specialized HTML5 fields.

### Variants

Every component and behavior with a color variant takes the same
`OatVariant` enum: `DEFAULT`, `SECONDARY`, `SUCCESS`, `WARNING`, `DANGER`.
Oat's CSS styles them per component: buttons support `SECONDARY` and `DANGER`,
badges all of them, and alerts and toasts `SUCCESS`, `WARNING` and `DANGER`;
any other variant renders like `DEFAULT`.

## Architecture: Components vs. Behaviors

Wicket Oat provides both **Components** and **Behaviors** for almost every UI element. This dual approach gives you maximum flexibility depending on your needs.

### When to use Components (`Oat.Components`)
Use a component when you want a self-contained UI widget and don't want to worry about the underlying HTML structure.
- **Pros:** Easiest to use; encapsulates markup logic; handles internal structure (like headers/footers in a Card).
- **Example:** `add(Oat.Components.alert("id", "Saved!", OatVariant.SUCCESS))`
- **Markup Requirement:** Requires a simple tag like `<div wicket:id="id"></div>`.

### When to use Behaviors (`Oat.Behaviors`)
Use a behavior when you want to "Oat-ify" an existing Wicket component. This is the power of **Composition over Inheritance**.
- **Pros:** Highly flexible; can be applied to *any* component (Links, Labels, Containers); keeps your component hierarchy clean.
- **Example:** `myWicketLink.add(Oat.Behaviors.button())` — This turns a standard Wicket `Link` into a styled Oat button without changing the Java class of the link.
- **Markup Requirement:** You provide the markup (e.g., an `<a>` or `<button>` tag) and the behavior ensures the correct CSS classes and attributes are applied.

### Summary Comparison

| Feature | Component | Behavior |
| :--- | :--- | :--- |
| **Philosophy** | "Give me an Alert" | "Make this thing look like an Alert" |
| **Java Usage** | `new OatAlert(...)` | `anyComponent.add(new AlertBehavior())` |
| **Markup** | Handled by Oat | Handled by You |
| **Best For** | Quick UI building | Customizing existing Wicket logic |

## Usage Strategies

Wicket Oat components are plain Wicket components — you can always create
them with `new`. Whether you reach for `Oat.Components`/`Oat.Behaviors` or a
constructor mostly comes down to what the component needs.

### When to use the factory (`Oat.Components` / `Oat.Behaviors`)
Components whose behavior is driven by a callback or a list of items —
`OatButton`, `OatTabs`, `OatAccordion`, `OatButtonGroup`, `OatBreadcrumb`,
`OatPagination`, `OatDropdown`, `OatAvatarGroup` — are backed by an `abstract`
class. Their constructors alone aren't enough; you'd need to write an
anonymous subclass to implement a method like `onClick` or `populateItem`.
The factory does that for you from a lambda, so it's the natural default
here, not just a shortcut:
```java
// No subclass needed - the factory implements onClick() for you
add(Oat.Components.button("id", "Click Me", target -> ...));   // <a wicket:id="id"></a>

// The same for a form's submit button: onSubmit when valid, onError when not
form.add(Oat.Components.submitButton("save", "Save", target -> ..., target -> target.add(form)));
```

### When to use a constructor
Every other component — `OatAlert`, `OatBadge`, `OatCard`, `OatDialog`,
`OatAvatar`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`,
`OatDataTable`, `OatEmptyState`, the form fields, and more — has no abstract
methods, so a plain constructor works just as well, and is the more familiar
style if you're used to plain Wicket:
```java
add(new OatAlert("id", "Saved!", OatVariant.SUCCESS));
```
For `OatAlert`/`OatBadge` the constructor is actually the *more* capable
option: it has an overload that binds the variant to a reactive
`IModel<OatVariant>` which the factory doesn't expose. `OatDialog` and
`OatDropdown` accept either a plain `String` or an `IModel<String>` on both
the constructor and the factory, so pick whichever shape matches what you
already have on hand.

`OatDialog` can be opened and closed from any Ajax handler with
`dialog.open(target)` / `dialog.close(target)`, and validates the form fields in
its body when Confirm is clicked, staying open to show any errors:

```java
OatDialog editDialog = new OatDialog("editDialog", Model.of("Edit person")) {
    @Override
    protected void onConfirm(AjaxRequestTarget target) {
        success("Saved."); // closes afterwards, unless an error was reported here
    }
};
editDialog.setBody(new PersonFieldsPanel(OatDialog.BODY_ID, selectedPerson));

// e.g. in a table row's AjaxLink:
selectedPerson.setObject(person);
editDialog.open(target);
```

### Going further: subclassing and composition
Every component has a public constructor, so you can extend `OatButton`,
`OatAlert`, etc., just like any other Wicket component:
```java
public class MyBusinessButton extends OatButton {
   // Your complex business logic here
}
```
Or keep your own class hierarchy entirely and use behaviors to apply the Oat
look to it — the most flexible option when a component already has its own
base class:
```java
public class MyComplexActionLink extends AjaxLink<Void> {
    public MyComplexActionLink(String id) {
        super(id);
        add(Oat.Behaviors.button().setVariant(OatVariant.SECONDARY));
    }
}
```

## App Shell

### Sidebar menu

`OatAppLayout` builds its sidebar from `sidebarMenuItemsModel()`. Links can be
grouped into collapsible sections, open while one of their pages is shown, and
can carry a badge, such as a count:

```java
@Override
protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
    return Model.ofList(List.of(
            MenuItem.of("Dashboard", DashboardPage.class),
            MenuItem.group("Sales",
                    MenuItem.of("Orders", OrdersPage.class).withBadge(() -> orders.countOpen()),
                    MenuItem.of("Invoices", InvoicesPage.class)),
            MenuItem.of("Settings", SettingsPage.class)));
}
```

A badge is read on every render and hidden while it is `null`, empty or zero.
To update it without reloading the page, re-render the sidebar from an Ajax
handler with `target.add(sidebar)`. Pages the user isn't authorized to open are
left out, and so is a group left empty. Groups can't contain other groups.

### Busy buttons

For a slow action, a button can show that it's working: with
`setBusyIndicator(true)`, an `OatButton` or `OatSubmitButton` shows a spinner
(`aria-busy="true"`) while it waits for its Ajax request, and further clicks send
nothing, so the action can't run twice. `OatDialog` has the same setter for its
confirm button, and `Oat.Behaviors.ajaxBusy()` adds it to any other Ajax link or
button:

```java
form.add(Oat.Components.submitButton("save", "Save", target -> ...).setBusyIndicator(true));
add(new AjaxLink<Void>("refresh") { ... }.add(Oat.Behaviors.ajaxBusy()));
```

This needs `WicketOats.install(this)`, which hooks it into the requests.

### Confirming an action

`OatConfirmDialog` asks before an action runs. Add one to the page and `ask` it
from any Ajax handler with the action to run; one dialog serves any number of
buttons, e.g. a Delete button on every table row:

```java
OatConfirmDialog confirm = new OatConfirmDialog("confirm"); // <div wicket:id="confirm"></div>
add(confirm);

add(Oat.Components.button("delete", "Delete", target ->
        confirm.ask(target, "Delete invoice?", "This can't be undone.", t -> {
            invoices.delete(invoice);
            t.add(table);
        })));
```

The action stays on the server until the user answers, and runs at most once.
The confirm button is a danger button by default; `setConfirmLabel(...)` and
`setConfirmVariant(...)` change it.

### Loading slow content

`OatLazyLoadPanel` renders the page first and loads slow content over Ajax right
after, showing Oat skeleton lines until it arrives:

```java
add(Oat.Components.lazyLoad("revenue", id -> new RevenuePanel(id, reports.revenueThisYear()))
        .setPlaceholder(SkeletonBehavior.Shape.LINE, 2));
```

It extends Wicket's `AjaxLazyLoadPanel`, so it can also wait for background work:
override `isContentReady()`.

## Feedback Messages

Wicket's `info()`, `success()`, `warn()` and `error()` messages can be shown as
Oat alerts or as Oat toasts:

```java
// A FeedbackPanel that renders each message as an alert of the matching variant
add(new OatFeedbackPanel("feedback"));

// ...or show them as toasts - add this to your base page to cover every page
add(Oat.Behaviors.feedbackToasts());
```

Both update themselves during Ajax requests, so an event handler only needs to
call `success("Saved.")` - no `target.add(...)` or `Oat.toast(...)`. The toasts
also pick up session messages, so a `getSession().success(...)` before
`setResponsePage(...)` appears on the next page. Both leave out the errors that
Oat form fields already show inline; pass your own `IFeedbackMessageFilter` to
change what they show.

## Tabs

`OatTabbedPanel` is Wicket's `AjaxTabbedPanel` with Oat styling: tabs are
`ITab`s, only the selected tab's panel is created, and switching swaps it over
Ajax. `Oat.tab(...)` builds a tab from a panel factory:

```java
add(new OatTabbedPanel<>("tabs", List.of(
        Oat.tab("Profile", ProfilePanel::new),
        Oat.tab("Settings", SettingsPanel::new))));
```

`OatTabs` instead renders every panel up front and switches between them in
the browser, for small static content.

## Page Building Blocks

The parts most business pages are made of: a header, key figures, the record's
details, and actions.

```html
<header wicket:id="pageHeader">          <!-- the content becomes the actions -->
    <div wicket:id="quickNote"></div>
    <div wicket:id="newInvoice"></div>
</header>
<div class="row">
    <article wicket:id="revenue" class="col-3"></article>
</div>
<dl wicket:id="details"></dl>
```

```java
OatPageHeader header = new OatPageHeader("pageHeader", Model.of(customer.getName()))
        .setSubtitle("Customer since 2019")
        .setBreadcrumb(List.of(MenuItem.of("Home", HomePage.class), MenuItem.of("Customers", CustomersPage.class)));
add(header);

// A button opening a panel with any content, e.g. a small form
header.add(new OatPopover("quickNote", "Quick note", id -> new QuickNotePanel(id, customer)));

// The usual action, with related ones in a menu
OatSplitButton newInvoice = new OatSplitButton("newInvoice", "New invoice", target -> ...);
newInvoice.addAction("New quote", target -> ...);
newInvoice.addAction("Archive customer", target -> confirm.ask(...)).setVariant(OatVariant.DANGER);
header.add(newInvoice);

// A key figure and how it changed: +12.5% on a green badge
add(new OatStatCard("revenue", Model.of("Revenue"), () -> reports.revenue())
        .setChange(() -> reports.revenueChange())          // setChange(model, false) where lower is better
        .setHint(Model.of("vs. last year")));

// The record's fields as labels and values; labels from the .properties file
add(new OatDescriptionList("details", new CompoundPropertyModel<>(customer))
        .addProperty("vatId")
        .addProperty("email")
        .addItem(Model.of("Status"), id -> new OatBadge(id, "Active", OatVariant.SUCCESS)));
```

Numbers and dates in stat cards and description lists follow the user's
locale (`1,234`, `Mar 1, 2019`), and an empty value shows as "—".

Fields that render a single input can also suggest values as the user types,
from a native `<datalist>`: `field.setSuggestions(List.of("Call", "Meeting", "Email"))`.

## Multi-Step Forms, Lookups and Histories

`OatWizard` fills a form in over several steps, with numbered steps showing
where the user is. Next validates the current step; Back and the numbers of
finished steps go back without validating. Each step is a component, created
when shown, so keep the data in an object all steps share:

```java
OatWizard wizard = new OatWizard("wizard") {
    @Override
    protected void onFinish(AjaxRequestTarget target) {
        orders.place(order);
        setResponsePage(OrderPlacedPage.class);
    }
};
wizard.addStep("Customer", id -> new CustomerStep(id, order));
wizard.addStep("Items", id -> new ItemsStep(id, order));
wizard.addStep("Review", id -> new ReviewStep(id, order));
```

`onNext(step, target)` can check a step further and keep the user on it by
reporting an error.

`OatAutoCompleteField` looks its value up on the server as the user types,
for choices too many for a dropdown. On submit the text must be one of the
suggestions, and the field's value is that object:

```java
form.add(new OatAutoCompleteField<Customer>("customer")
        .setChoices(text -> customers.search(text))
        .setDisplay(Customer::name));
```

`setFreeText(text -> ...)` accepts other text too. For a fixed list of
suggestions, `setSuggestions(...)` on any text field is simpler.

`OatDateRangeField` edits a `DateRange(from, to)` as two date inputs; either
end may be left open, `setRequired(true)` asks for both, and an end before the
start is rejected.

`OatTimeline` lists events in order, such as an order's history or an audit
trail, each with a title, its time (`java.time`, in the user's locale) and an
optional description and color:

```java
add(new OatTimeline<>("history", () -> orders.history(order), Event::title, Event::at)
        .setDescription(Event::details)
        .setVariant(event -> event.isPayment() ? OatVariant.SUCCESS : OatVariant.DEFAULT));
```

## Sign-in, Master-Detail and Messages

`OatLoginForm` is a sign-in form; implement `signIn` with your session or
security framework:

```java
add(new OatLoginForm("login") {
    @Override
    protected boolean signIn(String username, String password, boolean rememberMe) {
        return AuthenticatedWebSession.get().signIn(username, password);   // wicket-auth-roles
    }
}.setRememberMe(true).setForgotPasswordLink(ResetPasswordPage.class));
```

A failed attempt shows "Wrong username or password." without saying which was
wrong, and clears the password. After a successful one the session gets a new
id (against session fixation) and the user continues to the page they were
going to. The fields carry `autocomplete` hints for password managers.

`OatMasterDetail` shows a list next to the selected item's details, and one at a
time on phones, with a Back button. The master is any component that calls
`select(target, item)`:

```java
OatMasterDetail<Customer> customers = new OatMasterDetail<>("customers", new Model<>());
customers.setMaster(id -> new CustomerList(id, customers));   // its links call customers.select(target, c)
customers.setDetail((id, customer) -> new CustomerPanel(id, customer));
```

`OatMessageList` and `OatMessageInput` show and add comments or notes, with the
author's avatar (initials by default), the time, and the text with its line
breaks:

```java
OatMessageList<Note> notes = new OatMessageList<>("notes", () -> notesOf(customer), Note::author, Note::text, Note::at);
add(notes);
add(new OatMessageInput("addNote", (target, text) -> { save(customer, text); target.add(notes); }));
```

`OatCustomField` gives an input of your own, made of several inputs (usually a
`FormComponentPanel`), an Oat label, hint and inline errors, and points the
`aria-describedby`/`aria-invalid` of every input inside at them:

```java
form.add(new OatCustomField<String>("phone", PhoneInput::new));   // (id, model) -> your input
```

## Charts

Simple charts are rendered as SVG on the server, with no JavaScript library. Each
one is a `<figure>` with a title and a legend (from two series on). Hovering a mark
shows its value, and a "Data" disclosure holds the numbers as a table, which is
what screen readers use. The colors come from a colorblind-safe palette with
separate steps for light and dark themes. Override `--oat-chart-1` to `--oat-chart-8`
for your brand.

```java
add(Oat.Components.columnChart("monthly", () -> reports.monthly(), Month::label, Month::revenue)
        .setTitle("Revenue per month")
        .setValueFormat((value, locale) -> euros(value, locale)));
add(Oat.Components.lineChart("trend", () -> reports.monthly(), Month::label)
        .addSeries("2026", Month::revenue)
        .addSeries("2025", Month::lastYear));
add(Oat.Components.barChart("customers", () -> reports.topCustomers(), Row::name, Row::revenue));
add(Oat.Components.donutChart("regions", () -> reports.byRegion(), Region::name, Region::revenue));
add(new OatStatCard("revenue", "Revenue", revenue).setTrend(() -> reports.last12Months()));
```

- **Bar chart:** compares categories, especially with long names. Ranked lists work well.
- **Column chart:** for a few ordered categories, such as months.
- **Line chart:** change over time, with up to three series.
- **Donut chart:** how a whole divides into at most six parts. The smallest beyond
  that are combined into "Other".
- **Sparkline:** a small trend line without axes.

## Menus, Split Layouts and Long Lists

```java
// A row of commands
OatMenuBar commands = Oat.Components.menuBar("commands")
        .addAction("New", target -> create(target))
        .addLink("Reports", ReportsPage.class);
commands.addMenu("Export").addAction("CSV", target -> exportCsv(target));
add(commands);

// Right-click (or Shift+F10) on a row
row.add(Oat.Behaviors.contextMenu()
        .addAction("Open", target -> open(target, mail))
        .addAction(Model.of("Delete"), OatVariant.DANGER, target -> delete(target, mail)));

// Two panes; the first is resizable by dragging its corner, stacked on phones
add(Oat.Components.splitLayout("inbox", id -> new MailList(id), id -> new MailView(id)).setSplit(40));

// 20 items at a time, the next batch as the user scrolls
add(Oat.Components.loadMoreList("mail", provider, 20, (id, mail) -> new MailRow(id, mail)).setLoadOnScroll(true));
```

`OatMultiSelectField` chooses several values, looked up on the server as the
user types, and shows them as chips with a remove button. Like `OatAutoCompleteField`,
it can't be used inside a modal `OatDialog`, because Wicket adds the suggestion
list to the page body, which the dialog covers.

```java
form.add(new OatMultiSelectField<User>("members", "Members", membersModel)
        .setChoices(text -> users.search(text))
        .setDisplay(User::name));
```

`OatIcon` draws one of 64 Lucide icons (`OatIcon.names()`) inline, at the text's
size and color. It's decorative unless you give it a label:

```java
add(new OatIcon("edit", "pencil"));                         // <svg wicket:id="edit"></svg>
add(new OatIcon("remove", "trash-2").setLabel("Delete"));  // role="img" aria-label="Delete"
```

`OatCookieConsent` asks for consent to optional cookies, remembers the answer for a
year, and hides itself once the user has answered. Check the answer with
`OatCookieConsent.isAccepted()` before loading, for example, analytics:

```java
add(Oat.Components.cookieConsent("cookies").setPolicyLink(PrivacyPage.class));
```

## Data Tables

`OatDataTable` is a Wicket `DataTable` with Oat styling: sortable headers show
the sort order (and set `aria-sort`), and it pages with `OatPagingNavigator`.
Columns for common cells are in `dev.jbaby.wicket.oat.components.table`:

```java
List<IColumn<Invoice, String>> columns = new ArrayList<>();
columns.add(new OatSelectionColumn<>(selected));               // checkboxes; selected is an IModel<Set<Invoice>>
columns.add(OatLinkColumn.toPage(Model.of("Number"), "number", Invoice::number,
        InvoicePage.class, invoice -> new PageParameters().add("id", invoice.id())));
columns.add(new OatBadgeColumn<>(Model.of("Status"), "status", Invoice::status,
        invoice -> invoice.status() == Status.PAID ? OatVariant.SUCCESS : OatVariant.SECONDARY));
columns.add(new OatNumberColumn<Invoice, String>(Model.of("Total"), "total", Invoice::total)
        .setCurrency(Invoice::currency));                       // €1,234.50, right-aligned
columns.add(new OatDateColumn<>(Model.of("Due"), "due", Invoice::due));
columns.add(new OatBooleanColumn<>(Model.of("Exported"), Invoice::exported));

OatActionsColumn<Invoice, String> actions = new OatActionsColumn<>();   // a "⋯" menu per row
actions.addAction("Mark as paid", (target, invoice) -> ...)
        .setVisibleWhen(invoice -> invoice.status() != Status.PAID);
actions.addAction("Delete", (target, invoice) -> confirm.ask(...)).setVariant(OatVariant.DANGER);
columns.add(actions);

OatDataTable<Invoice, String> table = new OatDataTable<>("invoices", columns, provider, 10);
table.addBulkAction("Export", (target, rows) -> { exports.export(rows); table.clearSelection(target); });
table.setToolbar(id -> new InvoiceSearchPanel(id, search));
table.setEmptyState(id -> Oat.Components.emptyState(id, "No invoices found"));
```

- Numbers and dates are formatted in the user's locale; `OatNumberColumn` takes
  `setFractionDigits(...)` and a fixed or per-row currency, `OatDateColumn`
  `setStyle(...)`, `setPattern(...)` and a zone for `Instant`s.
- With an `OatSelectionColumn`, a bar above the table shows how many rows are
  selected, with the bulk actions and "Clear selection". The selection lasts
  across pages and sorting, and rows are compared with `equals`, so give the row
  type a meaningful one (a record, or an entity comparing ids). Bulk actions
  receive a copy of the selected rows; call `clearSelection(target)` when done.
  Each change also sends an `OatSelectionColumn.SelectionChanged` event.
- The table has a markup id: `target.add(table)` re-renders it after an action.
  To filter, have the data provider read a search model, and in the search
  field's Ajax handler call `table.getTable().setCurrentPage(0)` and
  `target.add(table.getTable())`.

### Row details, editing in place, columns and CSV

```java
columns.add(0, new OatRowDetailsColumn<>((id, order) -> new OrderLinesPanel(id, order)));   // expands a row
columns.add(new OatEditableColumn<Line, String, Integer>(Model.of("Quantity"), "quantity",
                Line::quantity, Line::setQuantity)
        .setType(Integer.class).setRequired(true).addValidator(RangeValidator.minimum(1))
        .onSave((target, line) -> lines.save(line)));

table.setColumnChooser(true);          // a "Columns" popover to hide and show columns
table.setCsvExport("invoices.csv");    // "Export CSV": every row, in the current sort order
```

- **Row details:** the arrow opens a full-width panel under the row. It's created the
  first time the row is opened, and stays open across pages and sorting.
- **Editing in place:** a cell shows its value as a button. Clicking it, or pressing
  Enter, turns it into a field with Save and Cancel; Enter saves and Escape cancels.
  The value is converted and validated, an error shows under the field, and focus
  goes back to the cell afterwards.
- **CSV:** the file has the columns with a header text and a value (any
  `IExportableColumn`, which includes the Oat value columns). Numbers and dates stay
  machine-readable, and text that a spreadsheet would run as a formula is defused.

## CRUD

`OatCrud` is a table of records with everything to manage them: "New" above it,
Edit and Delete on every row, a dialog to edit a record in, and a confirmation
before deleting one.

```java
OatCrud<Customer, String> customers = new OatCrud<>("customers", columns, provider, 20)
        .setEditor((id, customer) -> new CustomerFields(id, customer))
        .setNewItem(Customer::new)
        .setTitle(Customer::name)
        .onSave((target, customer) -> repository.save(customer))
        .onDelete((target, customer) -> repository.delete(customer));
```

The editor is a panel of form fields. It gets a `CompoundPropertyModel` over the
record, so Oat fields named after a property (`new OatTextField<>("name")`) find
their value and label by themselves. Save writes the fields to the record only
when they are all valid; Cancel leaves it as it was. Records to edit are loaded
through the provider's `model(...)`, so a `LoadableDetachableModel` gives a fresh
entity to save. `setSearch(id -> ...)` puts a search field beside "New", and
`getDataTable()` gives the table for selection, the column chooser or CSV export.

## Trees

`OatTree` (a nested tree) and `OatTableTree` (a tree grid) are Wicket's
`NestedTree` and `TableTree`, styled with Oat's tokens instead of Wicket's image
themes. Children load only when their parent is expanded, through an
`ITreeProvider`.

```java
add(new OatTree<>("folders", new FolderProvider())
        .setLabel(Folder::name)
        .setIcon(folder -> "folder")
        .onSelect((target, folder) -> show(target, folder)));

List<IColumn<Account, String>> columns = List.of(
        new TreeColumn<>(Model.of("Account")),
        new OatNumberColumn<>(Model.of("Balance"), Account::balance).setCurrency(EUR));
add(new OatTableTree<>("accounts", columns, new AccountProvider(), 50).setLabel(Account::name));
```

The expand buttons are real buttons, with `aria-expanded` and "Expand/Collapse
{name}" labels, so they work with the keyboard and screen readers. After a toggle,
focus stays on the button. A selected node is marked with `aria-current`.

## Paging

`OatPagingNavigator` and `OatAjaxPagingNavigator` are Wicket's `PagingNavigator`
and `AjaxPagingNavigator` rendered as Oat pagination, for any `IPageable`
(`DataView`, `PageableListView`, `DataTable`, ...). `OatDataTable` uses
`OatPagingNavigator` for its navigation toolbar.

```java
add(new OatAjaxPagingNavigator("navigator", dataView));
```

## Content Security Policy

Wicket Oat works under Wicket's default strict, nonce-based CSP. The library
renders no inline scripts or `style` attributes, and Oat's JavaScript only sets
styles through the CSSOM, which CSP doesn't restrict. `WicketOats.install(app)`
adds just `img-src data:` (for the icons Oat's CSS embeds as `data:` URLs) and
leaves the rest of your policy alone, so add sources for your own content
yourself:

```java
getCspSettings().blocking().add(CSPDirective.IMG_SRC, "https://images.example.com");
```

If your own markup relies on inline `style` attributes, allow them for styles
only. Browsers ignore `'unsafe-inline'` while a nonce is present, so replace the
directive rather than adding to it:

```java
getCspSettings().blocking()
        .remove(CSPDirective.STYLE_SRC)
        .add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF, CSPDirectiveSrcValue.UNSAFE_INLINE);
```

Call `install()` after any `strict()`/`clear()` of your own, since those reset
the directives it adds.

The context menu and a load-on-scroll `OatLoadMoreList` use a small script of the
library's own, `wicket-oat.js`. Wicket renders it with the page's nonce, and it
only reads `data-` attributes, so they need nothing added to the policy.

## Translations

The few texts Wicket Oat renders itself are Wicket resources, translated into
German (`de`), Spanish (`es`), French (`fr`), Italian (`it`), Japanese (`ja`),
Dutch (`nl`) and Portuguese (`pt`), with English as the fallback. They follow
the component's locale (usually the session's), just like Wicket's own
validation messages. To change one, or to add a language, define its key in
the properties of the page, or of a base page all your pages extend, e.g.
`BasePage_de.properties`:

```properties
OatDialog.confirm=Speichern
OatTagInput.placeholder=Schlagwörter hinzufügen...
```

A component's own properties are found before the application's, so to use
`MyApplication_de.properties` instead, have Wicket search it first:

```java
getResourceSettings().getStringResourceLoaders()
        .add(0, new ClassStringResourceLoader(MyApplication.class));
```

| Key | English |
| :--- | :--- |
| `OatDialog.cancel` / `OatDialog.confirm` | Cancel / Confirm |
| `OatAppLayout.toggleMenu` | Toggle menu |
| `OatAppLayout.title` / `OatAppLayout.name` | Wicket Oat Application (not translated: meant to be overridden) |
| `OatPagingNavigator.label` | Pagination |
| `OatFileDropzone.choose` / `OatFileDropzone.hint` | Choose files / Drop files here or click to choose |
| `OatTagInput.placeholder` | Add tags... |
| `IConverter.BigDecimal` on `OatMoneyField` / `OatPercentField` | The value of '${label}' is not a valid amount / percentage. |

## Resources

`install()` adds Oat's CSS and JS to every page. The references are public
(`WicketOats.OAT_CSS`, `OAT_JS`, `THEMES_CSS`, `WICKET_OAT_CSS`), so Wicket's own
resource replacement can swap them, e.g. to serve Oat from a CDN:

```java
addResourceReplacement(WicketOats.OAT_JS,
        new UrlResourceReference(Url.parse("https://cdn.example.com/oat.min.js")));
```

To add them only on the pages that use Oat, turn the automatic contribution off
and render them yourself:

```java
WicketOats.install(this).setAddResources(false);

// in a page's renderHead(IHeaderResponse response)
WicketOats.renderResources(response);
```

## Forms

Oat form fields (`OatTextField`, `OatDropdownChoice`, `OatSwitch`, ...) wrap a
label, the Wicket form component and an inline feedback/hint message, but are
meant to be used like the plain Wicket components they wrap:

```java
Form<Person> form = new Form<>("form", new CompoundPropertyModel<>(person));
form.setOutputMarkupId(true);
add(form);

// Model inherited from the CompoundPropertyModel by id ("name" -> person.name),
// label looked up by id in the page's .properties file (name=Full name)
OatTextField<String> name = new OatTextField<String>("name").setRequired(true);
name.add(StringValidator.maximumLength(50)); // validators and Ajax form-component behaviors go to the wrapped input
form.add(name);

form.add(new OatNumberField<Integer>("age").setMin(0)); // fluent setters keep the concrete type
form.add(new OatDropdownChoice<String>("country", Model.ofList(countries)));

form.add(new AjaxButton("save") {
    @Override
    protected void onError(AjaxRequestTarget target) {
        target.add(form); // fields render their own tag, so they can be re-rendered to show inline errors
    }
});
```

You can still pass a model and a label explicitly
(`new OatTextField<>("name", "Full name", model)`), and `getField()` gives
access to the wrapped `FormComponent` for anything else.

### Addons, money and sections

Fields that render a single input (text, number, email, URL, date, ...) can show
text before or after it, using Oat's input group:

```java
form.add(new OatTextField<String>("website").setPrefix("https://"));
form.add(new OatNumberField<Integer>("weight").setSuffix("kg"));
```

`OatMoneyField` and `OatPercentField` edit a `BigDecimal` in the user's locale:
`1.234,50` in German, `1,234.50` in English. The money field takes the number of
decimals from its currency and shows the currency symbol where the locale puts it
(`$ 12.50`, `12,50 €`). Input with more decimals is rounded half-even.

```java
form.add(new OatMoneyField("price")
        .setCurrency(Currency.getInstance("EUR")) // or an IModel<Currency>, e.g. from the record
        .setMin(BigDecimal.ZERO));
form.add(new OatPercentField("taxRate")); // 19 means 19%, shown with a % suffix
```

`OatFieldset` splits a long form into titled sections. It is a Wicket `Border`
on a `<fieldset>` tag, so the fields inside still inherit their models from the
form's `CompoundPropertyModel`, and its legend comes from the `.properties` file
by id:

```html
<fieldset wicket:id="billing">
    <div wicket:id="street"></div>
    <div wicket:id="city"></div>
</fieldset>
```

```java
OatFieldset billing = new OatFieldset("billing").setDescription("Where the invoice goes.");
billing.add(new OatTextField<String>("street"), new OatTextField<String>("city"));
form.add(billing);
```

## Layout Utilities

Wicket Oat uses Oat's CSS and no other CSS library, so there is no Tailwind or
Bootstrap to learn. For layout, Oat's semantic styling of plain HTML does most
of the work. For the rest, Oat ships a small, fixed set of utility classes
(in `oat.min.css`, added by `WicketOats.install(this)`):

| Purpose | Classes |
| :--- | :--- |
| Stacks | `vstack` (column), `hstack` (row that wraps, items centered) - both with a default gap |
| Flexbox | `flex`, `flex-col`, `items-center`, `justify-center`, `justify-between`, `justify-end` |
| Gap | `gap-1`, `gap-2`, `gap-4`, `gap-6` |
| Margin | `mt-2`, `mt-4`, `mt-6`, `mt-8`, `mb-2`, `mb-4`, `mb-6`, `mb-8` |
| Padding / width | `p-4`, `w-100` |
| Text | `align-left`, `align-center`, `align-right`, `text-light`, `text-lighter` |
| Lists and links | `unstyled` on a `ul`/`ol` (no bullets) or an `a` (no link styling) |
| Grid | `container`, `row`, `col-1` ... `col-12`, `offset-1` ... `offset-6` |

The numbers refer to Oat's spacing scale (`--space-1`, `--space-2`, ...), not
to pixels, and only the classes listed exist: there is no `gap-3` or `mt-5`.
A form with right-aligned actions, for example:

```html
<form wicket:id="form" class="vstack gap-4">
    <div wicket:id="name"></div>
    <div wicket:id="email"></div>
    <footer class="hstack justify-end mt-2">
        <button wicket:id="submit"></button>
    </footer>
</form>
```

The 12-column grid drops to a single column on narrow screens, so it lays out
forms well. `Oat.Behaviors` adds its classes from Java, including to Oat form
fields, whose own tag takes them:

```java
OatFieldset customer = new OatFieldset("customer");
customer.add(Oat.Behaviors.row());
customer.add(new OatTextField<String>("name").add(Oat.Behaviors.col(6)));
customer.add(new OatEmailField("email").add(Oat.Behaviors.col(6)));
```

`Oat.Behaviors.container()`, `col(span, offset)`, `hstack()` and `vstack()` work
the same way.

Anything beyond that belongs in your own stylesheet, using Oat's CSS variables
(`var(--space-4)`, `var(--primary)`, ...) so it follows the current theme.

## Theming

Wicket Oat ships 19 themes, and `OatAppLayout` applies the current user's theme
as the `data-theme` attribute on `<html>`. The choice is kept in a cookie
(cached in the session), so it survives the session and works with any session
class:

```java
// In WebApplication.init(): defaults, and custom themes defined in your CSS
WicketOats.install(this)
        .setDefaultTheme(OatTheme.LIGHT)                 // or null to follow the browser
        .addTheme(new OatTheme("brand", "Brand", "🏷️"));

// Anywhere during a request
OatTheme.setCurrent(OatTheme.NORD);
OatTheme theme = OatTheme.current();

// A picker for all registered themes, and a component pinned to one theme
add(new OatThemeSwitcher("themes"));
card.add(Oat.Behaviors.theme(OatTheme.LIGHT));
```

A custom theme is a `[data-theme="brand"] { --background: ...; --primary: ...; }`
block overriding Oat's CSS variables. To keep the choice somewhere else - only in
the session, or in a user profile - pass an `OatThemeStore` to
`setThemeStore(...)` (`SessionThemeStore` is built in). Without `OatAppLayout`,
add `Oat.Behaviors.theme()` to a `TransparentWebMarkupContainer` on your
`<html wicket:id="html">` tag.

### Built-in Themes:
`DARK`, `LIGHT`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`, `BUSINESS`, `BUSINESS_DARK`.

`BUSINESS` and `BUSINESS_DARK` are a sober light/dark pair for line-of-business
apps: neutral surfaces, a corporate-blue primary, status colors that meet WCAG AA
contrast, tighter corner radii, and tabular figures in tables.

### Density

Independent of the theme, `OatDensity.COMPACT` tightens Oat's spacing scale and
uses smaller body text, for data-heavy apps with many forms and tables.
`OatAppLayout` applies it as `data-density` on `<html>`:

```java
WicketOats.install(this)
        .setDefaultTheme(OatTheme.BUSINESS)
        .setDensity(OatDensity.COMPACT);

// Or only for one component, e.g. a large table on an otherwise default page
table.add(Oat.Behaviors.density(OatDensity.COMPACT));
```

Without `OatAppLayout`, add `Oat.Behaviors.density()` next to
`Oat.Behaviors.theme()` on your `<html>` container.

## Running the Examples

The project includes an examples module `wicket-oat-examples`. To run it:

1. Clone the repository.
2. Install the library into your local Maven repository, so the examples
   module can find it when run on its own (repeat this after changing
   `wicket-oat-core`):
   ```bash
   ./mvnw install -DskipTests
   ```
3. Run the application using Maven (it starts MongoDB through Docker Compose,
   so Docker must be running; the argument points it at the repository's
   `compose.yaml`, since the app runs in the module directory):
   ```bash
   ./mvnw spring-boot:run -pl wicket-oat-examples \
       -Dspring-boot.run.arguments=--spring.docker.compose.file=../compose.yaml
   ```
4. Access the demo at `http://localhost:8080/`.

## AI-Powered Development

Wicket Oat ships a standard Agent Skill at [`wicket-oat/`](wicket-oat/) — a plain `SKILL.md` plus a `references/` folder — to help you build UI faster with an AI coding agent. It gives the agent deep knowledge of the Oat API, component library, and theming system, and works with any tool that supports this directory-based skill convention.

### Installing the Skill
Point your agent at the `wicket-oat/` directory, or copy it into your agent's own skills folder. For example, with Claude Code:

```bash
cp -r wicket-oat .claude/skills/wicket-oat
```

Check your agent's own documentation for how it discovers or loads skills.

### How to Use
Once installed, the AI agent will automatically understand instructions like:
- "Add a primary Oat button to this page that shows a success toast on click."
- "Turn this standard Wicket Label into an Oat success badge."
- "Create a registration form using Oat components."

## Requirements

- Java 17 or higher
- Apache Wicket 10.8.0+
- Maven

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

It bundles [Oat UI](https://github.com/knadh/oat) by Kailash Nadh, also MIT
licensed - see [LICENSE-oat.txt](wicket-oat-core/src/main/resources/META-INF/LICENSE-oat.txt).
