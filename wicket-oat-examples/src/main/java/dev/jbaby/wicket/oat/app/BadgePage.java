package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.components.OatBadge;
import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;
import org.apache.wicket.markup.html.basic.Label;

public class BadgePage extends BasePage {

    public BadgePage() {
        add(new OatBadge("defaultBadge", "Default"));
        add(new OatBadge("secondaryBadge", "Secondary", OatVariant.SECONDARY));
        add(new OatBadge("outlineBadge", "Outline", OatVariant.SECONDARY).setOutline(true));
        add(new OatBadge("successBadge", "Success", OatVariant.SUCCESS));
        add(new OatBadge("warningBadge", "Warning", OatVariant.WARNING));
        add(new OatBadge("dangerBadge", "Danger", OatVariant.DANGER));

        // Using behavior on a regular Label
        Label customLabel = new Label("customBehaviorBadge", "New");
        customLabel.add(Oat.Behaviors.badge(OatVariant.SUCCESS));
        add(customLabel);
    }
}
