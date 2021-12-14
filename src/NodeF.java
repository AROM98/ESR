//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeF {

    public static void main(String args[]) throws UnknownHostException {

        String bootstrapper = "127.0.0.2";
        String nodeIp = InetAddress.getLocalHost().getHostAddress();
        int porta = 81;

        /**
         * Sequência inicil do node
         * cria uma thread de beacons para avisar que está vivo
         */
        Thread beaconThread = new Thread(new Beacon(bootstrapper,nodeIp,porta));
        beaconThread.start();


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

        /**
         * servidor fica a espera de pedido permanentemente
         * recebe input do stream
         */
        while (true) {

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
            String data = "";
            try {
                byte[] tmp = ByteMessages.readBytes(input);
                data = new String(tmp);
                System.out.println(">>> "+data);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * Se a mensagem do cliente for "Y" então tenho que avisar
             * o servidor que o cliente quer começar uma stram.
             * Caso contrário irá apenas responder ao cliente.
             *
             *
             * o cliente deve enviar juntamente com Y o
             * nome do ficheiro que pretende obter o stream.
             */
            if(data.equals("Y")){
                String start_stream_message = "start stream "+ip_origem;
                Cliente cli = new Cliente(bootstrapper, porta, start_stream_message);
                pool.execute(cli);
            }
            else{
                /**
                 * vou responder ao pedido
                 * Então vou criar um thread da pool (Client) para responder
                 */
                // NOVA CLIENT THREAD
                Cliente cli = new Cliente(ip_origem.toString(), porta, "O servidor diz OLA");
                pool.execute(cli);
            }


        }
    }
}
