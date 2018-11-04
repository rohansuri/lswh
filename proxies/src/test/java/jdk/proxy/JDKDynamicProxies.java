package jdk.proxy;

import jdk.proxy.AppContext;
import jdk.proxy.TimeMe;
import jdk.proxy.TimeMeImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// JDK's Dynamic Proxy limitation:
// can only proxy interface methods
// for more power, we need to use CGLIB
public class JDKDynamicProxies {
    private static final Logger logger = LoggerFactory.getLogger(JDKDynamicProxies.class);

    @Test
    public void test() throws InterruptedException {
        AppContext ctx = new AppContext();

        // before proxying
        TimeMe impl = new TimeMeImpl();
        impl.timeMe();


        ctx.addBean(impl);
        ctx.start(); // spring does it's magic
        impl = ctx.getBean(TimeMe.class);

        // after proxying
        impl.timeMe();

        logger.info("Runtime class is {}",impl.getClass());

    }

}


