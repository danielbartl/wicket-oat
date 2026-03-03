# Wicket Oat Behaviors

Use `Oat.Behaviors` for the "Power Path" to style existing Wicket components via composition.

## Available Behaviors
- `alert(variant?)`: Applies alert styling and `role="alert"`.
- `badge(variant?)`: Applies badge styling.
- `button()`: Styles any component (Link, Button, etc.) as an Oat button.
- `card()`: Styles a container as a card.
- `field()`: Styles a form field container.
- `hint()`: Styles a small text hint inside a field.
- `switchBehavior()`: Turns a `CheckBox` into a visual toggle switch.
- `tooltip(text)`: Adds a styled tooltip.
- `spinner(size?)`: Adds a spinner overlay/logic.
- `skeleton(shape?)`: Adds a skeleton loading effect.
- `theme()`: Allows programmatic theme application to a component.
- `clientSideClick(script)`: Efficiently runs JS on click while maintaining CSP.

## Composition Example
```java
// Style a standard Wicket AjaxLink as a primary Oat button
add(new AjaxLink<Void>("id") {
    @Override
    public void onClick(AjaxRequestTarget target) { ... }
}.add(Oat.Behaviors.button().setVariant(ButtonBehavior.Variant.PRIMARY)));
```
