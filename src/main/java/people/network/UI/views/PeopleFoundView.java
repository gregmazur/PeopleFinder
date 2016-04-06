package people.network.UI.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import people.network.UI.MainPage;
import people.network.UI.PeopleContainer;
import people.network.service.ImageService;
import people.network.service.image.ImageProcessing;

import java.io.IOException;

/**
 * Created by greg on 19.03.16.
 */
@SpringView
public class PeopleFoundView extends VerticalLayout implements View {
    private static final long serialVersionUID = -1200000724647918808L;
    private MainPage mainPage;
    private VerticalLayout lazyLayout;
    private PeopleContainer peopleContainer;
    private ImageService imageService;
    private Panel panel;
    private int indexCounter = 0;
    private int maxNumberOfPpl;

    public PeopleFoundView(MainPage mainPage) {
        this.mainPage = mainPage;
        imageService = ImageProcessing.createInstance();
        init();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        peopleContainer = new PeopleContainer(mainPage, imageService);
        maxNumberOfPpl = peopleContainer.getPotentialPersons().size();
        lazyLayout.removeAllComponents();
        indexCounter = 0;
        try {
            for (int i = 0; i < 10; ++i) {
                addNewRow(lazyLayout);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void init() {
        panel = new Panel();
        panel.setSizeFull();
        addComponent(panel);
        setExpandRatio(panel, 1.0f);

        lazyLayout = new VerticalLayout();

        lazyLayout.setWidth("100%");
        lazyLayout.setStyleName("demoContentLayout");
        lazyLayout.setSpacing(true);
        lazyLayout.setMargin(true);
        panel.setContent(lazyLayout);
    }


    protected void addNewRow(ComponentContainer container) throws IOException {
        container.addComponent(peopleContainer.getRow(++indexCounter));
    }


}

