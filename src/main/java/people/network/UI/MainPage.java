package people.network.UI;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import people.network.UI.client.RPCaller;
import people.network.UI.views.FindingForm;
import people.network.UI.views.PeopleFoundView;
import people.network.entity.SearchPerson;
import people.network.service.resourceProvider.SourceService;
import people.network.service.rest.ExternalRestService;

import javax.servlet.annotation.WebServlet;
import java.util.Locale;

/**
 * Created by greg on 08.03.16.
 */
@EqualsAndHashCode(callSuper = true)
@Theme("valo")
@SpringUI
@Push
@Data
public class MainPage extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MainPage.class, widgetset = "people.network.UI.CustomWidget")
    public static class Servlet extends VaadinServlet {
    }

    private static final long serialVersionUID = 5548861727207728718L;

    public static final String ENTERING_FORM = "";
    public static final String PEOPLE_FOUND = "PeopleFound";

    private SearchPerson searchPerson = new SearchPerson();
    private RPCaller rpCaller;
    private Navigator navigator;

    @Autowired
    private SourceService source;
    @Autowired
    private ExternalRestService service;

    @Override
    protected void init(VaadinRequest request) {
        Locale locale = request.getLocale();
        source.setLocale(locale);
        setLocale(locale);
        navigator = new Navigator(this, this);
        navigator.addView(ENTERING_FORM, new FindingForm(this));
        PeopleFoundView view = new PeopleFoundView(this);
        rpCaller = view.createRPCaller();
        navigator.addView(PEOPLE_FOUND, view);
    }

}
