import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Node {

    public static void main(String args[]) throws UnknownHostException {

        String server = "";
        String nodeIp = args[2];
        int porta = 6868;
        int portaNode = 6869;
        int portaBeacon = 6870;
        int receber_input;


        if (args.length != 3) {
            System.out.println("Insira o ip do server e flag nodo/cliente nos argumentos");
        } else {

            /**
             * Sequência inicial do node
             * cria uma thread de beacons para avisar que está vivo
             */
         /*   Thread beaconThread = new Thread(new Beacon(server, nodeIp, portaBeacon));
            beaconThread.start();*/


            /**
             * Variaveis iniciais do servidor
             */
            ServerSocket serverSocket;
            Socket socket;
            InputStream input = null;
            InetAddress ip_origem;


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
             */

            if (receber_input == 1) {
                System.out.println("Quando quiser receber a stream insira o nome do ficheiro:");
                Scanner sc = new Scanner(System.in);
                String str = sc.nextLine();
                System.out.println("Mandando pedido ao servidor para receber a stream...");

                Socket clientSocket;
                OutputStream out;

                try {
                    System.out.println("Vou abrir em " + server + ":" + porta);
                    clientSocket = new Socket(server, porta);
                    System.out.println("Abri cli-socket em " + server + ":" + porta);

                    out = clientSocket.getOutputStream();

                    /**
                     * Envia request de stream
                     */
                    String msg = "1 " + str + " " + nodeIp;
                    byte[] tmp = msg.getBytes();
                    ByteMessages.sendBytes(tmp, out);

                    /**
                     * Recebe confirmaçao do servidor de onde vai receber a stream
                     */

                    serverSocket = new ServerSocket(portaNode);
                    socket = serverSocket.accept();

                    input = socket.getInputStream();

                    tmp = ByteMessages.readBytes(input);
                    String portaStream = new String(tmp);
                    System.out.println("Porta que vou escutar: " + portaStream + "\n");

                    /**
                     * Inicia a classe que recebe os pacotes da stream pelo UDP e ao mesmo tempo reproduz a stream.
                     */
                    VideoPlayer_Client t = new VideoPlayer_Client(Integer.parseInt(portaStream));


                    //pacote de reabrir a porta
                    System.out.println("Vou abrir em " + server + ":" + porta);
                    clientSocket = new Socket(server, porta);
                    System.out.println("Abri cli-socket em " + server + ":" + porta);

                    out = clientSocket.getOutputStream();
                    msg = "5 " + portaStream;
                    tmp = msg.getBytes();
                    ByteMessages.sendBytes(tmp, out);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                while (true) {
                    try {
                        System.out.println("À espera de informação do servidor...");

                        serverSocket = new ServerSocket(portaNode);
                        socket = serverSocket.accept();
                        ip_origem = socket.getInetAddress();

                        System.out.println("Client accepted from " + ip_origem.toString());

                        input = socket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String data;
                    String[] res = new String[0];
                    try {
                        byte[] tmp = ByteMessages.readBytes(input);
                        data = new String(tmp);
                        res = data.split(" "); //res[0] é a porta para receber a stream e res[1] é o ip para onde mando a stream
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String portaStream = res[0];
                    String ipNodoVizinho = res[1];

                    NodeStream ns = new NodeStream(ipNodoVizinho, Integer.parseInt(portaStream));

                }
            }
        }
    }
}