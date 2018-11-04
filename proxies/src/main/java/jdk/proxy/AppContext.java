package jdk.proxy;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// something like spring's ApplicationContext
public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);

    private final List<Object> beans = new ArrayList<>();

    public void start(){
        // let's say our jdk.proxy.AppContext for now, is only capable of acting on jdk.proxy.Time annotations

        for(int i = 0; i < beans.size(); i++){
            Object bean = beans.get(i);
            Class<?> clazz = bean.getClass();

            // for a bean, we replace the bean by our proxied bean if it has any jdk.proxy.Time annotations
            // where the proxy would intercept jdk.proxy.Time annotated methods
            // and measure the invocation time

            for(Method method: clazz.getMethods()){ // only public methods
                if(method.getAnnotation(Time.class) != null){

                    Class<?> _interface = isAnInterfaceMethod(method);

                    if(_interface == null){
                        continue;
                    }

                    Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(),
                            new Class[]{_interface}, new TimeIt(bean));
                    beans.set(i, proxy);
                    break;
                }
            }
        }
    }

    @VisibleForTesting
    static Class<?> isAnInterfaceMethod(Method method) {

        for(Class<?> clazz: method.getDeclaringClass().getInterfaces()){
            logger.info("Checking if {} has method {}", clazz, method.getName());
            try {
                if(clazz.getMethod(method.getName(), method.getParameterTypes()) != null){
                    logger.info("Yes it does!");
                    return clazz;
                }
            }
            catch (NoSuchMethodException e){}
        }
        return null;
    }

    // our beans in spring world get added to app context some way or the other
    // here just for the proxying example, we add it explictly
    public void addBean(Object bean){
        beans.add(bean);
    }

    public <T> T getBean(Class<T> type){
       // beans.stream().forEach(o -> System.out.println(o.getClass()));
       // beans.stream().forEach(o -> System.out.println(Arrays.toString(o.getClass().getInterfaces())));

        return (T) beans.stream().filter((o) ->
             o.getClass().equals(type) || (Proxy.isProxyClass(o.getClass()) &&
                    Arrays.stream(o.getClass().getInterfaces()).anyMatch(i -> i.equals(type))))
                .findFirst().get();
    }
}
