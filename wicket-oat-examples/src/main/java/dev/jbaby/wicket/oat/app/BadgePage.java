package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.behaviors.BadgeBehavior;
import org.apache.wicket.markup.html.basic.Label;

public class BadgePage extends BasePage {

    public BadgePage() {
        add(new OatBadge("defaultBadge", "Default"));
        add(new OatBadge("secondaryBadge", "Secondary", BadgeBehavior.Variant.SECONDARY));
        add(new OatBadge("outlineBadge", "Outline", BadgeBehavior.Variant.OUTLINE));
        add(new OatBadge("successBadge", "Success", BadgeBehavior.Variant.SUCCESS));
        add(new OatBadge("warningBadge", "Warning", BadgeBehavior.Variant.WARNING));
        add(new OatBadge("dangerBadge", "Danger", BadgeBehavior.Variant.DANGER));

        // Using behavior on a regular Label
        Label customLabel = new Label("customBehaviorBadge", "New");
        customLabel.add(new BadgeBehavior(BadgeBehavior.Variant.SUCCESS));
        add(customLabel);
    }
}
