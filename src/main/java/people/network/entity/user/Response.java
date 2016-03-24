package people.network.entity.user;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by greg on 14.03.16.
 */
@Data
public class Response implements Serializable{
    static final long serialVersionUID = -7627629688361524110L;
    private int count;
    private Person[] items;
}
