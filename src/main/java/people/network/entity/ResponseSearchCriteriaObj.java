package people.network.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseSearchCriteriaObj {
    private String title;
    private int id;

    @Override
    public String toString() {
        return title;
    }
}
