package people.network.UI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.springframework.util.MultiValueMap;
import people.network.entity.user.UserDetails;
import people.network.rest.Utils;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 19.03.16.
 */
public class PeopleFoundView extends VerticalLayout implements View {
    private static final long serialVersionUID = -1200000724647918808L;
    private MainPage mainPage;

    public PeopleFoundView(MainPage mainPage) {
        this.mainPage = mainPage;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        MultiValueMap<String,String> map = mainPage.getSearchPerson().getUserSearchParams();
        map.add("fields", "photo_max_orig");
        List<UserDetails> userDetails = mainPage.getService().getUserList(Utils.GET_USERS_METHOD, map, 1000, 0);
        for (Object o : userDetails){
            try {
                UserDetails details = (UserDetails) o;
                HorizontalLayout layout = new HorizontalLayout(new Label(details.toString()));
                StreamResource resource = new StreamResource(new ImageStreamResource(details.getPictureStream()), o.toString());
                layout.setIcon(resource);
                addComponents(layout);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
