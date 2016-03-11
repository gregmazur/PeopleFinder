package people.network.entity;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;

/**
 * Created by greg on 08.03.16.
 */

@SpringComponent
@UIScope
public class Details {

    private String name;
    private int age;
    private int city;

    public String makeRequest() {
        return "users.search?params[q]=" +
                getFormatedName() + "&params[count]=5&params[fields]=photo%2Cscreen_name&params[online]=0&params[has_photo]=0&params[v]=5.45";
    }

    private String getFormatedName() {
        if (null == name) return " ";
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            result.append(words[i]);
            if (i < words.length-1) result.append("%20");
        }
        return result.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
