//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor onde estão alocados os ficheiros para os clientes.
 */
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
        Socket socket;
        InputStream input = null;
        InetAddress ip_origem = null;

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

                System.out.println("Client accepted from "+ ip_origem.toString());

                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * Vou receber input em byte[]
             *  e como exemplo vou converter para string
             */
            try {
                byte[] tmp = ByteMessages.readBytes(input);
                String data = new String(tmp);
                System.out.println(">>> "+data);
            } catch (IOException e) {
                e.printStackTrace();
            }


            /**
             * vou responder ao cliente
             * Então vou criar um thread da pool (Client) para responder
             */
            // NOVA CLIENT THREAD
            Cliente cli = new Cliente(ip_origem.toString(), porta, "O servidor diz OLA");
            pool.execute(cli);
        }
    }


    public static void main(String args[]){
        Server svr = new Server(81);
        svr.run();
    }
}
