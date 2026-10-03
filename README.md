# Wicket Oat

[![Build](https://github.com/danielbartl/wicket-oat/actions/workflows/build.yml/badge.svg)](https://github.com/danielbartl/wicket-oat/actions/workflows/build.yml)

Wicket Oat is a modern, lightweight, and themeable UI component library for [Apache Wicket](https://wicket.apache.org/). It provides a set of high-quality components and behaviors built on top of a sleek, modern design system.

**[Live demo & docs](https://danielbartl.github.io/wicket-oat/)** &middot; **[Step-by-step tutorial](https://danielbartl.github.io/wicket-oat/tutorial.html)**

## Features

- **Fluent API:** Easily create components and behaviors using the `Oat` factory class.
- **Modern Design:** Beautifully designed components like Buttons, Cards, Modals, and more.
- **Themeable:** Built-in support for multiple themes (Light, Dark, Midnight, Nord, etc.) that can be switched dynamically.
- **Wicket 10+:** Fully compatible with Apache Wicket 10 and Java 25.
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
    <version>0.0.1-SNAPSHOT</version>
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
    Oat.toast(target, "Hello from Wicket Oat!", OatToastBehavior.Variant.SUCCESS);
}));

// Create an alert via Components factory
add(Oat.Components.alert("myAlert", "Your changes were saved.", AlertBehavior.Variant.SUCCESS));

// Create a badge via Components factory
add(Oat.Components.badge("myBadge", "New", BadgeBehavior.Variant.SUCCESS));
```

Every Oat component is also a plain Wicket component, so for components that
don't need a callback or a list of items, a regular constructor works just as
well:

```java
// The alert and badge above, built directly with their constructors
add(new OatAlert("myAlert", "Your changes were saved.", AlertBehavior.Variant.SUCCESS));
add(new OatBadge("myBadge", "New", BadgeBehavior.Variant.SUCCESS));
```

`OatButton` above is one of the components whose constructor requires
subclassing (see [Usage Strategies](#usage-strategies) below), so the factory
is the more natural choice for it.

## Available Components

- **General:** `OatButton`, `OatBadge`, `OatAvatar`, `OatAvatarGroup`, `OatAlert`, `OatCard`, `OatEmptyState`, `OatFeedbackPanel`
- **Navigation/Layout:** `OatAppLayout`, `OatAccordion`, `OatButtonGroup`, `OatBreadcrumb`, `OatPagingNavigator`, `OatAjaxPagingNavigator`, `OatPagination`
- **Overlays:** `OatDialog`, `OatDropdown`, `OatTabs`
- **Data Display:** `OatDataTable`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`
- **Forms:** `OatTextField`, `OatCheckBox`, `OatDropdownChoice`, `OatTextArea`, `OatSwitch`, `OatTagInput`, `OatFileUpload`, `OatFileDropzone`, and more specialized HTML5 fields.

## Architecture: Components vs. Behaviors

Wicket Oat provides both **Components** and **Behaviors** for almost every UI element. This dual approach gives you maximum flexibility depending on your needs.

### When to use Components (`Oat.Components`)
Use a component when you want a self-contained UI widget and don't want to worry about the underlying HTML structure.
- **Pros:** Easiest to use; encapsulates markup logic; handles internal structure (like headers/footers in a Card).
- **Example:** `add(Oat.Components.alert("id", "Saved!", Variant.SUCCESS))`
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
add(Oat.Components.button("id", "Click Me", target -> ...));
```

### When to use a constructor
Every other component — `OatAlert`, `OatBadge`, `OatCard`, `OatDialog`,
`OatAvatar`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`,
`OatDataTable`, `OatEmptyState`, the form fields, and more — has no abstract
methods, so a plain constructor works just as well, and is the more familiar
style if you're used to plain Wicket:
```java
add(new OatAlert("id", "Saved!", AlertBehavior.Variant.SUCCESS));
```
For `OatAlert`/`OatBadge` the constructor is actually the *more* capable
option: it has an overload that binds the variant to a reactive
`IModel<Variant>` which the factory doesn't expose. `OatDialog` and
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
        add(Oat.Behaviors.button().setVariant(Variant.SECONDARY));
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

## Theming

Wicket Oat comes with several built-in themes. You can apply a theme to your session or specific components:

```java
// Set a global theme in your session (if using OatSession)
OatSession.get().setTheme(OatTheme.DARK);

// Or add the theme behavior to a component/page via Behaviors factory
add(Oat.Behaviors.theme().setTheme(OatTheme.NORD));
```

### Built-in Themes:
`DARK`, `LIGHT`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`.

## Running the Examples

The project includes an examples module `wicket-oat-examples`. To run it:

1. Clone the repository.
2. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run -pl wicket-oat-examples
   ```
3. Access the demo at `http://localhost:8080/`.

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

- Java 25 or higher
- Apache Wicket 10.8.0+
- Maven

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
