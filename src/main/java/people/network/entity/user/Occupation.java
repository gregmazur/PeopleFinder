package people.network.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author Mazur G
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Occupation {
    String name;
}
