package prenio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerSocket serverSocket;

    private final ExecutorService service;

    public Server(int port) throws IOException {
        logger.info("Creating server on port {}", port);

        serverSocket = new ServerSocket(port);
        service = Executors.newCachedThreadPool();
        // try and use work stealing pool?
    }

    public void start(){
        logger.info("Starting server");
        service.submit(this);
    }

    @Override
    public void run(){
        logger.info("Started Acceptor Thread");
        while(true){
            // will the returned socket here be auto closed? or will the server socket be?
            // try(Socket socket = serverSocket.accept())
            // surely the returned socket at least
            // that is what was immediately closing my socket, right upon acceptance

            try
            {
                Socket socket = serverSocket.accept();
                logger.info("New socket accepted from {}", socket.getRemoteSocketAddress());
                service.submit(new EchoHandler(socket));
            }
            catch (SocketException e){
                logger.info("Closed called, will stop acceptor thread");
                break;
            }
            catch (IOException e){
                logger.error("Acceptor failed. Will close server because of", e);
                close();
                break;
            }

        }
    }

    public void close(){
        logger.info("Closing server");
        service.shutdown();
        try {
            serverSocket.close(); // will cause the blocking accept to throw SocketException
            logger.info("Server closed");
        }
        catch (IOException e){
            // nothing we can do
            logger.error("Closing server socket failed because of", e);
        }
    }

}
