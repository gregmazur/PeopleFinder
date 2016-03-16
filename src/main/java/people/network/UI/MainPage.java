package people.network.UI;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import people.network.rest.JsonService;

import java.io.IOException;

/**
 * Created by greg on 08.03.16.
 */
@Theme("valo")
@SpringUI
@PreserveOnRefresh
public class MainPage extends UI {

    @Autowired
    private JsonService service;

    @Override
    protected void init(VaadinRequest request) {
        String token = getAccessToken();
        service.setAccessToken(token);
        if (null == token) openSignInWindow();
        else setContent(new FindingForm(service));

    }

    private String getAccessToken() {
        String uri = getPage().getUriFragment();
        if (null != uri) return uri.substring(uri.indexOf("=") + 1, uri.indexOf("&"));
        return null;
    }

    private void openSignInWindow() {
        Window subWindow = new Window("Welcome");
        VerticalLayout subContent = new VerticalLayout();
        subContent.setMargin(true);
        subWindow.setContent(subContent);
        subWindow.setWidth(30, Unit.PERCENTAGE);
        subWindow.setHeight(30, Unit.PERCENTAGE);

        subContent.addComponent(new Label("Please login first"));
        String link = "https://oauth.vk.com/authorize?client_id=5343222&display=page&redirect_uri=http://localhost:8080&scope=friends&response_type=token&v=5.8";
        subContent.addComponent(new Button("LOGIN", event -> {
            getPage().open(link, "login");
        }));
        // Center it in the browser window
        subWindow.center();

        // Open it in the UI
        addWindow(subWindow);
    }


//            "5343222",
//            "th0q54SVYPLxl53EoO7c",
//            "https://api.vk.com/method");


}
