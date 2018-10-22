package prenio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

// even for this simple example, is it a good idea to have a separate reader/writer task?

/**
 * Single line echo-er
 */
public class EchoHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(EchoHandler.class);

    private final Socket socket;

    public EchoHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        logger.info("Running EchoHandler for socket {}", socket.getRemoteSocketAddress());
        // isr/osr converts a byte stream to character stream
        // we expect echo messages in string (default charset)

        /*
        logger.info("Socket closed? {}", socket.isClosed());
        logger.info("Socket connected? {}", socket.isConnected());
        // use less to check this ^^ isClosed, isConnected do not return live/current state
        // it's just a -- has the socket ever been
        // state management has to be done in the app itself! by using read/write
        */

        // closing reader/writer would chain the closes to ultimately close the socket too
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){

            while(true){
                String read = reader.readLine();
                if(read == null){ // EOS
                    break;
                }

                logger.info("Server received \"{}\", will echo it back", read);

                writer.write(read);
                writer.flush(); // flush anything buffered immediately
            }
        }
        catch (IOException e){
            // either read/write failed
            // or maybe the close failed
            logger.error("", e);
        }
    }
}
