package people.network.entity.user;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;


/**
 * Created by greg on 08.03.16.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails implements Serializable {

    private static final long serialVersionUID = 6083957108569951546L;

    private long id;
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "last_name")
    private String lastName;
    @JsonProperty(value = "photo_max_orig")
    private String picURL;

    private InputStream picture;

    @Override
    public String toString() {
        return "UserDetails{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
