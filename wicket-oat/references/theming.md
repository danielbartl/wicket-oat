# Wicket Oat Theming

Theming is controlled by the `data-theme` attribute on the `<html>` or container tags.

## Built-in Themes
- `LIGHT`, `DARK`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`.

## Changing Themes
Themes are managed via `OatSession`.

```java
// Globally change for the user session
OatSession.get().setTheme(OatTheme.NORD);
```

## Custom Themes
Define custom themes in your CSS:

```css
[data-theme="custom-blue"] {
    color-scheme: dark;
    --background: #001f3f;
    --primary: #0074d9;
    --foreground: #ffffff;
}
```

`OatSession.setTheme(OatTheme)` only accepts the built-in `OatTheme` enum values above, so it cannot select a custom theme name like `custom-blue`. To apply one, set the `data-theme` attribute yourself instead of going through `OatSession` — e.g. hardcode it in markup (`<html data-theme="custom-blue">`) or add an `AttributeModifier("data-theme", "custom-blue")` to the component that carries the attribute.
