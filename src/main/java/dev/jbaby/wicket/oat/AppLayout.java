package dev.jbaby.wicket.oat;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jspecify.annotations.NonNull;

public abstract class AppLayout extends WebPage {

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // Shared assets for all pages using this layout
        response.render(CssHeaderItem.forUrl("https://unpkg.com/@knadh/oat/oat.min.css"));
        response.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/@knadh/oat/oat.min.js"));
    }

    public AppLayout() {
        // Put shared components here later if needed (nav, user menu, feedback, etc.)
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        appTitle();
        appName();

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
}