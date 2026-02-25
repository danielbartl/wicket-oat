package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class OatAppLayout extends WebPage {

    protected WebMarkupContainer footer;
    private WebMarkupContainer sidebar;

    public OatAppLayout() {
        TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");
        html.add(new OatThemeBehavior());
        add(html);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        appTitle();
        appName();
        sidebar();
        sidebarMenu();
        footer();

    }

    protected void appTitle() {
        add(new Label("appTitle", appTitleModel()));
    }

    protected @NonNull IModel<?> appTitleModel() {
        return Model.of("Wicket Oat Application");
    }

    protected void appName() {
        add(new Label("appName", appNameModel()));
    }

    protected @NonNull IModel<?> appNameModel() {
        return Model.of("Wicket Oat Application");
    }

    protected void sidebar() {
        sidebar = new WebMarkupContainer("sidebar");
        add(sidebar);
    }

    /**
     * Child pages/apps customize the menu by overriding {@link #sidebarMenuItemsModel()}.
     * AppLayout owns the rendering (UL/LI) via a ListView.
     */
    protected void sidebarMenu() {
        sidebar.add(new ListView<>("menuItems", sidebarMenuItemsModel()) {
            @Override
            protected void populateItem(ListItem<MenuItem> item) {

                MenuItem mi = item.getModelObject();

                BookmarkablePageLink<?> link = new BookmarkablePageLink<>("link", mi.pageClass());
                link.add(new Label("label", mi.label()));
                item.add(link);

                boolean isCurrent = getPage().getClass().equals(mi.pageClass());

                if (isCurrent) {
                    // Accessibility-friendly "current page" marker
                    link.add(AttributeModifier.replace("aria-current", "page"));

                    // Optional: allow custom styling via CSS on the <li>
                    item.add(new AttributeAppender("class", Model.of("active"), " "));

                } else {

                    // Ensure non-current items don't keep aria-current from reuse
                    link.add(AttributeModifier.remove("aria-current"));

                }

            }
        });
    }

    /**
     * Override in child pages (or better: in an app-specific base page) to supply menu items.
     */
    protected @NonNull IModel<List<MenuItem>> sidebarMenuItemsModel() {
        return Model.ofList(List.of());
    }

    protected void footer() {
        footer = new WebMarkupContainer("footer");
        sidebar.add(footer);

        footer.add(new Link<Void>("lightTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.LIGHT);
            }
        });
        footer.add(new Link<Void>("darkTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.DARK);
            }
        });
        footer.add(new Link<Void>("midnightTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.MIDNIGHT);
            }
        });
        footer.add(new Link<Void>("nordTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.NORD);
            }
        });
        footer.add(new Link<Void>("everforestTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.EVERFOREST);
            }
        });
        footer.add(new Link<Void>("tokyoNightTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.TOKYO_NIGHT);
            }
        });
        footer.add(new Link<Void>("rosePineDawnTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.ROSE_PINE_DAWN);
            }
        });
        footer.add(new Link<Void>("royalTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.ROYAL);
            }
        });
        footer.add(new Link<Void>("clayTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.CLAY);
            }
        });
        footer.add(new Link<Void>("catppuccinMochaTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.CATPPUCCIN_MOCHA);
            }
        });
        footer.add(new Link<Void>("catppuccinLatteTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.CATPPUCCIN_LATTE);
            }
        });
        footer.add(new Link<Void>("materialTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.MATERIAL);
            }
        });
        footer.add(new Link<Void>("daisyTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.DAISY);
            }
        });
        footer.add(new Link<Void>("ultravioletTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.ULTRAVIOLET);
            }
        });
        footer.add(new Link<Void>("halloweenTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.HALLOWEEN);
            }
        });
        footer.add(new Link<Void>("xmasTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.XMAS);
            }
        });
        footer.add(new Link<Void>("wireframeTheme") {
            @Override
            public void onClick() {
                OatSession.get().setTheme(OatTheme.WIREFRAME);
            }
        });
    }
}
