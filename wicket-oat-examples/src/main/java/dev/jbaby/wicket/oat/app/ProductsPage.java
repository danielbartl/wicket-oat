package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.components.OatCrud;
import dev.jbaby.wicket.oat.components.OatDescriptionList;
import dev.jbaby.wicket.oat.components.form.OatDropdownChoice;
import dev.jbaby.wicket.oat.components.form.OatMoneyField;
import dev.jbaby.wicket.oat.components.form.OatNumberField;
import dev.jbaby.wicket.oat.components.form.OatSearchField;
import dev.jbaby.wicket.oat.components.form.OatTextArea;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import dev.jbaby.wicket.oat.components.table.OatEditableColumn;
import dev.jbaby.wicket.oat.components.table.OatNumberColumn;
import dev.jbaby.wicket.oat.components.table.OatRowDetailsColumn;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.RangeValidator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An OatCrud of products: New, Edit and Delete with an edit dialog, plus the data
 * table extras - expandable row details, a stock column edited in place, a column
 * chooser and a CSV export.
 */
public class ProductsPage extends BasePage {

    public static final List<String> CATEGORIES = List.of("Hardware", "Software", "Services", "Accessories");

    /** A product, equal by id like an entity. Getters and setters for the CompoundPropertyModel. */
    public static final class Product implements Serializable {
        private long id;
        private String name;
        private String category;
        private BigDecimal price;
        private Integer stock;
        private String description;

        public Product() {
        }

        Product(long id, String name, String category, String price, int stock, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = new BigDecimal(price);
            this.stock = stock;
            this.description = description;
        }

        public long getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public boolean equals(Object other) {
            return other instanceof Product product && product.id == id;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }
    }

    private final List<Product> products = new ArrayList<>(List.of(
            new Product(1, "Laptop 14\"", "Hardware", "1299.00", 12, "14-inch business laptop, 32 GB RAM, 1 TB SSD."),
            new Product(2, "Docking station", "Accessories", "189.00", 40, "USB-C dock with two displays, Ethernet and 100 W charging."),
            new Product(3, "Office suite, yearly", "Software", "99.00", 250, "Word processing, spreadsheets and mail for one user."),
            new Product(4, "On-site setup", "Services", "480.00", 6, "A technician sets up and hands over up to five devices."),
            new Product(5, "Monitor 27\"", "Hardware", "349.00", 18, "27-inch QHD monitor with a height-adjustable stand."),
            new Product(6, "Wireless keyboard", "Accessories", "59.00", 75, "Quiet keys, three paired devices, USB-C charging."),
            new Product(7, "Backup, 1 TB", "Services", "12.00", 500, "Encrypted off-site backup, billed monthly."),
            new Product(8, "Antivirus, yearly", "Software", "39.00", 300, "Protection for up to three devices.")));
    private final AtomicLong nextId = new AtomicLong(100);
    private final IModel<String> search = new Model<>();

    public ProductsPage() {
        add(Oat.Behaviors.feedbackToasts());
        Currency eur = Currency.getInstance("EUR");

        List<IColumn<Product, String>> columns = new ArrayList<>();
        columns.add(new OatRowDetailsColumn<>((id, product) -> {
            Fragment details = new Fragment(id, "detailsFragment", this);
            details.add(new OatDescriptionList("list")
                    .addItem("Description", Model.of(product.getObject().getDescription()))
                    .addItem("Article no.", Model.of(String.format("P-%05d", product.getObject().getId()))));
            return details;
        }));
        columns.add(new LambdaColumn<>(Model.of("Name"), "name", Product::getName));
        columns.add(new LambdaColumn<>(Model.of("Category"), "category", Product::getCategory));
        columns.add(new OatNumberColumn<Product, String>(Model.of("Price"), "price", Product::getPrice).setCurrency(eur));
        columns.add(new OatEditableColumn<Product, String, Integer>(Model.of("Stock"), "stock", Product::getStock, Product::setStock)
                .setType(Integer.class)
                .setRequired(true)
                .addValidator(RangeValidator.minimum(0))
                .onSave((target, product) -> success("Stock of " + product.getName() + " is now " + product.getStock() + ".")));

        OatCrud<Product, String> crud = new OatCrud<>("products", columns, new ProductProvider(), 5)
                .setEditor(ProductFields::new)
                .setNewItem(Product::new)
                .setTitle(Product::getName)
                .onSave((target, product) -> {
                    if (product.getId() == 0) {
                        product.id = nextId.getAndIncrement();
                        products.add(product);
                    }
                    success(product.getName() + " saved.");
                })
                .onDelete((target, product) -> {
                    products.remove(product);
                    success(product.getName() + " deleted.");
                });
        crud.setSearch(id -> {
            Fragment toolbar = new Fragment(id, "searchFragment", this);
            Form<Void> form = new Form<>("form");
            toolbar.add(form);
            form.add(new OatSearchField("search", "Search products", search)
                    .setPlaceholder(Model.of("Name or category"))
                    .add(new AjaxFormComponentUpdatingBehavior("input") {
                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);
                            attributes.setThrottlingSettings(new ThrottlingSettings(Duration.ofMillis(300)));
                        }

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            crud.getDataTable().getTable().setCurrentPage(0);
                            target.add(crud.getDataTable());
                        }
                    }));
            return toolbar;
        });
        crud.getDataTable().setColumnChooser(true).setCsvExport("products.csv");
        add(crud);
    }

    /** The edit dialog's fields, found by property name through the CompoundPropertyModel. */
    public static final class ProductFields extends Panel {
        public ProductFields(String id, IModel<Product> product) {
            super(id, product);
            add(new OatTextField<String>("name", "Name", null).setRequired(true));
            add(new OatDropdownChoice<String>("category", Model.ofList(CATEGORIES)).setLabel(Model.of("Category")).setRequired(true));
            add(new OatMoneyField("price", "Price", null).setCurrency(Currency.getInstance("EUR")).setRequired(true));
            add(new OatNumberField<Integer>("stock", "Stock", null).setRequired(true));
            add(new OatTextArea<String>("description", "Description", null));
        }
    }

    private final class ProductProvider extends SortableDataProvider<Product, String> {
        ProductProvider() {
            setSort("name", org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder.ASCENDING);
        }

        private List<Product> filtered() {
            String text = search.getObject() == null ? "" : search.getObject().strip().toLowerCase(Locale.ROOT);
            return products.stream().filter(p -> text.isEmpty() || p.getName().toLowerCase(Locale.ROOT).contains(text)
                    || p.getCategory().toLowerCase(Locale.ROOT).contains(text)).toList();
        }

        @Override
        public Iterator<? extends Product> iterator(long first, long count) {
            Comparator<Product> order = switch (getSort() == null ? "name" : getSort().getProperty()) {
                case "category" -> Comparator.comparing(Product::getCategory);
                case "price" -> Comparator.comparing(Product::getPrice);
                case "stock" -> Comparator.comparing(Product::getStock);
                default -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            };
            if (getSort() != null && !getSort().isAscending()) {
                order = order.reversed();
            }
            return filtered().stream().sorted(order).skip(first).limit(count).iterator();
        }

        @Override
        public long size() {
            return filtered().size();
        }

        @Override
        public IModel<Product> model(Product product) {
            return Model.of(product);
        }
    }
}
