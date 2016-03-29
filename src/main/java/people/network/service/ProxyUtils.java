package people.network.service;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 *
 *
 * @author Yemelin A.M. <a href="mailto:artem@ibis.ua">artem@ibis.ua</a>
 **/
public enum ProxyUtils {

    INSTANCE;

    private Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.ibis", 3128));

    public static Proxy getProxy() {
        return INSTANCE.proxy;
    }
}
