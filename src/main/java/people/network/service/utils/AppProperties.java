/**
 * File: $Id: AppProperties.java,v 1.0 2016/04/11 12:59 $
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
package people.network.service.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Properties;

/**
 * Application properties
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public enum AppProperties {

    INSTANCE;

    public final static String PROPERTY_FILE = "application.properties";

    public final static String PROXY_IN_USE   = "proxy.use";
    public final static String PROXY_HOST     = "proxy.host";
    public final static String PROXY_PORT     = "proxy.port";

    private Properties prop;
    private Proxy proxy;

    AppProperties() {
        prop = new Properties();
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE);
        try {
            if (inStream != null) {
                prop.load(inStream);
            } else {
                throw new FileNotFoundException(String.format("Property file '%s' not found in the classpath.", PROPERTY_FILE));
            }
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }

        String proxyHost = prop.getProperty(PROXY_HOST);
        int proxyPort = Integer.valueOf(prop.getProperty(PROXY_PORT));
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }

    public static String getPropertyValue(String propertyKey) {
        return INSTANCE.prop.getProperty(propertyKey);
    }

    public static String getProxyHost() {
        return getPropertyValue(PROXY_HOST);
    }

    public static String getProxyPort() {
        return getPropertyValue(PROXY_PORT);
    }

    public static Proxy getProxy() {
        return INSTANCE.proxy;
    }

    public static boolean isProxyInUse() {
        String proxyInUse = getPropertyValue(PROXY_IN_USE);
        return Boolean.valueOf(proxyInUse);
    }
}
