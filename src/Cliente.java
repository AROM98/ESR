import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente implements Runnable{

    //mudar args
    public Cliente(String ip){

    }

    public void run() {

        Socket clientSocket = null;
        PrintWriter out;
        BufferedReader in;
        String msg = "o servidor é um fdp";

        /**
         *  tenta fazer a ligação.
         */
        try {
            clientSocket = new Socket("127.0.0.1", 6666);
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
        Cliente cli = new Cliente("");
        cli.run();
    }
}
