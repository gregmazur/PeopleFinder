package people.network.UI.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import org.springframework.util.MultiValueMap;
import people.network.UI.MainPage;
import people.network.UI.client.RPCaller;
import people.network.entity.user.Occupation;
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.ProcessingEvent;
import people.network.service.ProcessingListener;
import people.network.service.image.ImageProcessing;
import people.network.service.resourceProvider.SourceService;
import people.network.service.rest.Utils;

import java.io.IOException;
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
    private SourceService source;
    private List<Person> proceedPersons = new ArrayList<>();
    private int showedPersonsCount = 0;
    RPCaller caller;

    public PeopleFoundView(MainPage mainPage) {
        this.mainPage = mainPage;
        imageService = ImageProcessing.createInstance();
        source = mainPage.getSource();
        init();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        showedPersonsCount = 0;
        listLayout.removeAllComponents();
        MultiValueMap<String, String> map = mainPage.getSearchPerson().getUserSearchParams();
        String url = mainPage.getService().buildURLforPersonSearchRequest(Utils.GET_USERS_METHOD, map, 1000, 0);
        RPCaller caller = mainPage.getRpCaller();
        caller.makeRequest(url);
        List<Person> potentialPersons = new ArrayList<>();
//        try {
//            potentialPersons = mainPage.getService().getUserList(caller.getResponse());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!mainPage.getSearchPerson().getImages().isEmpty()) {
            imageService.addProcessingListener(this);
            imageService.findSimilarPeople(mainPage.getSearchPerson(), potentialPersons);
        } else {
            proceedPersons = potentialPersons;
            for (showedPersonsCount = 0; showedPersonsCount < 20 && showedPersonsCount < potentialPersons.size(); ++showedPersonsCount) {
                addRow(showedPersonsCount);
            }
        }
        listLayout.addComponent(new Label(source.getMessage("pls.wait.message")));
        addLoadMoreBtn();

    }

    private void init() {
        setResponsive(true);
        setSizeFull();
        setImmediate(true);
        listLayout = new VerticalLayout();

        listLayout.setWidth("100%");
        listLayout.setSpacing(true);
        listLayout.setMargin(true);
        listLayout.setImmediate(true);

        loadMoreButton = new Button(source.getMessage("load.more.btn"));
        loadMoreButton.addClickListener(buttonEvent -> {
            listLayout.removeAllComponents();
            int max = 20 + showedPersonsCount;
            for (int i = 0; i < max && showedPersonsCount < proceedPersons.size(); i++, showedPersonsCount++) {
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

        image.setWidth("219px");
        Resource resource = new ExternalResource(person.getPicURL());
        image.setSource(resource);
        String url = "http://vk.com/id" + person.getId();

        image.addClickListener(event -> mainPage.getPage().open(url, person.getFirstName() + person.getLastName()));
        layout.addComponent(image);

        VerticalLayout rightSide = new VerticalLayout();
        rightSide.setSpacing(true);
        layout.addComponent(rightSide);
        layout.setExpandRatio(rightSide, 1.0f);

        Link link = new Link(url, new ExternalResource(url));
        rightSide.addComponent(link);
        Label name = new Label(person.getFirstName() + " " + person.getLastName());
        rightSide.addComponent(name);
        Occupation occupationValue = person.getOccupation();
        if (null != occupationValue) {
            Label occupation = new Label(occupationValue.getName());
            rightSide.addComponent(occupation);
        }
        if (person.getSimilarity() > 0) {
            Label similar = new Label(source.getMessage("similarity") + String.valueOf(100 - person.getSimilarity()));
            rightSide.addComponent(similar);
        }
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
            showedPersonsCount = proceedPersons.size();
        }
        updateList(showedPersonsCount);

    }

    private void updateList(int portionSize) {
        listLayout.removeAllComponents();
        for (int i = 0; i < portionSize; ++i) {
            addRow(i);
        }
        addLoadMoreBtn();
        mainPage.push();
    }

    private void addLoadMoreBtn() {
        listLayout.addComponent(loadMoreButton);
        listLayout.setComponentAlignment(loadMoreButton, Alignment.MIDDLE_CENTER);
    }

    public RPCaller createRPCaller() {
        return new RPCaller(this);
    }
}

