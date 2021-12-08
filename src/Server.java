import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream input = null;
        PrintWriter out;
        InetAddress ip_origem = null;
        int porta_origem = 0;

        /**
         * Abertura do server-socket
         */
        try {
            serverSocket = new ServerSocket(porta);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            /**
             * servidor fica a espera de pedido
             * recebe input do stream
             */
            try {

                System.out.println("Server started");
                System.out.println("Waiting for a client ...");

                socket = serverSocket.accept();
                ip_origem = socket.getInetAddress(); // Guarda IP de origem
                porta_origem = socket.getPort();

                System.out.println("Client accepted from "+ ip_origem.toString());
                System.out.println(socket.getPort());

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
            //while (line != null)
            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line != null) {
                System.out.println(">> "+line);
            }

            /**
             * vou responder ao cliente
             * Então vou criar um thread da pool (Client) para responder
             */
            // NOVA CLIENT THREAD
            Cliente cli = new Cliente(ip_origem.toString(), porta_origem, "O servidor diz OLA");
            pool.execute(cli);
            //out = new PrintWriter(socket.getOutputStream(), true);
            //out.println("O servidor diz OLA");
        }
    }

    public static void main(String args[]){
        Server svr = new Server(6666);
        svr.run();
    }


}
