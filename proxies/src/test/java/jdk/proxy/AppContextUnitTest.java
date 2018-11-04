package jdk.proxy;

import jdk.proxy.AppContext;
import jdk.proxy.TimeMe;
import jdk.proxy.TimeMeImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class AppContextUnitTest {

    @Test
    public void test()throws Exception{
        Method method = TimeMeImpl.class.getMethod("timeMe", null);
        Assert.assertNotNull(AppContext.isAnInterfaceMethod(method));
    }

    @Test
    public void testGetBean() {
        AppContext ctx = new AppContext();

        TimeMe impl = new TimeMeImpl();

        ctx.addBean(impl);
        ctx.start(); // spring does it's magic
        impl = ctx.getBean(TimeMe.class);

        Assert.assertNotNull(impl);
    }
}
