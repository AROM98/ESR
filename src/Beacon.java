import java.io.*;
import java.net.Socket;

public class Beacon implements Runnable{
    private String bootstrapper;
    private String nodeIp;
    private int porta;


    public Beacon(String bootstrapper, String nodeIp, int porta){
        this.bootstrapper = bootstrapper;
        this.nodeIp = nodeIp;
        this.porta = porta;
    }

    public void run(){

        Socket clientSocket;
        OutputStream out;

        try {
            System.out.println("Vou abrir em " + bootstrapper + ":" + porta);
            clientSocket = new Socket(bootstrapper,porta);
            System.out.println("Abri cli-socket em " + bootstrapper + ":" + porta);

            out = clientSocket.getOutputStream();

            String msg = nodeIp + " " + "0"; //flag 0 ping
            byte[] tmp = msg.getBytes();
            ByteMessages.sendBytes(tmp, out);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
