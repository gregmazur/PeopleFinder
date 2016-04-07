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
    private Button loadMoreButton;
    private ImageService imageService;
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
        List<Person> potentialPersons = mainPage.getService().getUserList(Utils.GET_USERS_METHOD, map, 1000, 0);
        if (!mainPage.getSearchPerson().getImages().isEmpty()) {
            imageService.addProcessingListener(this);
            imageService.findSimilarPeople(mainPage.getSearchPerson(), potentialPersons);
        }else {
            proceedPersons = potentialPersons;
            for (index = 0; index < 20; ++index) {
                addRow(index);
            }
            addLoadMoreBtn();
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
        listLayout.setImmediate(true);

        loadMoreButton = new Button("LOAD MORE");
        loadMoreButton.addClickListener(buttonEvent -> {
            listLayout.removeAllComponents();
            int max = 20 + index;
            for (int i = 0; i < max && index < proceedPersons.size(); i++, index++) {
                addRow(i);
            }
            listLayout.addComponent(loadMoreButton);
            listLayout.setComponentAlignment(loadMoreButton, Alignment.MIDDLE_CENTER);
            mainPage.push();
        });

        setContent(listLayout);
    }


    public void addRow(int index) {
        HorizontalLayout layout = new HorizontalLayout();
        Person person = proceedPersons.get(index);
        Image image = new Image();
        image.setHeight("207px");
        image.setWidth("200px");
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
        boolean firstLoad = proceedPersons.isEmpty();
        proceedPersons.addAll(event.getProcessedPersons());
        proceedPersons.sort(Person::compareBySimilarity);
        if (firstLoad) {
            listLayout.removeAllComponents();
            for (int i = 0; i < proceedPersons.size(); ++i) {
                addRow(i);
            }
            addLoadMoreBtn();
            mainPage.push();
        }
    }

    private void addLoadMoreBtn(){
        listLayout.addComponent(loadMoreButton);
        listLayout.setComponentAlignment(loadMoreButton, Alignment.MIDDLE_CENTER);
    }
}

