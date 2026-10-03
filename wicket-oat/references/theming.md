# Wicket Oat Theming

Theming is controlled by the `data-theme` attribute, which `OatAppLayout` sets on `<html>` from the current user's theme. There is no special session class: the choice is kept by an `OatThemeStore` - by default `CookieThemeStore` (an `oat-theme` cookie kept for a year, cached in the session).

## Built-in Themes
`OatTheme` constants: `LIGHT`, `DARK`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`. `OatTheme` is a record (`value`, `label`, `icon`); `OatTheme.builtIns()` lists them.

## Configuring
`WicketOats.install(app)` returns the application's `OatSettings`:

```java
WicketOats.install(this)
        .setDefaultTheme(OatTheme.LIGHT)                 // DARK by default; null follows the browser
        .addTheme(new OatTheme("custom-blue", "Custom Blue", "🔵"))
        .setThemeStore(new SessionThemeStore());         // optional: session only, no cookie
```

`setThemes(List.of(...))` limits which themes users can pick. `OatSettings.get()` returns the settings during a request.

## Changing Themes
```java
OatTheme.setCurrent(OatTheme.NORD);   // for the current user; null goes back to the default
OatTheme theme = OatTheme.current();  // the stored choice if registered, else the default (may be null)
```

`new OatThemeSwitcher("id")` renders a link per registered theme (icon, or label if none), marking the current one with `aria-current="true"`.

## Applying a Theme
- `OatAppLayout` applies the current theme to `<html>` automatically.
- With your own base page: `TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html"); html.add(Oat.Behaviors.theme());` on `<html wicket:id="html">`.
- `Oat.Behaviors.theme(OatTheme.LIGHT)` pins one component to a theme, whatever the user chose.

## Custom Themes
Define the CSS variables, then register the theme so it can be chosen:

```css
[data-theme="custom-blue"] {
    color-scheme: dark;
    --background: #001f3f;
    --foreground: #ffffff;
    --card: #00264d;
    --primary: #0074d9;
    --border: #003366;
}
```

Theme values must be lowercase letters, digits and dashes. Only registered themes are accepted from the cookie; an unknown value falls back to the default.
