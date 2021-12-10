import java.io.PrintWriter;
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

        Socket clientSocket = null;
        PrintWriter out;

        try {
            System.out.println("Vou abrir em " + bootstrapper + ":" + porta);
            clientSocket = new Socket(bootstrapper,porta);
            System.out.println("Abri cli-socket em " + bootstrapper + ":" + porta);

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            while (true) {
                out.println(nodeIp + " " + 0); //flag 0 ping ao servidor
                Thread.sleep(1000);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        //beaconSender bb = new beaconSender();
        //bb.run();
    }
}
