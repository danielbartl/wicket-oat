# Wicket Oat Components

Use `Oat.Components` for a "Fast Path" to build UI with encapsulated markup.

## General
- `alert(id, message?, variant?)`: Creates an `OatAlert`. `message`/`variant` can each be a plain value, an `IModel`, or omitted.
- `badge(id, label, variant?)`: Creates an `OatBadge`. `label` can be a `String` or an `IModel<?>`.
- `button(id, label, onClick)`: Creates an `OatButton` with an Ajax click handler.
- `card(id)`: Creates an `OatCard` container.
- `avatar(id, urlModel)` / `avatar(id, initials)`: Creates an `OatAvatar` from just an image URL or just initials.
- `avatar(id, urlModel, initials, size)`: Creates an `OatAvatar` with both an image and initials fallback; `size` is required in this overload.
- `avatarGroup(id, model, size?, populateItem)`: Creates an `OatAvatarGroup` (a clustered/overlapping set of avatars).
- `emptyState(id, title, message?)`: Creates an `OatEmptyState` placeholder.

## Layout & Navigation
- `accordion(id, model, exclusive?, populateItem)`: Creates an `OatAccordion`.
- `buttonGroup(id, model, populateItem)`: Creates an `OatButtonGroup` container for buttons.
- `breadcrumb(id, model, populateItem)`: Creates an `OatBreadcrumb` trail.
- `pagingNavigator(id, pageable)` / `ajaxPagingNavigator(id, pageable)`: `OatPagingNavigator` / `OatAjaxPagingNavigator`, Wicket's (Ajax)`PagingNavigator` rendered as Oat pagination for any `IPageable` (`DataView`, `PageableListView`, `DataTable`). Prefer these whenever there is an `IPageable`. For the Ajax one, the pageable or a parent needs `setOutputMarkupId(true)`.
- `pagination(id, model, populateItem)`: Creates an `OatPagination` control - hand-rolled page links for when there's no `IPageable`; you style the current page yourself.
- `dropdown(id, triggerLabel, model, populateItem)`: Creates an `OatDropdown` popover menu.
- `tabs(id, model, populateTab, populatePanel)`: Creates an `OatTabs` tab strip + panels.
- `dialog(id, triggerLabel, header)`: Creates an `OatDialog` modal, opened via its trigger.
- `OatAppLayout`: (Base Page) Provides sidebar and topnav.

## Form Components
Most form components are available via `Oat.Components.[type]Field` and produce an `Oat[Type]Field`, but a few don't follow the `Field` naming: `textArea` → `OatTextArea`, `dropdownChoice` → `OatDropdownChoice`, `checkBox` → `OatCheckBox`, `oatSwitch` → `OatSwitch`, `fileUpload` → `OatFileUpload`, `fileDropzone` → `OatFileDropzone`, `tagInput` → `OatTagInput`.

Every one takes an optional trailing hint-text model — but only when `label` is passed as an `IModel<String>`, not a plain `String`:
`Oat.Components.[type]Field(id, IModel<String> labelModel, model, hintModel?)`. There is no `(id, String label, model, hintModel)` overload; wrap the label in `Model.of("Label")` if you need a hint.

The label and the model can also be left out, exactly like plain Wicket form components: `[type]Field(id)` / `new Oat[Type]Field(id)` inherits the model from a parent `CompoundPropertyModel` by id, and `[type]Field(id, model)` / `new Oat[Type]Field(id, model)` keeps an explicit model. A missing label is looked up in the `.properties` files with the id as the key (falling back to the id). `dropdownChoice` mirrors `DropDownChoice`: `(id, choices, renderer?)` and `(id, model, choices, renderer?)`.

Behavior of the field panels:
- They render their own tag with a markup id, so `target.add(field)` (or `target.add(form)`) re-renders them, e.g. to show inline errors in an Ajax `onError`. Attributes on that tag in your markup (e.g. `class`) are kept.
- `field.add(...)` sends validators and `AjaxFormComponentUpdatingBehavior`s to the wrapped input; other behaviors apply to the panel's own tag. `getField()` returns the wrapped `FormComponent`.
- Fluent setters (`setRequired`, `setLabel`, `setPlaceholder`, `addValidator`, `add`, and `setMin`/`setMax` on number/range fields) return the concrete field type, so they chain freely.

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
- `feedbackPanel(id, filter?)`: `OatFeedbackPanel`, a Wicket `FeedbackPanel` rendering each `info()`/`success()`/`warn()`/`error()` message as an Oat alert of the matching variant. It adds itself to Ajax responses when it has messages to show or clear, so no `target.add(feedback)` is needed. By default it leaves out errors Oat form fields already show inline (`NotShownInlineFilter`); pass `null` to show everything.
- `dataTable(id, columns, dataProvider, rowsPerPage)`: Styled Wicket DataTable, paged with `OatPagingNavigator`. Use `setEmptyState(id -> emptyState(id, "No results"))` for a custom placeholder when it has no rows; the factory must use the id it is given.
- `progress(id, value, max?)`: Standard `<progress>` bar; `max` is an optional bound model.
- `meter(id, value)`: Simple `<meter>`.
- `meter(id, value, min, max, low, high, optimum)`: Advanced `<meter>` with full range/zone control.
- `spinner(id, size?)`: Loading indicator.
- `skeleton(id, shape?)`: Placeholder for loading content.

## Usage Example
```java
add(Oat.Components.button("myBtn", "Save", target -> {
    // Save logic
    Oat.toast(target, "Saved!", OatToastBehavior.Variant.SUCCESS);
}));
```
