package dev.jbaby.wicket.oat.app.showcase;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/** A scenario's markup and components, as a panel. */
class ScenarioPanel extends Panel implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {

    private final Scenario scenario;

    ScenarioPanel(String id, Scenario scenario) {
        super(id);
        this.scenario = scenario;
        scenario.populate().accept(this);
    }

    @Override
    public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
        return new StringResourceStream("<wicket:panel>" + scenario.markup() + "</wicket:panel>");
    }

    // One markup per scenario, rather than one for the class
    @Override
    public String getCacheKey(MarkupContainer container, Class<?> containerClass) {
        return ScenarioPanel.class.getName() + "-" + scenario.id();
    }
}
