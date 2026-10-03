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

## Quick Start

### 1. Add Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>wicket-oat-core</artifactId>
    <version>0.1.0</version>
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

- **General:** `OatButton`, `OatBadge`, `OatAvatar`, `OatAvatarGroup`, `OatAlert`, `OatCard`, `OatEmptyState`, `OatFeedbackPanel`
- **Navigation/Layout:** `OatAppLayout`, `OatAccordion`, `OatButtonGroup`, `OatBreadcrumb`, `OatPagingNavigator`, `OatAjaxPagingNavigator`, `OatPagination`
- **Overlays:** `OatDialog`, `OatDropdown`, `OatTabbedPanel`, `OatTabs`
- **Data Display:** `OatDataTable`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`
- **Forms:** `OatTextField`, `OatCheckBox`, `OatDropdownChoice`, `OatRadioChoice`, `OatCheckBoxMultipleChoice`, `OatListMultipleChoice`, `OatTextArea`, `OatSwitch`, `OatTagInput`, `OatFileUpload`, `OatFileDropzone`, and more specialized HTML5 fields.

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

Anything beyond that belongs in your own stylesheet, using Oat's CSS variables
(`var(--space-4)`, `var(--primary)`, ...) so it follows the current theme.

## Theming

Wicket Oat ships 17 themes, and `OatAppLayout` applies the current user's theme
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
`DARK`, `LIGHT`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`.

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
