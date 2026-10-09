package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.app.*;
import org.apache.wicket.Page;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WicketOatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WicketOatApplication.class, args);
    }

    @Bean
    WebApplication wicketApp(ApplicationContext ctx) {
        return new WebApplication() {
            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }

            @Override
            protected void init() {

                super.init();

                mountPage("/home", HomePage.class);
                mountPage("/getting-started", GettingStartedPage.class);
                mountPage("/accordion", AccordionPage.class);
                mountPage("/alert", AlertPage.class);
                mountPage("/badge", BadgePage.class);
                mountPage("/button", ButtonPage.class);
                mountPage("/button-group", ButtonGroupPage.class);
                mountPage("/card", CardPage.class);
                mountPage("/form", FormPage.class);
                mountPage("/invoice", InvoicePage.class);
                mountPage("/app-shell", AppShellPage.class);
                mountPage("/customer", CustomerPage.class);
                mountPage("/order", OrderWizardPage.class);
                mountPage("/customers", CustomersPage.class);
                mountPage("/sign-in", SignInPage.class);
                mountPage("/table", TablePage.class);
                mountPage("/data-table", DataTablePage.class);
                mountPage("/components", ComponentsPage.class);
                mountPage("/theme", ThemePage.class);
                mountPage("/breadcrumb", BreadcrumbPage.class);
                mountPage("/pagination", PaginationPage.class);
                mountPage("/dialog", DialogPage.class);
                mountPage("/dropdown", DropdownPage.class);
                mountPage("/tabs", TabsPage.class);
                mountPage("/upload", UploadPage.class);

                getComponentInstantiationListeners().add(
                        new SpringComponentInjector(this, ctx));

                // Built-in themes plus a custom one, whose CSS is in examples.css
                // (.setDensity(OatDensity.COMPACT) would make every page denser)
                WicketOats.install(this)
                        .addTheme(new OatTheme("ocean", "Ocean", "🌊"));

                // The avatar demo loads photos from Unsplash; image hosts for an app's own
                // content are the app's to allow, on top of Wicket's strict default CSP.
                getCspSettings().blocking().add(CSPDirective.IMG_SRC, "https://images.unsplash.com");
            }
        };
    }

    @Bean
    public WicketFilter wicketFilter(WebApplication wicketApp) {

        final var filter = new WicketFilter(wicketApp);
        filter.setFilterPath("/");
        return filter;

    }
}
