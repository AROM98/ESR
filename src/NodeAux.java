import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class NodeAux {

    String server;
    String nodeIp;
    int porta = 6868;
    int portaNode = 6869;

    public NodeAux(String server, String nodeIp){
        this.server = server;
        this.nodeIp = nodeIp;
    }

    public void NodeInput() {


        Socket socket;
        InputStream input;

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

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(portaNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    }

    public void NodeRelay() {

        Socket socket;
        InputStream input = null;
        InetAddress ip_origem;

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(portaNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                System.out.println("À espera de informação do servidor...");

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
