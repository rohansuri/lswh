package prenio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StartServer {

    private static final Logger logger = LoggerFactory.getLogger(StartServer.class);

    public static void main(String[] args) {
        logger.info("Passed argument {}", args[0]);
        try {
            Server server = new Server(Integer.parseInt(args[0]));
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        }
        catch (IOException e){
            logger.error("Couldn't start server because of", e);
        }
    }
}
