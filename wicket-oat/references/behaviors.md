# Wicket Oat Behaviors

Use `Oat.Behaviors` for the "Power Path" to style existing Wicket components via composition.

## Available Behaviors
- `accordion(exclusive?)`: Turns a container into an accordion.
- `ajaxBusy()`: While an Ajax link/button's request runs, sets `aria-busy="true"` (Oat's spinner) and drops further clicks, so it can't run twice. `OatButton`, `OatSubmitButton` and `OatDialog` (its confirm button) turn it on with `setBusyIndicator(true)`; it's off by default. Needs `WicketOats.install`.
- `alert(variant?)`: Applies alert styling and `role="alert"`.
- `badge(variant?)`: Applies badge styling.
- `button()`: Styles any component (Link, Button, etc.) as an Oat button.
- `buttonGroup()`: Styles a container as a button group.
- `card()`: Styles a container as a card.
- `contextMenu()`: `ContextMenuBehavior`, a menu of actions opened by right-click, Shift+F10 or the Menu key on the component. `addAction(label, target -> ...)`, `addAction(IModel label, OatVariant.DANGER, ...)`. It's keyboard navigable and returns focus to the component when it closes. Uses `wicket-oat.js` (CSP-safe).
- `field()`: Styles a form field container.
- `hint()`: Styles a small text hint inside a field.
- `inputGroup()`: Joins a `<fieldset>`'s inputs, buttons and `<label>`/`<legend>` addons into one control (`class="group"`). Oat fields have `setPrefix`/`setSuffix` for this.
- `row()`, `col(span)`, `col(span, offset)`, `container()`: Oat's 12-column grid (`row`, `col-1`...`col-12`, `offset-1`...`offset-6`, `container`); it drops to one column on narrow screens. On an Oat form field they apply to the field's own tag.
- `hstack()` / `vstack()`: A wrapping row or a column with Oat's standard gap.
- `oatSwitch()`: Turns a `CheckBox` into a visual toggle switch.
- `tooltip(text)`: Adds a styled tooltip.
- `spinner(size?)`: Adds a spinner overlay/logic.
- `skeleton(shape?)`: Adds a skeleton loading effect.
- `theme()`: Allows programmatic theme application to a component.
- `density()` / `density(OatDensity)`: Applies the configured or a given density (`data-density`), e.g. a compact table, or `DEFAULT` spacing for one part of a compact app.
- `clientSideClick(javascript)`: Efficiently runs JS on click while maintaining CSP.
- `feedbackToasts(filter?)`: Add to a page (or base page) to show `info()`/`success()`/`warn()`/`error()` feedback messages - including session messages - as Oat toasts, on full renders and during Ajax requests without touching the `AjaxRequestTarget`. By default leaves out errors Oat form fields already show inline.

## Composition Example
```java
// Style a standard Wicket AjaxLink as a primary Oat button (the default variant)
add(new AjaxLink<Void>("id") {
    @Override
    public void onClick(AjaxRequestTarget target) { ... }
}.add(Oat.Behaviors.button()));
```
