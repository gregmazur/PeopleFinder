package people.network.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespSrchCrtriaObj implements Serializable {

    private String title;
    private String name;
    private long id;

    @Override
    public String toString() {
        return null == title ? name : title;
    }
}
