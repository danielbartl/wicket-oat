package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.Oat;

public class GettingStartedPage extends BasePage {

    public GettingStartedPage() {
        // We'll add a live component demo at the end of the tutorial
        add(Oat.Components.button("demoButton", "Try Me!", target -> {
            Oat.toast(target, "It works! You're ready to build.", OatVariant.SUCCESS, "Success");
        }));
    }
}
