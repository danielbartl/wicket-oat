package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.Oat;
import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class BadgePage extends BasePage {

    public BadgePage() {
        add(Oat.Components.badge("defaultBadge", "Default"));
        add(Oat.Components.badge("secondaryBadge", "Secondary", BadgeBehavior.Variant.SECONDARY));
        add(Oat.Components.badge("outlineBadge", "Outline", BadgeBehavior.Variant.SECONDARY).setOutline(true));
        add(Oat.Components.badge("successBadge", "Success", BadgeBehavior.Variant.SUCCESS));
        add(Oat.Components.badge("warningBadge", "Warning", BadgeBehavior.Variant.WARNING));
        add(Oat.Components.badge("dangerBadge", "Danger", BadgeBehavior.Variant.DANGER));

        // Using behavior on a regular Label
        Label customLabel = new Label("customBehaviorBadge", "New");
        customLabel.add(Oat.Behaviors.badge(BadgeBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
