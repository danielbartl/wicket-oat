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
myLabel.add(Oat.Behaviors.badge(Variant.SUCCESS));
```

## Core Philosophy: Fast Path vs Power Path

- **Fast Path (`Oat.Components`)**: Use for standard UI widgets with encapsulated markup. Great for rapid development.
- **Power Path (`Oat.Behaviors`)**: Use for custom logic and deep customization. Add behaviors to *any* standard Wicket component.

## Reference Material

- [COMPONENTS.md](references/components.md): Full list of available components and form fields.
- [BEHAVIORS.md](references/behaviors.md): Detailed guide on styling via composition.
- [THEMING.md](references/theming.md): How to switch and define themes.

## Common Tasks

### Form Handling
Use `CompoundPropertyModel` with `Oat.Components` form fields for clean, readable code.
```java
Form<MyData> form = new Form<>("form", new CompoundPropertyModel<>(data));
form.add(Oat.Components.textField("name", "Name", model.bind("name")));
```

### Toasts & Notifications
Trigger toasts from Java via the `Oat` facade:
```java
Oat.toast(target, "Message", OatToastBehavior.Variant.SUCCESS, "Title");
```
