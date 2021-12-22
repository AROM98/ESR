//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NodeF {

    public static void main(String args[]) throws UnknownHostException {

        String server = "";
        String nodeIp = InetAddress.getLocalHost().getHostAddress();
        int porta = 81;
        int receber_input = 0;


        if(args.length != 2){
            System.out.println("Insira o ip do server e flag nodo/cliente nos argumentos"); //e o proprio ip do node");
        }
        else{

            /**
             * Sequência inicial do node
             * cria uma thread de beacons para avisar que está vivo
             */
            Thread beaconThread = new Thread(new Beacon(server,nodeIp,porta));
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

            server = args[0];
            receber_input = Integer.parseInt(args[1]);

            System.out.println("O server é: " + server);
            System.out.println("O ip do Node é: " + nodeIp);

            /**
            * servidor fica a espera de pedido permanentemente
            * recebe input ecrito e do stream
             *
             *
             *
             * VLC_player_thread
            */

            while (true) {

                if(receber_input == 1){
                    System.out.println("Quando quiser receber a stream escreva Y");
                    Scanner sc = new Scanner(System.in);
                    String str = sc.nextLine();
                    if(str.equals("Y")) {


                        System.out.println("Mandando pedido ao servidor para receber a stream...");

                        Socket clientSocket;
                        OutputStream out;


                        try {
                            System.out.println("Vou abrir em " + server + ":" + porta);
                            clientSocket = new Socket(server, porta);
                            System.out.println("Abri cli-socket em " + server + ":" + porta);

                            out = clientSocket.getOutputStream();
                            input = clientSocket.getInputStream();

                            /**
                             * Envia

                             */
                            String msg = nodeIp + " " + "1";
                            byte[] tmp = msg.getBytes();
                            ByteMessages.sendBytes(tmp, out);

                            /**
                             * Recebe
                             */
                            tmp = ByteMessages.readBytes(input);
                            String nodeVizinho = new String(tmp);
                            System.out.println("nodo Vizinho: " + nodeVizinho);


                            //abrir novo cliente agora a escutar do vizinho
                            /**
                             *   * id ficheiro
                             *     num chunk
                             *     flag
                             */
                            //receber o resto da stream

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //metodos do vlc para reproduzir a stream
                    }

                }else {

                    try {

                        System.out.println("Server started");
                        System.out.println("Waiting for a client... ");

                        socket = serverSocket.accept();
                        ip_origem = socket.getInetAddress(); // Guarda IP de origem do cliente

                        System.out.println("Client accepted from " + ip_origem.toString());

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
                        System.out.println(">>> " + data);
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
                    if (data.equals("Y")) {
                        String start_stream_message = "1 "+ip_origem;
                        Cliente cli = new Cliente(server, porta, start_stream_message);
                        pool.execute(cli);
                    } else {
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
    }
}
