import java.io.*;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class Beacon implements Runnable{
    private String server;
    private String nodeIp;
    private int porta;


    public Beacon(String server, String nodeIp, int porta){
        this.server = server;
        this.nodeIp = nodeIp;
        this.porta = porta;
    }

    public void run(){

        Socket clientSocket;
        OutputStream out;

        while(true) {

            try {
                clientSocket = new Socket(server, porta);
                System.out.println("Beacon signal " + server + ":" + porta);

                out = clientSocket.getOutputStream();

                String msg = "0 " + nodeIp; //flag 0 ping
                byte[] tmp = msg.getBytes();
                ByteMessages.sendBytes(tmp, out);

                sleep(10000);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
