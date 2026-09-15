# Wicket Oat

Wicket Oat is a modern, lightweight, and themeable UI component library for [Apache Wicket](https://wicket.apache.org/). It provides a set of high-quality components and behaviors built on top of a sleek, modern design system.

## Features

- **Fluent API:** Easily create components and behaviors using the `Oat` factory class.
- **Modern Design:** Beautifully designed components like Buttons, Cards, Modals, and more.
- **Themeable:** Built-in support for multiple themes (Light, Dark, Midnight, Nord, etc.) that can be switched dynamically.
- **Wicket 10+:** Fully compatible with Apache Wicket 10 and Java 25.
- **Spring Boot Integration:** Seamless integration with Spring Boot applications.
- **Comprehensive Component Set:** Includes everything from basic buttons to complex data tables.

## Quick Start

### 1. Add Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>wicket-oat-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Install Wicket Oat

In your Wicket `WebApplication` class, call `WicketOats.install(this)` in the `init()` method:

```java
public class MyWicketApplication extends WebApplication {
    @Override
    protected void init() {
        super.init();
        
        // Install Wicket Oat components and styles
        WicketOats.install(this);
    }
}
```

### 3. Use Components

Use the `Oat` factory class to create components in your pages:

```java
// Create a button with an Ajax click handler via Components factory
add(Oat.Components.button("myButton", "Click Me", target -> {
    Oat.toast(target, "Hello from Wicket Oat!", OatToastBehavior.Variant.SUCCESS);
}));

// Create an alert via Components factory
add(Oat.Components.alert("myAlert", AlertBehavior.Variant.INFO)
    .setBody(Model.of("This is an informative alert.")));

// Create a badge via Components factory
add(Oat.Components.badge("myBadge", "New", BadgeBehavior.Variant.PRIMARY));
```

## Available Components

- **General:** `OatButton`, `OatBadge`, `OatAvatar`, `OatAvatarGroup`, `OatAlert`, `OatCard`, `OatEmptyState`
- **Navigation/Layout:** `OatAppLayout`, `OatAccordion`, `OatButtonGroup`, `OatBreadcrumb`, `OatPagination`
- **Overlays:** `OatDialog`, `OatDropdown`, `OatTabs`
- **Data Display:** `OatDataTable`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`
- **Forms:** `OatTextField`, `OatCheckBox`, `OatDropdownChoice`, `OatTextArea`, `OatSwitch`, `OatTagInput`, `OatFileUpload`, `OatFileDropzone`, and more specialized HTML5 fields.

## Architecture: Components vs. Behaviors

Wicket Oat provides both **Components** and **Behaviors** for almost every UI element. This dual approach gives you maximum flexibility depending on your needs.

### When to use Components (`Oat.Components`)
Use a component when you want a self-contained UI widget and don't want to worry about the underlying HTML structure.
- **Pros:** Easiest to use; encapsulates markup logic; handles internal structure (like headers/footers in a Card).
- **Example:** `add(Oat.Components.alert("id", Variant.INFO))`
- **Markup Requirement:** Requires a simple tag like `<div wicket:id="id"></div>`.

### When to use Behaviors (`Oat.Behaviors`)
Use a behavior when you want to "Oat-ify" an existing Wicket component. This is the power of **Composition over Inheritance**.
- **Pros:** Highly flexible; can be applied to *any* component (Links, Labels, Containers); keeps your component hierarchy clean.
- **Example:** `myWicketLink.add(Oat.Behaviors.button())` — This turns a standard Wicket `Link` into a styled Oat button without changing the Java class of the link.
- **Markup Requirement:** You provide the markup (e.g., an `<a>` or `<button>` tag) and the behavior ensures the correct CSS classes and attributes are applied.

### Summary Comparison

| Feature | Component | Behavior |
| :--- | :--- | :--- |
| **Philosophy** | "Give me an Alert" | "Make this thing look like an Alert" |
| **Java Usage** | `new OatAlert(...)` | `anyComponent.add(new AlertBehavior())` |
| **Markup** | Handled by Oat | Handled by You |
| **Best For** | Quick UI building | Customizing existing Wicket logic |

## Usage Strategies

Depending on your project's complexity, you can choose between two development styles:

### 1. The Fast Path (Factory Methods)
Use `Oat.Components` and `Oat.Behaviors` for standard UI assembly. This is the fastest way to build a modern Wicket application.
```java
// Fast and readable for 80% of use cases
add(Oat.Components.button("id", "Click Me", target -> ...));
```

### 2. The Power Path (Inheritance & Behaviors)
If you need deep customization or want to follow traditional Wicket patterns, you are not "trapped" by the factory. 

**Subclassing:**
Every component has a public constructor. You can extend `OatButton`, `OatAlert`, etc., just like any other Wicket component.
```java
public class MyBusinessButton extends OatButton {
   // Your complex business logic here
}
```

**Composition (Recommended):**
Oat's behaviors are the most powerful way to customize. You can keep your own class hierarchy and "compose" the UI look.
```java
public class MyComplexActionLink extends AjaxLink<Void> {
    public MyComplexActionLink(String id) {
        super(id);
        add(Oat.Behaviors.button().setVariant(Variant.PRIMARY));
    }
}
```

## Theming

Wicket Oat comes with several built-in themes. You can apply a theme to your session or specific components:

```java
// Set a global theme in your session (if using OatSession)
OatSession.get().setTheme(OatTheme.DARK);

// Or add the theme behavior to a component/page via Behaviors factory
add(Oat.Behaviors.theme().setTheme(OatTheme.NORD));
```

### Built-in Themes:
`DARK`, `LIGHT`, `MIDNIGHT`, `NORD`, `EVERFOREST`, `TOKYO_NIGHT`, `ROSE_PINE_DAWN`, `ROYAL`, `CLAY`, `CATPPUCCIN_MOCHA`, `CATPPUCCIN_LATTE`, `MATERIAL`, `DAISY`, `ULTRAVIOLET`, `HALLOWEEN`, `XMAS`, `WIREFRAME`.

## Running the Examples

The project includes an examples module `wicket-oat-examples`. To run it:

1. Clone the repository.
2. Run the application using Maven:
   ```bash
   ./mvnw spring-boot:run -pl wicket-oat-examples
   ```
3. Access the demo at `http://localhost:8080/`.

## AI-Powered Development

Wicket Oat comes with a dedicated **Gemini CLI Skill** to help you build UI faster using AI. The skill provides the agent with deep knowledge of the Oat API, component library, and theming system.

### Installing the Skill
If you use the [Gemini CLI](https://github.com/google/gemini-cli), you can install the skill directly from this repository:

```bash
# Install the skill locally in your project
gemini skills install ./wicket-oat.skill --scope workspace

# Reload skills to enable it
/skills reload
```

### How to Use
Once installed, the AI agent will automatically understand instructions like:
- "Add a primary Oat button to this page that shows a success toast on click."
- "Turn this standard Wicket Label into an Oat success badge."
- "Create a registration form using Oat components."

## Requirements

- Java 25 or higher
- Apache Wicket 10.8.0+
- Maven

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
