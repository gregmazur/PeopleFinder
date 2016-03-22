/**
 * File: $Id: SearchPerson.java,v 1.0 2016/03/21 14:43 $
 * Copyright 2000, 2001 by Integrated Banking Information Systems Limited,
 * 22 Uspenskaya st., Odessa, Ukraine.
 * All rights reserved.
 * <p>
 * This Software is owned by Integrated Banking Information Systems Limited.
 * The structure, organization and code of the Software are the valuable
 * trade secrets and confidential information of Integrated Banking
 * Information Systems Limited. The Software is protected by copyright,
 * including without limitation by Ukrainian Copyright Law,
 * international treaty provisions and applicable laws in the country
 * in  which it is being used.
 * You shall use such Confidential Information only in accordance with
 * the terms of the license agreement you entered into with IBIS.
 * It may not disclosed to any third party or used to create any software
 * which is substantially similar to the expression of the Software.
 */
package people.network.entity;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.util.Collection;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/
@Data
public class SearchPerson {
    private Collection<InputStream> images;
    private MultiValueMap<String, String> userSearchParams = new LinkedMultiValueMap<>(35);
}
