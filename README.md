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
// Create a button with an Ajax click handler
add(Oat.button("myButton", "Click Me", target -> {
    Oat.toast(target, "Hello from Wicket Oat!", OatToastBehavior.Variant.SUCCESS);
}));

// Create an alert
add(Oat.alert("myAlert", AlertBehavior.Variant.INFO)
    .setBody(Model.of("This is an informative alert.")));

// Create a badge
add(Oat.badge("myBadge", "New", BadgeBehavior.Variant.PRIMARY));
```

## Available Components

- **General:** `OatButton`, `OatBadge`, `OatAvatar`, `OatAlert`, `OatCard`, `OatEmptyState`
- **Navigation/Layout:** `OatAppLayout`, `OatAccordion`, `OatButtonGroup`
- **Data Display:** `OatDataTable`, `OatProgress`, `OatMeter`, `OatSkeleton`, `OatSpinner`
- **Forms:** `OatTextField`, `OatCheckBox`, `OatDropdownChoice`, `OatTextArea`, `OatSwitch`, and more specialized HTML5 fields.

## Theming

Wicket Oat comes with several built-in themes. You can apply a theme to your session or specific components:

```java
// Set a global theme in your session (if using OatSession)
OatSession.get().setTheme(OatTheme.DARK);

// Or add the theme behavior to a component/page
add(Oat.theme().setTheme(OatTheme.NORD));
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

## Requirements

- Java 25 or higher
- Apache Wicket 10.8.0+
- Maven

## License

[Add License Information Here]
