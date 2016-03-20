package people.network.UI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.springframework.util.MultiValueMap;
import people.network.entity.user.UserDetails;
import people.network.rest.JsonService;
import people.network.rest.Utils;

import java.util.List;

/**
 * Created by greg on 19.03.16.
 */
public class PeopleFoundView extends VerticalLayout implements View {
    private static final long serialVersionUID = -1200000724647918808L;
    private MultiValueMap<String, String> userSearchParams;
    private JsonService service;

    public PeopleFoundView(JsonService service, MultiValueMap<String, String> userSearchParams) {
        this.userSearchParams = userSearchParams;
        this.service = service;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        MultiValueMap<String,String> map = userSearchParams;
        map.add("fields", "photo_max_orig");
        List<UserDetails> userDetails = service.getUserList(Utils.GET_USERS_METHOD, userSearchParams, 1000, 0);
        for (Object o : userDetails){
            UserDetails details = (UserDetails) o;
            addComponents(new Label(details.toString()));
        }
    }
}
