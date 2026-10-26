package dev.jbaby.wicket.oat.app.showcase;

import dev.jbaby.wicket.oat.OatDensity;
import dev.jbaby.wicket.oat.OatSettings;
import dev.jbaby.wicket.oat.OatTheme;
import dev.jbaby.wicket.oat.behaviors.OatDensityBehavior;
import dev.jbaby.wicket.oat.behaviors.OatThemeBehavior;
import dev.jbaby.wicket.oat.components.OatHtml;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Every scenario on one bare page, with nothing but the library's CSS and the cards': what
 * the website's previews are made from, and what the browser checks run over.
 * {@code ?theme=nord} shows it in one theme without storing the choice,
 * {@code ?density=compact} compact, and {@code ?scenario=button,badge} only those scenarios.
 */
public class ShowcasePage extends WebPage {

    private static final CssResourceReference SHOWCASE_CSS = new CssResourceReference(ShowcasePage.class, "showcase.css");

    public ShowcasePage(PageParameters parameters) {
        super(parameters);

        OatTheme theme = OatSettings.get().findTheme(parameters.get("theme").toOptionalString());
        OatDensity density = Arrays.stream(OatDensity.values())
                .filter(d -> d.name().equalsIgnoreCase(parameters.get("density").toString(""))).findFirst().orElse(null);
        add(new OatHtml("html").add(new OatThemeBehavior(theme), new OatDensityBehavior(density)));

        Set<String> only = Set.of(parameters.get("scenario").toString("").split(","));
        List<Scenarios.Group> groups = Scenarios.ALL.stream()
                .map(group -> new Scenarios.Group(group.name(), group.scenarios().stream()
                        .filter(scenario -> only.contains("") || only.contains(scenario.id())).toList()))
                .filter(group -> !group.scenarios().isEmpty())
                .toList();

        add(new ListView<>("groups", groups) {
            @Override
            protected void populateItem(ListItem<Scenarios.Group> group) {
                group.add(new Label("name", group.getModelObject().name()));
                group.add(new ListView<>("scenarios", group.getModelObject().scenarios()) {
                    @Override
                    protected void populateItem(ListItem<Scenario> item) {
                        Scenario scenario = item.getModelObject();
                        item.add(AttributeModifier.replace("data-scenario", scenario.id()));
                        if (scenario.wide()) {
                            item.add(AttributeModifier.append("class", "wide"));
                        }
                        ScenarioPanel preview = new ScenarioPanel("preview", scenario);
                        preview.add(AttributeModifier.replace("data-preview", scenario.id()));
                        if (scenario.block()) {
                            preview.add(AttributeModifier.append("class", "block"));
                        }
                        item.add(preview);
                        item.add(new Label("title", scenario.title()));
                        item.add(new Label("code", scenario.code()));
                    }
                });
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(SHOWCASE_CSS));
    }
}
