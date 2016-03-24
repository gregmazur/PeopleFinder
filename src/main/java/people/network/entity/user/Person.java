package people.network.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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

    public FImage getFImagePic() {
        try {
            URL url = new URL(picURL);
            return ImageUtilities.readF(url);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getPictureStream() throws IOException {
        URL url = new URL(picURL);
        return url.openStream();
    }

    public static int compareBySimilarity(Person u1, Person u2) {
        return Double.compare(u1.getSimilarity(), u2.getSimilarity());
    }
}
