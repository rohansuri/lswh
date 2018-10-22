package prenio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

// use netcat rather than this
public class StdinClient {
    private static final Logger logger = LoggerFactory.getLogger(StdinClient.class);
    private static final String NEW_LINE = System.getProperty("line.separator");

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 9999);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();

        Scanner sc = new Scanner(System.in);

        // this is actually useless, calling close will not "wakeup" the thread blocked on
        // waiting for I/O on stdin
        closeResourcesShutdownHook(sc, socket);

        while(true){
            String readALine = sc.nextLine();

            byte[] lineBytes = readALine.getBytes();

            os.write(lineBytes);
            os.write(NEW_LINE.getBytes());
            os.flush();

            int bytesRead = is.read(lineBytes);

            if(bytesRead != lineBytes.length){
                logger.error("Unexpected server behavior! Isn't an echo server, difference of" +
                        "{} in bytes sent vs received. Closing client socket", Math.abs(bytesRead - lineBytes.length));
                return;
            }
            // we know we're interacting with a echo server
            // so, it'd return what we sent
            // hence we could safely use the same buffer which we used to send
            String serverMsg = new String(lineBytes, 0, bytesRead);
            logger.info("Server echoes {}", serverMsg);

        }

    }

    private static void closeResourcesShutdownHook(AutoCloseable... closeables){
        // can't do () -> {}
        // have to do new Thread(()->{}) since target type of lambda conversion must be an Interface
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            logger.info("Closing all resources");
            for(AutoCloseable closeable: closeables){
                try {
                    closeable.close();
                }
                catch (Exception e){
                    logger.error("Closing failed", e);
                }
            }
        }));
    }
}
/**
 * If the main thread is blocked on the stdin read, then the shutdown hook also blocks!
 * It blocks on the close, since the monitor is acquired by main
 * Hence we should maybe use NIO here too? for File?
 * that blocks for a read only when there really is something to read!
 * great :) learnt two NIO applications
 * There must be a way to interrupt the thread via another thread too...after all clean up should be supported
 *
 * Note: In the thread dumps, on the first line having the tid, nid, etc
 * The hex in the last is the Last Known Stack Pointer
 *
 * "Thread-0" #10 prio=5 os_prio=0 tid=0x00007f8dc4001800 nid=0x7db5 waiting for monitor entry [0x00007f8e08216000]
 *    java.lang.Thread.State: BLOCKED (on object monitor)
 * 	at sun.nio.cs.StreamDecoder.close(StreamDecoder.java:191)
 * 	- waiting to lock <0x000000078cea1b00> (a java.io.InputStreamReader)
 *
 * "main" #1 prio=5 os_prio=0 tid=0x00007f8e1c00d800 nid=0x7d8c runnable [0x00007f8e23192000]
 *    java.lang.Thread.State: RUNNABLE
 * 	at java.io.FileInputStream.readBytes(Native Method)
 * 	at java.io.FileInputStream.read(FileInputStream.java:255)
 * 	at java.io.BufferedInputStream.read1(BufferedInputStream.java:284)
 * 	at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
 * 	- locked <0x000000078c71db00> (a java.io.BufferedInputStream)
 * 	at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
 * 	at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
 * 	at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
 * 	- locked <0x000000078cea1b00> (a java.io.InputStreamReader)
 *
 */
