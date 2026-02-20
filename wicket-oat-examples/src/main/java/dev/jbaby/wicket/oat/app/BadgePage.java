package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.Badge;
import dev.jbaby.wicket.oat.components.BadgeBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class BadgePage extends BasePage {

    public BadgePage() {
        add(new Badge("defaultBadge", "Default"));
        add(new Badge("secondaryBadge", "Secondary", BadgeBehavior.Variant.SECONDARY));
        add(new Badge("outlineBadge", "Outline", BadgeBehavior.Variant.OUTLINE));
        add(new Badge("successBadge", "Success", BadgeBehavior.Variant.SUCCESS));
        add(new Badge("warningBadge", "Warning", BadgeBehavior.Variant.WARNING));
        add(new Badge("dangerBadge", "Danger", BadgeBehavior.Variant.DANGER));

        // Using behavior on a regular Label
        Label customLabel = new Label("customBehaviorBadge", "New");
        customLabel.add(new BadgeBehavior(BadgeBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
