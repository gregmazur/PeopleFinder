/**
 * File: $Id: ProcessingEvent.java,v 1.0 2016/04/06 13:53 $
 * Copyright 2000, 2001 by Integrated Banking Information Systems Limited,
 * 22 Uspenskaya st., Odessa, Ukraine.
 * All rights reserved.
 *
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
package people.network.service;

import lombok.Data;
import people.network.entity.user.Person;

import java.util.EventObject;
import java.util.List;

/**
 * Processing Event
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
@Data
public class ProcessingEvent extends EventObject {

    private EventId eventId;

    private List<Person> processedPersons;

    public ProcessingEvent(Object source, EventId eventId, List<Person> processedPersons) {
        super(source);
        this.eventId = eventId;
        this.processedPersons = processedPersons;
    }

    public enum EventId {
        PARTIAL_PROCESSING, FINAL_PROCESSED;
    }
}
