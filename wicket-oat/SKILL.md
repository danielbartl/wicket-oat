---
name: wicket-oat
description: Support for the Wicket Oat UI library — a themeable Java/Apache Wicket component library (buttons, forms, cards, dialogs, tabs, toasts, badges, dark/light themes, etc.). Use whenever writing or modifying Apache Wicket pages/components in this project: adding UI elements, building forms, styling an existing Wicket component, showing toasts or alerts, or switching/defining themes — even if the user doesn't mention "Oat" by name. Covers Oat.Components (Fast Path factory methods for common widgets) and Oat.Behaviors (Power Path composition for custom components/styling), plus the full built-in theme list and how to switch or define custom themes.
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
Extend `OatAppLayout` for your main pages to get the sidebar and topnav structure.

### 3. Creating UI
Use the `Oat` factory for the most common tasks:
```java
// Add a button
add(Oat.Components.button("id", "Label", target -> { ... }));

// Add a behavior to an existing component
myLabel.add(Oat.Behaviors.badge(OatVariant.SUCCESS));
```

## Core Philosophy: Fast Path vs Power Path

- **Fast Path (`Oat.Components`)**: Use for standard UI widgets with encapsulated markup. Great for rapid development.
- **Power Path (`Oat.Behaviors`)**: Use for custom logic and deep customization. Add behaviors to *any* standard Wicket component.

## Layout and Styling: Oat CSS Only

Wicket Oat uses Oat's CSS and nothing else. Never add Tailwind, Bootstrap or other utility classes. Prefer plain semantic HTML (Oat styles `form`, `fieldset`, `header`, `footer`, `nav`, `table`, `dialog`, ... directly), and use only Oat's built-in utility classes for layout:

- Stacks: `vstack`, `hstack`
- Flexbox: `flex`, `flex-col`, `items-center`, `justify-center`, `justify-between`, `justify-end`
- Spacing: `gap-1`, `gap-2`, `gap-4`, `gap-6`, `mt-2`/`4`/`6`/`8`, `mb-2`/`4`/`6`/`8`, `p-4`, `w-100`
- Text: `align-left`, `align-center`, `align-right`, `text-light`, `text-lighter`
- Lists and links: `unstyled`
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
form.setOutputMarkupId(true);
form.add(new OatTextField<String>("name").setRequired(true)); // MyPage.properties: name=Name
// In an AjaxButton's onError: target.add(form) shows the inline validation errors
```

### Toasts & Notifications
Prefer Wicket's own feedback messages and let Oat display them - as toasts with `add(Oat.Behaviors.feedbackToasts())` on the (base) page, or as alerts with `add(new OatFeedbackPanel("feedback"))`. Both update during Ajax requests on their own:
```java
success("Saved."); // in any event handler; no target.add(...) needed
```
For a one-off toast with a title, call the `Oat` facade directly:
```java
Oat.toast(target, "Message", OatVariant.SUCCESS, "Title");
```
