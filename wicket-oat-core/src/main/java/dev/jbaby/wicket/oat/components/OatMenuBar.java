package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.behaviors.ButtonBehavior;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A row of commands for a page or a document - File, Edit, Export - each a button, a
 * link, or a menu of further commands:
 * <pre>{@code
 * OatMenuBar bar = new OatMenuBar("commands");
 * bar.addAction("Save", target -> save());
 * OatMenuBar.Menu export = bar.addMenu("Export");
 * export.addAction("CSV", target -> exportCsv());
 * export.addAction("PDF", target -> exportPdf());
 * bar.addLink("Reports", ReportsPage.class);
 * add(bar);
 * }</pre>
 * on {@code <nav wicket:id="commands" aria-label="Invoice"></nav>}. Actions and menu
 * entries run over Ajax; a menu closes after an entry is chosen. Menus have one level;
 * for deeper structures, group entries or move them to a page. The buttons are ghost
 * buttons by default ({@link #setStyle}), so the bar reads as a toolbar.
 */
public class OatMenuBar extends Panel {

    private final RepeatingView items = new RepeatingView("items");
    private ButtonBehavior.Style style = ButtonBehavior.Style.GHOST;

    public OatMenuBar(String id) {
        super(id);
        add(items);
    }

    /** A button running {@code onClick} over Ajax. */
    public OatMenuBar addAction(String label, SerializableConsumer<AjaxRequestTarget> onClick) {
        return addAction(Model.of(label), onClick);
    }

    /** A button running {@code onClick} over Ajax. */
    public OatMenuBar addAction(IModel<String> label, SerializableConsumer<AjaxRequestTarget> onClick) {
        Fragment fragment = new Fragment(items.newChildId(), "actionFragment", this);
        AjaxLink<Void> button = new AjaxLink<>("button") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onClick.accept(target);
            }
        };
        button.setBody(label);
        button.add(newButtonStyle());
        fragment.add(button);
        items.add(fragment);
        return this;
    }

    /** A link to a page. */
    public OatMenuBar addLink(String label, Class<? extends Page> page) {
        return addLink(Model.of(label), page, null);
    }

    /** A link to a page, with parameters. */
    public OatMenuBar addLink(IModel<String> label, Class<? extends Page> page, PageParameters parameters) {
        Fragment fragment = new Fragment(items.newChildId(), "linkFragment", this);
        BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", page, parameters);
        link.setBody(label);
        link.add(newButtonStyle());
        link.add(AttributeModifier.replace("aria-current", (IModel<String>) () ->
                getPage().getClass().equals(page) ? "page" : null));
        fragment.add(link);
        items.add(fragment);
        return this;
    }

    /** A button opening a menu; add its entries to the returned {@link Menu}. */
    public Menu addMenu(String label) {
        return addMenu(Model.of(label));
    }

    /** A button opening a menu; add its entries to the returned {@link Menu}. */
    public Menu addMenu(IModel<String> label) {
        Menu menu = new Menu();
        Fragment fragment = new Fragment(items.newChildId(), "menuFragment", this);
        OatDropdown<Menu.Entry> dropdown = new OatDropdown<>("dropdown", label, Model.ofList(menu.entries)) {
            @Override
            protected void populateItem(ListItem<Menu.Entry> item) {
                Menu.Entry entry = item.getModelObject();
                item.add(new Label("label", entry.label()));
                if (entry.variant() != null && entry.variant().getValue() != null) {
                    item.add(AttributeModifier.replace("data-variant", entry.variant().getValue()));
                }
                item.add(AjaxEventBehavior.onEvent("click", target -> {
                    entry.onClick().accept(target);
                    close(target);
                }));
            }
        };
        dropdown.getTrigger().add(AttributeModifier.remove("class"), newButtonStyle());
        fragment.add(dropdown);
        items.add(fragment);
        return menu;
    }

    /** The buttons' style: {@code GHOST} by default, {@code OUTLINE} or {@code null} for solid ones. */
    public OatMenuBar setStyle(ButtonBehavior.Style style) {
        this.style = style;
        return this;
    }

    private ButtonBehavior newButtonStyle() {
        return new ButtonBehavior() {
            @Override
            public void onComponentTag(org.apache.wicket.Component component, org.apache.wicket.markup.ComponentTag tag) {
                setStyle(style);
                super.onComponentTag(component, tag);
            }
        }.setSize(ButtonBehavior.Size.SMALL);
    }

    /** A menu in the bar. */
    public static final class Menu implements Serializable {

        private final List<Entry> entries = new ArrayList<>();

        Menu() {
        }

        public Menu addAction(String label, SerializableConsumer<AjaxRequestTarget> onClick) {
            return addAction(Model.of(label), OatVariant.DEFAULT, onClick);
        }

        /** An entry, e.g. with {@code DANGER} for a destructive one, shown in red. */
        public Menu addAction(IModel<String> label, OatVariant variant, SerializableConsumer<AjaxRequestTarget> onClick) {
            entries.add(new Entry(label, variant, onClick));
            return this;
        }

        private record Entry(IModel<String> label, OatVariant variant, SerializableConsumer<AjaxRequestTarget> onClick)
                implements Serializable {
        }
    }
}
