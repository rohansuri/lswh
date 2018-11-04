package jdk.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimeIt implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(TimeIt.class);

    private final Object whomToProxy;

    public TimeIt(Object whomToProxy){
        this.whomToProxy = whomToProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        logger.info("Inside invocation handler {}", this.getClass());

        // if the actual object has jdk.proxy.Time annotation on it's method
        if(whomToProxy.getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(Time.class) == null){
            logger.info("Method doesn't have jdk.proxy.Time annotation, will not be timed");
            return method.invoke(whomToProxy, args);
        }

        long start = System.nanoTime();
        Object toReturn = method.invoke(whomToProxy, args);
        long diff = System.nanoTime() - start;

        logger.info("Time taken by method {} is {} nanos", method.getName(), diff);

        return toReturn;
    }
}
