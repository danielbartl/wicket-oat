# Wicket Oat Components

Use `Oat.Components` for a "Fast Path" to build UI with encapsulated markup.

## General
- `alert(id, variant?)`: Creates an `OatAlert`.
- `badge(id, label, variant?)`: Creates an `OatBadge`.
- `button(id, label, onClick)`: Creates an `OatButton` with an Ajax click handler.
- `card(id)`: Creates an `OatCard` container.
- `avatar(id, urlModel, labelModel, size?)`: Creates an `OatAvatar`.
- `avatarGroup(id, model, size?, populateItem)`: Creates an `OatAvatarGroup` (a clustered/overlapping set of avatars).
- `emptyState(id, title, message?)`: Creates an `OatEmptyState` placeholder.

## Layout & Navigation
- `accordion(id, model, exclusive?, populateItem)`: Creates an `OatAccordion`.
- `buttonGroup(id, model, populateItem)`: Creates an `OatButtonGroup` container for buttons.
- `breadcrumb(id, model, populateItem)`: Creates an `OatBreadcrumb` trail.
- `pagination(id, model, populateItem)`: Creates an `OatPagination` control.
- `dropdown(id, triggerLabel, model, populateItem)`: Creates an `OatDropdown` popover menu.
- `tabs(id, model, populateTab, populatePanel)`: Creates an `OatTabs` tab strip + panels.
- `dialog(id, triggerLabel, header)`: Creates an `OatDialog` modal, opened via its trigger.
- `OatAppLayout`: (Base Page) Provides sidebar and topnav.

## Form Components
All form components follow the naming `Oat[Type]Field` and are available via `Oat.Components.[type]Field`.
Every one also takes an optional trailing hint-text model:
`Oat.Components.[type]Field(id, label, model, helper?)`.

- `textField(id, label, model)`
- `passwordField(id, label, model)`
- `emailField(id, label, model)`
- `numberField(id, label, model)`
- `textArea(id, label, model)`
- `dropdownChoice(id, label, model, choices, renderer?)`
- `checkBox(id, label, model)`
- `oatSwitch(id, label, model)`
- `dateField(id, label, model)`
- `dateTimeLocalField(id, label, model)`
- `timeField(id, label, model)`
- `colorField(id, label, model)`
- `urlField(id, label, model)`
- `searchField(id, label, model)`
- `telField(id, label, model)`
- `monthField(id, label, model)`
- `weekField(id, label, model)`
- `rangeField(id, label, model)`
- `fileUpload(id, label, model)`
- `fileDropzone(id, label, model)`: Drag-and-drop alternative to `fileUpload`.
- `tagInput(id, label, model)`: Comma-separated tag/chip input.

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
