import java.io.*;
import java.net.Socket;

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

        try {
            System.out.println("Vou abrir em " + server + ":" + porta);
            clientSocket = new Socket(server,porta);
            System.out.println("Abri cli-socket em " + server + ":" + porta);

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
