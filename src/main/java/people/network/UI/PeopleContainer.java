/**
 * File: $Id: PeopleContainer.java,v 1.0 2016/03/29 12:59 $
 * Copyright 2000, 2001 by Integrated Banking Information Systems Limited,
 * 22 Uspenskaya st., Odessa, Ukraine.
 * All rights reserved.
 * <p>
 * This Software is owned by Integrated Banking Information Systems Limited.
 * The structure, organization and code of the Software are the valuable
 * trade secrets and confidential information of Integrated Banking
 * Information Systems Limited. The Software is protected by copyright,
 * including without limitation by Ukrainian Copyright Law,
 * international treaty provisions and applicable laws in the country
 * in  which it is being used.
 * You shall use such Confidential Information only in accordance with
 * the terms of the license agreement you entered into with IBIS.
 * It may not disclosed to any third party or used to create any software
 * which is substantially similar to the expression of the Software.
 */
package people.network.UI;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import lombok.Data;
import org.springframework.util.MultiValueMap;
import people.network.entity.user.Person;
import people.network.service.ImageService;
import people.network.service.image.ImageProcessing;
import people.network.service.rest.Utils;

import java.io.IOException;
import java.util.List;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
@Data
public class PeopleContainer {
    private MainPage mainPage;
    private boolean pictureUploaded;
    private List<Person> potentialPersons;
    private ImageService imageService;

    public PeopleContainer(MainPage mainPage, ImageService service) {
        this.mainPage = mainPage;
        this.pictureUploaded = !mainPage.getSearchPerson().getImages().isEmpty();
        this.imageService = service;
        init();
    }

    private void init() {
        MultiValueMap<String, String> map = mainPage.getSearchPerson().getUserSearchParams();
        potentialPersons = mainPage.getService().getUserList(Utils.GET_USERS_METHOD, map, 1000, 0);
        if (pictureUploaded) {
            potentialPersons = imageService.getSimilarPeople(mainPage.getSearchPerson(), potentialPersons);
        }
    }

    public HorizontalLayout getRow(int index) throws IOException {
        HorizontalLayout layout = new HorizontalLayout();
        Person person = potentialPersons.get(index);
        Image image = new Image();
        image.setHeight("158px");
        image.setWidth("133px");
        Resource resource = pictureUploaded ?
                new ImageStreamResource(person.getPictureStream(), String.valueOf(person.getId())) :
                new ExternalResource(person.getPicURL());
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
        return layout;
    }


}
