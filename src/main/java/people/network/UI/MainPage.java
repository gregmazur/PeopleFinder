package people.network.UI;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import people.network.entity.Details;

/**
 * Created by greg on 08.03.16.
 */
@Theme("valo")
@SpringUI
public class MainPage extends UI {
    @Autowired
    private Details details;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(true);
        root.setSpacing(true);
        setContent(root);

        FormLayout form = new FormLayout();

        final TextField name = new TextField("Name");
        name.addValueChangeListener(
                event -> {
                    details.setName(name.getValue());
                    root.addComponent(new Label(details.makeRequest()));
                });

        form.addComponents(name);
        root.addComponents(form);
    }
}
