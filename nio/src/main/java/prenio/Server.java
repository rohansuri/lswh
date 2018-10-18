package prenio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerSocket serverSocket;
    private volatile boolean stop;

    private final ExecutorService service;

    public Server(int port) throws IOException {
        logger.info("Creating server on port {}", port);

        serverSocket = new ServerSocket(port);
        service = Executors.newCachedThreadPool();
        // try and use work stealing pool?
    }

    public void start(){
        logger.info("Starting server...");
        service.submit(this);
    }

    @Override
    public void run(){

        while(!stop){
            // will the returned socket here be auto closed? or will the server socket be?
            try(Socket socket = serverSocket.accept()){
                logger.info("New socket accepted from {}", socket.getRemoteSocketAddress());
                service.submit(new EchoHandler(socket));
            }
            catch (IOException e){
                logger.error("Closing server because of", e);
                stop = true;
            }
        }
        close();
    }

    private void close(){
        service.shutdown();
        try {
            serverSocket.close();
        }
        catch (IOException e){
            // nothing we can do

        }
    }

    // expose via JMX? or atleast add shutdown handler?
    public void stop(){
        stop = true;
    }
}
