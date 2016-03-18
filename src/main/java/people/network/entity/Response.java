package people.network.entity;

import lombok.Data;

/**
 * Created by greg on 14.03.16.
 */
@Data
public class Response {
    private int count;
    private RespSrchCrtriaObj[] items;
}
