package people.network.UI.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import org.springframework.util.MultiValueMap;
import org.vaadin.alump.lazylayouts.LazyComponentProvider;
import org.vaadin.alump.lazylayouts.LazyComponentRequestEvent;
import org.vaadin.alump.lazylayouts.LazyLoadingIndicator;
import org.vaadin.alump.lazylayouts.LazyVerticalLayout;
import org.vaadin.viritin.LazyList;
import org.vaadin.viritin.ListContainer;
import org.vaadin.viritin.fields.MTable;
import people.network.UI.MainPage;
import people.network.UI.PeopleContainer;
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.image.ImageProcessing;
import people.network.service.rest.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by greg on 19.03.16.
 */
public class PeopleFoundView extends VerticalLayout implements View, LazyComponentProvider {
    private static final long serialVersionUID = -1200000724647918808L;
    private MainPage mainPage;
    private LazyVerticalLayout lazyLayout;
    private PeopleContainer peopleContainer;
    private ImageService imageService;
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

        lazyLayout.enableLazyLoading(this);
        updateIndicatorText();
    }

    private void init() {
        lazyLayout = new LazyVerticalLayout();

        // Parent can be also defined manually, but here we trust the automatic resolving
        //lazyLayout.setScrollingParent(panel);

        lazyLayout.setWidth("100%");
        lazyLayout.setStyleName("demoContentLayout");
        lazyLayout.setSpacing(true);
        lazyLayout.setMargin(true);
        addComponent(lazyLayout);
    }

    @Override
    public void onLazyComponentRequest(LazyComponentRequestEvent event) {
        new DelayedAddRunnable(event).run();
    }

    protected void addNewRow(ComponentContainer container) throws IOException {
        container.addComponent(peopleContainer.getRow(++indexCounter));
    }

    protected void updateIndicatorText() {
        LazyLoadingIndicator indicator = (LazyLoadingIndicator) lazyLayout.getLazyLoadingIndicator();


        int last = indexCounter + 5;
        if (last > maxNumberOfPpl) {
            last = maxNumberOfPpl;
        }
        indicator.setMessage("Loading " + (indexCounter + 1) + "-" + last + " / " + maxNumberOfPpl + "...");

    }

    private class DelayedAddRunnable implements Runnable {
        private LazyComponentRequestEvent event;

        public DelayedAddRunnable(LazyComponentRequestEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            // Add delay to act like DB query would be done here
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            PeopleFoundView.this.getUI().access(() -> {
                try {
                    lazyLoad(event);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected void lazyLoad(LazyComponentRequestEvent event) throws IOException {

        // Check how many to add
        int load = 10;
        if (indexCounter + load >= maxNumberOfPpl) {
            load = maxNumberOfPpl - indexCounter;
        }

        // Add new components
        for (int i = 0; i < load; ++i) {
            addNewRow(event.getComponentContainer());
        }

        // Disable when limit is hit
        if (indexCounter >= maxNumberOfPpl) {
            lazyLayout.disableLazyLoading();
            Notification.show("All demo content loaded. Use reset to start from the beginning :)");
        } else {
            updateIndicatorText();
        }
    }
}

