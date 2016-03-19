package people.network.UI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;
import org.springframework.util.MultiValueMap;
import people.network.rest.JsonService;

/**
 * Created by greg on 19.03.16.
 */
public class PeopleFoundView extends VerticalLayout implements View {
    private MultiValueMap<String, String> userSearchParams;
    private JsonService service;

    public PeopleFoundView(JsonService service, MultiValueMap<String, String> userSearchParams) {
        this.userSearchParams = userSearchParams;
        this.service = service;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
