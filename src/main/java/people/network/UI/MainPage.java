package people.network.UI;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import people.network.entity.SearchPerson;
import people.network.service.rest.JsonService;

import java.util.Locale;

/**
 * Created by greg on 08.03.16.
 */
@Theme("valo")
@SpringUI
@Data
public class MainPage extends UI {
    private static final long serialVersionUID = 5548861727207728718L;

    public static final String ENTERING_FORM = "";
    public static final String PEOPLE_FOUND = "PeopleFound";

    private SearchPerson searchPerson;
    private Navigator navigator;

    @Autowired
    private JsonService service;

    @Override
    protected void init(VaadinRequest request) {
        Locale locale = request.getLocale();
        setLocale(locale);
        System.out.println(locale);
        navigator = new Navigator(this, this);
        navigator.addView(ENTERING_FORM, new FindingForm(this));
        navigator.addView(PEOPLE_FOUND, new PeopleFoundView(this));
    }

}
