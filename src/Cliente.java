import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente implements Runnable{


    public String ip;
    public int porta;

    //mudar args
    public Cliente(String ip, int porta){
        this.ip = ip;
        this.porta = porta;
    }

    public void run() {

        Socket clientSocket = null;
        PrintWriter out;
        BufferedReader in;
        String msg = "o cliente diz OLA";

        /**
         *  tenta fazer a ligação.
         */
        try {
            clientSocket = new Socket(ip, porta);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println(msg);
            String resp = in.readLine();

            System.out.println("resposta: "+resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]){
        Cliente cli = new Cliente("127.0.0.1", 6666);
        cli.run();
    }
}
