package jdk.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeMeImpl implements TimeMe {
    private static final Logger logger = LoggerFactory.getLogger(TimeMeImpl.class);
    @Time
    @Override
    public void timeMe() throws InterruptedException {

        logger.info("Inside timeMe impl, will sleep for 1 second");
        Thread.sleep(1_000);
        logger.info("Sleeping over");
    }
}
