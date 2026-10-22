---
name: wicket-oat
description: Support for the Wicket Oat UI library — a themeable Java/Apache Wicket component library (buttons, forms, cards, dialogs, tabs, toasts, badges, dark/light themes, etc.). Use whenever writing or modifying Apache Wicket pages/components in this project: adding UI elements, building forms, styling an existing Wicket component, showing toasts or alerts, or switching/defining themes — even if the user doesn't mention "Oat" by name. Covers the components (constructors, and lambda factories like OatButton.onClick for callback-driven ones) and Oat.Behaviors (Oat styling for any Wicket component), plus the full built-in theme list and how to switch or define custom themes.
---

# Wicket Oat Support

This skill helps you build modern Wicket applications using the Oat UI library.

## Quick Start

### 1. Installation
In `WebApplication.init()`:
```java
WicketOats.install(this);
```

### 2. Base Layout
Extend `OatAppLayout` for your main pages to get the sidebar and topnav structure, or `OatMobileLayout` for a phone-first app with a bottom tab bar (`OatBottomNav`).

### 3. Creating UI
Oat components are plain Wicket components, created with `new`; those driven by a callback have a static factory taking a lambda, like Wicket's `AjaxLink.onClick(...)`:
```java
// A component
add(new OatAlert("saved", "Your changes were saved.", OatVariant.SUCCESS));

// A button, without an anonymous subclass
add(OatButton.onClick("id", "Label", target -> { ... }));

// A behavior on an existing component
myLabel.add(Oat.Behaviors.badge(OatVariant.SUCCESS));
```

## Components and Behaviors

- **Components** (`new OatAlert(...)`, `OatButton.onClick(...)`): standard UI widgets with encapsulated markup.
- **Behaviors** (`Oat.Behaviors`): Oat styling for *any* standard Wicket component, for custom logic and deep customization.

## Layout and Styling: Oat CSS Only

Wicket Oat uses Oat's CSS and nothing else. Never add Tailwind, Bootstrap or other utility classes. Prefer plain semantic HTML (Oat styles `form`, `fieldset`, `header`, `footer`, `nav`, `table`, `dialog`, ... directly), and use only Oat's built-in utility classes for layout:

- Stacks: `vstack`, `hstack`
- Flexbox: `flex`, `flex-col`, `items-center`, `justify-center`, `justify-between`, `justify-end`
- Spacing: `gap-1`, `gap-2`, `gap-4`, `gap-6`, `mt-2`/`4`/`6`/`8`, `mb-2`/`4`/`6`/`8`, `p-4`, `w-100`
- Text: `align-left`, `align-center`, `align-right`, `text-light`, `text-lighter`
- Lists and links: `unstyled`
- From `wicket-oat.css`: `oat-divider` (a line with a word in the middle, e.g. `<div class="oat-divider">or</div>`); `<kbd>` is styled as a key
- Grid: `container`, `row`, `col-1`...`col-12`, `offset-1`...`offset-6` (or `Oat.Behaviors.row()`/`col(6)`/`container()` from Java)

No other sizes exist (no `gap-3`, `mt-5`, `px-4`, ...). For anything else, write a small CSS rule in the application's own stylesheet using Oat's CSS variables (`var(--space-4)`, `var(--primary)`, ...).

## Reference Material

- [COMPONENTS.md](references/components.md): Full list of available components and form fields.
- [BEHAVIORS.md](references/behaviors.md): Detailed guide on styling via composition.
- [THEMING.md](references/theming.md): How to switch and define themes.

## Common Tasks

### Form Handling
Oat form fields work like plain Wicket form components: with a `CompoundPropertyModel` on the form, leave out the model (it is inherited by id) and the label (looked up by id in the `.properties` file).
```java
Form<MyData> form = new Form<>("form", new CompoundPropertyModel<>(data));
form.add(new OatTextField<String>("name").setRequired(true)); // MyPage.properties: name=Name
form.add(OatSubmitButton.onSubmit("save", "Save", target -> ...));
// After an Ajax submit the fields show (and clear) their errors themselves: no target.add(form) needed
```
For the form's own messages next to it, e.g. `form.error("...")`, add `form.add(new OatFeedbackPanel("formFeedback", form))`; page-wide feedback and toasts then leave them out.

### Toasts & Notifications
Prefer Wicket's own feedback messages and let Oat display them - as toasts with `add(Oat.Behaviors.feedbackToasts())` on the (base) page, or as alerts with `add(new OatFeedbackPanel("feedback"))`. Both update during Ajax requests on their own:
```java
success("Saved."); // in any event handler; no target.add(...) needed
```
For a one-off toast with a title, call the `Oat` facade directly:
```java
Oat.toast(target, "Message", OatVariant.SUCCESS, "Title");
```
