package people.network.UI;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import org.vaadin.addon.oauthpopup.OAuthListener;
import org.vaadin.addon.oauthpopup.OAuthPopupButton;
import org.vaadin.addon.oauthpopup.OAuthPopupOpener;
import people.network.entity.Details;
import people.network.rest.ApiInfo;
import people.network.rest.GetTestComponent;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by greg on 08.03.16.
 */
@Theme("valo")
@SpringUI
@Push
@PreserveOnRefresh
public class MainPage extends UI {


    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MainPage.class)
    public static class Servlet extends VaadinServlet {
    }

    // Twitter test application at http://localhost:8080
    private static final ApiInfo VK_API = new ApiInfo("VK",
            VkontakteApi.class,
            "5343222",
            "th0q54SVYPLxl53EoO7c",
            "https://api.vk.com/method");

    private final VerticalLayout layout = new VerticalLayout();


    @Override
    protected void init(VaadinRequest request) {


        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);


        addVKButton();


        layout.addComponent(new Link("Add-on at Vaadin Directory", new ExternalResource("http://vaadin.com/addon/oauth-popup-add-on")));
        layout.addComponent(new Link("Source code at GitHub", new ExternalResource("https://github.com/ahn/vaadin-oauthpopup")));
    }



    private void addVKButton() {
        ApiInfo api = VK_API;
        OAuthPopupButton button = new OAuthPopupButton(VkontakteApi.class,api.apiKey, api.apiSecret);
        addButton(api, button);
    }




    private void addButton(final ApiInfo service, OAuthPopupButton button) {


        // In most browsers "resizable" makes the popup
        // open in a new window, not in a tab.
        // You can also set size with eg. "resizable,width=400,height=300"
        button.setPopupWindowFeatures("resizable,width=400,height=300");


        HorizontalLayout hola = new HorizontalLayout();
        hola.setSpacing(true);
        hola.addComponent(button);


        layout.addComponent(hola);


        button.addOAuthListener(new Listener(service, hola));
    }


    private class Listener implements OAuthListener {


        private final ApiInfo service;
        private final HorizontalLayout hola;


        private Listener(ApiInfo service, HorizontalLayout hola) {
            this.service = service;
            this.hola = hola;
        }


        @Override
        public void authSuccessful(final String accessToken,
                                   final String accessTokenSecret, String oauthRawResponse) {
            hola.addComponent(new Label("Authorized."));
            Button testButton = new Button("Test " + service.name + " API");
            testButton.addStyleName(BaseTheme.BUTTON_LINK);
            hola.addComponent(testButton);
            testButton.addClickListener(event -> {
                GetTestComponent get = new GetTestComponent(service,
                        accessToken, accessTokenSecret);
                Window w = new Window(service.name, get);
                w.center();
                w.setWidth("75%");
                w.setHeight("75%");
                addWindow(w);
            });
        }


        @Override
        public void authDenied(String reason) {
            hola.addComponent(new Label("Auth failed."));
        }
    }


}
