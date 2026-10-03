# Wicket Oat Components

Use `Oat.Components` for a "Fast Path" to build UI with encapsulated markup.

## Variants
Colors are a single `dev.jbaby.wicket.oat.OatVariant` enum everywhere (alerts, badges, buttons, toasts): `DEFAULT`, `SECONDARY`, `SUCCESS`, `WARNING`, `DANGER`. Buttons style `SECONDARY`/`DANGER`; badges all; alerts and toasts `SUCCESS`/`WARNING`/`DANGER`. There are no per-component `Variant` enums.

## General
- `alert(id, message?, variant?)`: Creates an `OatAlert`. `message`/`variant` can each be a plain value, an `IModel`, or omitted.
- `badge(id, label, variant?)`: Creates an `OatBadge`. `label` can be a `String` or an `IModel<?>`.
- `button(id, label, onClick)`: Creates an `OatButton` with an Ajax click handler, on an `<a>` or `<button>`. The label is rendered as the tag's text, so the markup is just `<a wicket:id="save"></a>` (no inner `<span wicket:id="label">` - that now fails). `new OatButton(id) { onClick... }` without a label renders the markup body instead, e.g. an icon with `setIcon(true)`.
- `submitButton(id, label, onSubmit, onError?)`: Creates an `OatSubmitButton`, an Oat-styled `AjaxButton` that submits its form: `onSubmit` runs when the form is valid, `onError` when it isn't (e.g. `target -> target.add(form)` to show inline errors). Markup: `<button wicket:id="save"></button>`. Subclass `OatSubmitButton` directly to override `onSubmit`/`onError` instead.
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
- `tabbedPanel(id, tabs)`: Creates an `OatTabbedPanel`, Wicket's `AjaxTabbedPanel` with Oat styling. Tabs are `ITab`s - build them with `Oat.tab("Title", SomePanel::new)` (the factory receives the panel id). Only the selected tab's panel is created, and switching swaps it over Ajax; `new OatTabbedPanel<>(id, tabs, selectedIndexModel)` keeps the selection in your own model. Prefer this for panels that are expensive or need server state.
- `tabs(id, model, populateTab, populatePanel)`: Creates an `OatTabs` tab strip + panels, all rendered up front and switched in the browser by the `ot-tabs` web component.
- `dialog(id, header)` / `dialog(id, triggerLabel, header)`: Creates an `OatDialog` modal on the native `<dialog>` element. Open and close it from any Ajax handler with `dialog.open(target)` / `dialog.close(target)` (`open` re-renders it first); with a trigger label it also renders a button that opens it natively. Set the body with `setBody(component)` - the component must use the id `OatDialog.BODY_ID`. The body sits in the dialog's own form, so fields in it are validated when Confirm is clicked: on a validation error the dialog stays open and shows them inline; otherwise the overridable `onConfirm(target)` runs and the dialog closes unless an error was reported in it there (e.g. `field.getField().error("Taken")`). `setConfirmLabel`, `setCancelLabel` and `setConfirmVariant(OatVariant.DANGER)` customize the buttons; the default labels come from the `OatDialog.confirm` / `OatDialog.cancel` resource keys.
- `OatAppLayout`: (Base Page) Provides sidebar and topnav.

## Form Components
Most form components are available via `Oat.Components.[type]Field` and produce an `Oat[Type]Field`, but a few don't follow the `Field` naming: `textArea` → `OatTextArea`, `dropdownChoice` → `OatDropdownChoice`, `checkBox` → `OatCheckBox`, `oatSwitch` → `OatSwitch`, `fileUpload` → `OatFileUpload`, `fileDropzone` → `OatFileDropzone`, `tagInput` → `OatTagInput`.

Every one takes an optional trailing hint-text model — but only when `label` is passed as an `IModel<String>`, not a plain `String`:
`Oat.Components.[type]Field(id, IModel<String> labelModel, model, hintModel?)`. There is no `(id, String label, model, hintModel)` overload; wrap the label in `Model.of("Label")` if you need a hint.

The label and the model can also be left out, exactly like plain Wicket form components: `[type]Field(id)` / `new Oat[Type]Field(id)` inherits the model from a parent `CompoundPropertyModel` by id, and `[type]Field(id, model)` / `new Oat[Type]Field(id, model)` keeps an explicit model. A missing label is looked up in the `.properties` files with the id as the key (falling back to the id). `dropdownChoice` mirrors `DropDownChoice`: `(id, choices, renderer?)` and `(id, model, choices, renderer?)`.

Behavior of the field panels:
- Required fields get `aria-required="true"` and an asterisk after their label (via `data-required` on the field container). The native `required` attribute is deliberately not set, since the browser would block the submit with its own message before Wicket could show its inline error.
- They render their own tag with a markup id, so `target.add(field)` (or `target.add(form)`) re-renders them, e.g. to show inline errors in an Ajax `onError`. Attributes on that tag in your markup (e.g. `class`) are kept.
- `field.add(...)` sends validators and `AjaxFormComponentUpdatingBehavior`s to the wrapped input; other behaviors apply to the panel's own tag. `getField()` returns the wrapped `FormComponent`.
- Fluent setters (`setRequired`, `setLabel`, `setPlaceholder`, `addValidator`, `add`, and `setMin`/`setMax` on number/range fields) return the concrete field type, so they chain freely.

- `textField(id, label, model)`
- `passwordField(id, label, model)`
- `emailField(id, label, model)`
- `numberField(id, label, model)`
- `textArea(id, label, model)`
- `dropdownChoice(id, label, model, choices, renderer?)`
- `radioChoice(id, label, model, choices, renderer?)`: `OatRadioChoice`, radio buttons in a `<fieldset>` with a `<legend>`; stacked, or in a row with `setInline(true)`. Same constructor/factory shapes as `dropdownChoice`.
- `checkBoxMultipleChoice(id, label, model, choices, renderer?)`: `OatCheckBoxMultipleChoice`, a checkbox group bound to an `IModel<? extends Collection<T>>` of the selected choices (updated in place); `setInline(true)` for a row.
- `listMultipleChoice(id, label, model, choices, renderer?)`: `OatListMultipleChoice`, a `<select multiple>` bound to a collection of the selected choices; `setMaxRows(n)` sets its height.
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
- `tagInput(id, label, model)`: Tag/chip input bound to an `IModel<List<String>>`. Tags are trimmed and de-duplicated; no tags is an empty list (and fails `setRequired(true)`). As in Oat's widget, a comma separates tags, so a tag can't contain one.

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
    Oat.toast(target, "Saved!", OatVariant.SUCCESS);
}));
```
