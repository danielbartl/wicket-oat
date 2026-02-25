package dev.jbaby.wicket.oat;

import dev.jbaby.wicket.oat.app.*;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
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
            public Session newSession(Request request, Response response) {
                return new OatSession(request);
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
                mountPage("/table", TablePage.class);
                mountPage("/data-table", DataTablePage.class);
                mountPage("/components", ComponentsPage.class);
                mountPage("/theme", ThemePage.class);

                getComponentInstantiationListeners().add(
                        new SpringComponentInjector(this, ctx));

                WicketOats.install(this);
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
