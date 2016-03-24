package people.network.UI;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.springframework.util.MultiValueMap;
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.image.ImageProcessing;
import people.network.service.rest.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
        map.add("fields", "photo_max_orig,has_photo");
        List<Person> potentialPersons = mainPage.getService().getUserList(Utils.GET_USERS_METHOD, map, 1000, 0);
        ImageService imageService = ImageProcessing.createInstance();
        Collection<Person> result = imageService.getSimilarPeople(mainPage.getSearchPerson(), potentialPersons);
        int i = 1;
        for (Object o : result){
            try {
                Person person = (Person) o;
                System.out.println(String.format("Proc person %d of %d.", i++, result.size()));
                InputStream stream = person.getPictureStream();
                if(stream != null) {
                    HorizontalLayout layout = new HorizontalLayout(new Label(person.toString()));
                    StreamResource resource = new StreamResource(new ImageStreamResource(stream), o.toString());
                    layout.setIcon(resource);
                    addComponents(layout);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
