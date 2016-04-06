package people.network.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import people.network.service.utils.ProxyUtils;

import java.io.*;
import java.net.URL;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person implements Serializable {

    private static final long serialVersionUID = 6083957108569951546L;

    private long id;
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "last_name")
    private String lastName;
    @JsonProperty(value = "photo_max_orig")
    private String picURL;

    private double similarity;

    @Override
    public String toString() {
        return "UserDetails{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Person)) return false;
        Person that = (Person) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public FImage getFImage() {
        try {
            URL url = new URL(picURL);
            InputStream stream = url.openConnection(ProxyUtils.getProxy()).getInputStream();
            return ImageUtilities.readF(stream);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int compareBySimilarity(Person p1, Person p2) {
        return Double.compare(p1.getSimilarity(), p2.getSimilarity());
    }
}
