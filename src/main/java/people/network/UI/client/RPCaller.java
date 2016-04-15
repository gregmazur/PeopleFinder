package people.network.UI.client;

import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * created by Greg 15-04-2016
 **/
public class RPCaller extends AbstractJavaScriptComponent {
    public RPCaller() {
        addFunction("returnResponse", arguments -> {
            String response = arguments.getString(0);
            // do whatever
        });
    }

    public void makeRequest(String url) {
        callFunction("makeRequest", url);
    }
}
