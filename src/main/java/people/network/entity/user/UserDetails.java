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
public class UserDetails implements Serializable{

    private static final long serialVersionUID = 6083957108569951546L;

    private long id;
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "last_name")
    private String lastName;
    @JsonProperty(value = "photo_max_orig")
    private String picURL;

    private double similarity;
    private URL url;
    private transient FImage fImage;

    @Override
    public String toString() {
        return "UserDetails{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    public boolean loadImage() {
        try {
            url = new URL(picURL);
            fImage = ImageUtilities.readF(url);
        } catch(Throwable th) {
            url = null;
            fImage = null;
            return false;
        }
        return true;
    }

    public InputStream getPictureStream() throws IOException {
        return url.openStream();
    }

    public static int compareBySimilarity(UserDetails u1, UserDetails u2) {
        return Double.compare(u1.getSimilarity(), u2.getSimilarity());
    }
}
