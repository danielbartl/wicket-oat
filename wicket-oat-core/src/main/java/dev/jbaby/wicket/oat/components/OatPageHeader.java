package dev.jbaby.wicket.oat.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;

/**
 * The top of a page: a breadcrumb trail, the page's title and a subtitle on one side,
 * and its main actions on the other - wrapping below the title on narrow screens.
 * <p>
 * It is a Wicket {@link Border} on a {@code <header>} tag; whatever you put inside the
 * tag becomes the actions, so they are plain markup and components:
 * <pre>{@code
 * <header wicket:id="pageHeader">
 *     <a wicket:id="export" class="button outline">Export</a>
 *     <button wicket:id="newInvoice"></button>
 * </header>
 * }</pre>
 * <pre>{@code
 * OatPageHeader header = new OatPageHeader("pageHeader", Model.of("Invoices"))
 *         .setSubtitle(Model.of("23 open, 4 overdue"))
 *         .setBreadcrumb(List.of(MenuItem.of("Home", HomePage.class), MenuItem.of("Sales", SalesPage.class)));
 * header.add(new BookmarkablePageLink<>("export", ExportPage.class));
 * header.add(Oat.Components.button("newInvoice", "New invoice", target -> ...));
 * add(header);
 * }</pre>
 * The breadcrumb links to the given pages and ends with the title, marked as the
 * current page; its {@code <nav>} is named by the {@code OatPageHeader.breadcrumb}
 * resource ("Breadcrumb").
 */
public class OatPageHeader extends Border {

    private final Label title;
    private final Label subtitle;
    private IModel<List<MenuItem>> breadcrumb = Model.ofList(List.of());

    /** Title looked up by {@code id} in the {@code .properties} files (falling back to the id). */
    public OatPageHeader(String id) {
        this(id, (IModel<String>) null);
    }

    public OatPageHeader(String id, String title) {
        this(id, Model.of(title));
    }

    /**
     * @param id the component id, also the resource key for a missing title
     * @param title the title, or {@code null} to look it up by {@code id}
     */
    public OatPageHeader(String id, IModel<String> title) {
        super(id);
        this.title = new Label("title", title != null ? title : new StringResourceModel(id, this).setDefaultValue(id));
        addToBorder(this.title);

        // An explicit (empty) model, so the label never inherits one from a parent
        // CompoundPropertyModel by its id
        subtitle = new Label("subtitle", new Model<String>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDefaultModelObject() != null);
            }
        };
        addToBorder(subtitle);

        WebMarkupContainer nav = new WebMarkupContainer("breadcrumb") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!breadcrumb.getObject().isEmpty());
            }
        };
        addToBorder(nav);
        nav.add(new ListView<>("crumbs", (IModel<List<MenuItem>>) () -> breadcrumb.getObject()) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                MenuItem crumb = item.getModelObject();
                BookmarkablePageLink<?> link = new BookmarkablePageLink<>("link", crumb.pageClass(), crumb.parameters());
                link.add(new Label("label", crumb.label()));
                item.add(link);
            }
        });
        // The trail ends with this page, by its title
        nav.add(new Label("current", (IModel<String>) () -> getTitle().getObject()));
    }

    /** A line under the title, e.g. a summary of the page's data; {@code null} hides it. */
    public OatPageHeader setSubtitle(String subtitle) {
        return setSubtitle(Model.of(subtitle));
    }

    /** A line under the title, e.g. a summary of the page's data; a {@code null} model or object hides it. */
    public OatPageHeader setSubtitle(IModel<String> subtitle) {
        this.subtitle.setDefaultModel(subtitle != null ? subtitle : new Model<String>());
        return this;
    }

    /**
     * The pages above this one, from the top, e.g. Home and Sales for an invoice page.
     * Each is a {@link MenuItem} link ({@code MenuItem.of(label, PageClass)}); the trail
     * ends with this page's title.
     */
    public OatPageHeader setBreadcrumb(List<MenuItem> crumbs) {
        return setBreadcrumb(Model.ofList(crumbs));
    }

    /** The pages above this one, from the top, read on every render. */
    public OatPageHeader setBreadcrumb(IModel<List<MenuItem>> crumbs) {
        for (MenuItem crumb : crumbs.getObject()) {
            if (crumb.isGroup()) {
                throw new IllegalArgumentException("A breadcrumb links to pages, not groups: " + crumb.label().getObject());
            }
        }
        this.breadcrumb = crumbs;
        return this;
    }

    /** The title model, as given or looked up by id. */
    @SuppressWarnings("unchecked")
    public IModel<String> getTitle() {
        return (IModel<String>) title.getDefaultModel();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        checkComponentTag(tag, "header");
        super.onComponentTag(tag);
        tag.append("class", "oat-page-header", " ");
    }

    @Override
    protected void onDetach() {
        breadcrumb.detach();
        super.onDetach();
    }
}
