# Wicket Oat Components

Use `Oat.Components` for a "Fast Path" to build UI with encapsulated markup.

## General
- `alert(id, variant?)`: Creates an `OatAlert`.
- `badge(id, label, variant?)`: Creates an `OatBadge`.
- `button(id, label, onClick)`: Creates an `OatButton` with an Ajax click handler.
- `card(id)`: Creates an `OatCard` container.
- `avatar(id, urlModel, labelModel, size?)`: Creates an `OatAvatar`.
- `emptyState(id, title, message?)`: Creates an `OatEmptyState` placeholder.

## Layout & Navigation
- `accordion(id, model, populateItem)`: Creates an `OatAccordion`.
- `buttonGroup(id)`: Creates an `OatButtonGroup` container for buttons.
- `OatAppLayout`: (Base Page) Provides sidebar and topnav.

## Form Components
All form components follow the naming `Oat[Type]Field` and are available via `Oat.Components.[type]Field`.

- `textField(id, label, model)`
- `passwordField(id, label, model)`
- `emailField(id, label, model)`
- `numberField(id, label, model)`
- `textArea(id, label, model)`
- `dropdownChoice(id, label, model, choices)`
- `checkBox(id, label, model)`
- `oatSwitch(id, label, model)`
- `dateField(id, label, model)`
- `colorField(id, label, model)`
- `urlField(id, label, model)`
- `fileUpload(id, label, model)`

## Data & Feedback
- `dataTable(id, columns, dataProvider, rowsPerPage)`: Styled Wicket DataTable.
- `progress(id, value)`: Standard `<progress>` bar.
- `meter(id, value)`: Advanced `<meter>` for ranges.
- `spinner(id, size?)`: Loading indicator.
- `skeleton(id, shape?)`: Placeholder for loading content.

## Usage Example
```java
add(Oat.Components.button("myBtn", "Save", target -> {
    // Save logic
    Oat.toast(target, "Saved!", OatToastBehavior.Variant.SUCCESS);
}));
```
