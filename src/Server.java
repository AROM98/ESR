//cd /../../../home/falape/Projetos/ESR/src/
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
                byte[] tmp = readBytes(input);
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
            Thread_Cliente cli = new Thread_Cliente(ip_origem.toString(), porta, "O servidor diz OLA");
            pool.execute(cli);
        }
    }



    public byte[] readBytes(InputStream in) throws IOException {
        // Again, probably better to store these objects references in the support class
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }

    public static void main(String args[]){
        Server svr = new Server(81);
        svr.run();
    }


}
