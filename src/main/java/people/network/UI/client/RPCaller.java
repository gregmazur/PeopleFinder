package people.network.UI.client;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import lombok.Getter;
import people.network.UI.views.PeopleFoundView;

/**
 * created by Greg 15-04-2016
 **/
@Getter
@JavaScript({"RPC.js"})
public class RPCaller extends AbstractJavaScriptComponent {
    private PeopleFoundView instance;
    private String response;

    public RPCaller(PeopleFoundView instance) {
        addFunction("returnResponse", arguments -> {
            response = arguments.getString(0);
            // do whatever
        });
    }

    public void makeRequest(String url) {
        callFunction("makeRequest", url);
    }
}
