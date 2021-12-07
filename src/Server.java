import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    public int porta;

    //mudar args
    public Server(int porta){
        this.porta = porta;
    }

    public void run() {
        /**
         * Variaveis iniciais do servidor
         */
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream input = null;
        PrintWriter out;

        while (true) {

            /**
             * Abertura do socket
             * servidor fica a espera de pedido
             * recebe input do stream
             */
            try {
                serverSocket = new ServerSocket(porta);
                System.out.println("Server started");
                System.out.println("Waiting for a client ...");

                socket = serverSocket.accept();

                System.out.println("Client accepted");

                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = "";


            /**
             * enquanto houver input na stream imprime.
             *  implementar o while para ler mais linhas...
             */
            while (line != null)
            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line != null) {
                System.out.println(line);
            }

            /**
             * vou responder ao cliente
             * Então vou criar um thread da pool (Client) para responder
             */
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("O servidor diz OLA");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        Server svr = new Server(6666);
        svr.run();
    }


}
