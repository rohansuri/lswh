package jdk.proxy;

import jdk.proxy.AppContext;
import jdk.proxy.TimeMe;
import jdk.proxy.TimeMeImpl;
import org.apache.bcel.ExceptionConst;
import org.apache.bcel.classfile.ClassParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// JDK's Dynamic Proxy limitation:
// can only proxy interface methods
// for more power, we need to use CGLIB
/*
    see sun.misc.ProxyGenerator.ProxyMethod
    JDK at runtime creates a class file and puts the bytes for the methods in the interfaces we supplied,
    putting method signatures, etc
    and code inside each of those methods is simply calling the invocationhandler's invoke method
    with the right arguments
 */
public class JDKDynamicProxies {
    private static final Logger logger = LoggerFactory.getLogger(JDKDynamicProxies.class);

    @Test
    public void test() throws Exception  {
        AppContext ctx = new AppContext();

        // before proxying
        TimeMe impl = new TimeMeImpl();
        impl.timeMe();


        ctx.addBean(impl);
        ctx.start(); // spring does it's magic
        impl = ctx.getBean(TimeMe.class);

        // after proxying
        impl.timeMe();

        logger.info("Runtime class is {}", impl.getClass());

    }

}


