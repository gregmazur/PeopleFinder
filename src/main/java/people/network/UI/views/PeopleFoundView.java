package people.network.UI.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.util.MultiValueMap;
import people.network.UI.MainPage;
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.ProcessingEvent;
import people.network.service.ProcessingListener;
import people.network.service.image.ImageProcessing;
import people.network.service.rest.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 19.03.16.
 */
@SpringView
public class PeopleFoundView extends Panel implements View, ProcessingListener {
    private static final long serialVersionUID = -1200000724647918808L;
    private MainPage mainPage;
    private VerticalLayout listLayout;
    private ImageService imageService;
    private List<Person> potentialPersons = new ArrayList<>();
    private List<Person> proceedPersons = new ArrayList<>();
    private int index = 0;

    public PeopleFoundView(MainPage mainPage) {
        this.mainPage = mainPage;
        imageService = ImageProcessing.createInstance();
        init();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        listLayout.removeAllComponents();
        MultiValueMap<String, String> map = mainPage.getSearchPerson().getUserSearchParams();
        potentialPersons = mainPage.getService().getUserList(Utils.GET_USERS_METHOD, map, 1000, 0);
        if (!mainPage.getSearchPerson().getImages().isEmpty()) {
            imageService.addProcessingListener(this);
            imageService.findSimilarPeople(mainPage.getSearchPerson(), potentialPersons);
        }else {
            for (index = 0; index < 10; ++index) {
                addRow(index, potentialPersons);
            }
        }
    }

    private void init() {

        setSizeFull();
        setImmediate(true);

        listLayout = new VerticalLayout();

        listLayout.setWidth("100%");
        listLayout.setStyleName("demoContentLayout");
        listLayout.setSpacing(true);
        listLayout.setMargin(true);
        setContent(listLayout);
    }

    @Override
    public void setScrollTop(int scrollTop) {
        super.setScrollTop(scrollTop);
        addRow(++index,potentialPersons);
        mainPage.push();

    }

    public void addRow(int index, List<Person> persons) {
        HorizontalLayout layout = new HorizontalLayout();
        Person person = persons.get(index);
        Image image = new Image();
        image.setHeight("158px");
        image.setWidth("133px");
        Resource resource = new ExternalResource(person.getPicURL());
        image.setSource(resource);
        layout.addComponent(image);

        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(true);
        layout.addComponent(rightSide);
        layout.setExpandRatio(rightSide, 1.0f);
        Label label = new Label(person.toString());
        rightSide.addComponent(label);
        String url = "http://vk.com/id" + person.getId();
        Link link = new Link(url, new ExternalResource(url));
        rightSide.addComponent(link);
        rightSide.addComponent(label);
        listLayout.addComponent(layout);
        layout.setWidth(50, Unit.PERCENTAGE);
        listLayout.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void eventHappened(ProcessingEvent event) {
        listLayout.removeAllComponents();
        proceedPersons.addAll(event.getProcessedPersons());
        proceedPersons.sort(Person::compareBySimilarity);
        for (int i = 0; i < proceedPersons.size(); ++i) {
            addRow(i, proceedPersons);
        }
        mainPage.push();
    }
}

