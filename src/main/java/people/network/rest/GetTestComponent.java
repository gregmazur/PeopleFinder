/**
 * File: $Id: GetTestComponent.java,v 1.0 2016/03/10 18:39 $
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
package people.network.rest;

import com.vaadin.ui.*;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 */
public class GetTestComponent extends Panel {

    private final VerticalLayout layout = new VerticalLayout();
    private final Token accessToken;
    private final ApiInfo service;
    private TextArea responseArea;

    public GetTestComponent(ApiInfo service, String accessToken, String accessTokenSecret) {
        setSizeFull();

        setContent(layout);
        layout.setSizeFull();

        this.service = service;
        this.accessToken = new Token(accessToken, accessTokenSecret);

        layout.setMargin(true);
        final TextField field = new TextField("Request:", service.exampleGetRequest);
        field.setWidth("100%");
        layout.addComponent(field);

        Button bu = new Button("GET");
        layout.addComponent(bu);
        bu.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                sendGet(field.getValue());
            }
        });

        responseArea = new TextArea("Response:");
        responseArea.setSizeFull();
        layout.addComponent(responseArea);
        layout.setExpandRatio(responseArea, 1);
    }

    private void sendGet(String get) {
        OAuthRequest request = new OAuthRequest(Verb.GET, get);
        createOAuthService().signRequest(accessToken, request);
        Response resp = request.send();
        responseArea.setValue(resp.getBody());

    }

    private OAuthService createOAuthService() {
        ServiceBuilder sb = new ServiceBuilder();
        sb.provider(service.scribeApi);
        sb.apiKey(service.apiKey);
        sb.apiSecret(service.apiSecret);
        sb.callback("http://www.google.fi");
        return sb.build();
    }
}
