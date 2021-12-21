//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Um Node será um Server e um Cliente ao mesmo tempo.
 * não pode é receber as suas proprias mensagens...
 */
public class Node {


    public static void main(String[] args) throws IOException{

        String server = "127.0.0.2";
        String nodeIp = InetAddress.getLocalHost().getHostAddress();
        int porta = 81;

        //BEACON
        Thread beaconThread = new Thread(new Beacon(server,nodeIp,porta));
        beaconThread.start();

        //threads para poder enviar e receber para outros nodos

        if(args.length != 1){
            System.out.println("Insira o ip do server nos argumentos"); //e o proprio ip do node");
        }
        else{
            server = args[0];

            System.out.println("O server é: " + server);
            System.out.println("O ip do Node é: " + nodeIp);


            /**
             * Pedido de um nodo ao servidor para receber a stream
             */
            System.out.println("Quando quiser receber a stream escreva Y");
            Scanner sc = new Scanner(System.in);
            String str = sc.nextLine();
            if(str.equals("Y")) {

                System.out.println("Mandando pedido ao servidor para receber a stream...");

                Socket clientSocket = null;
                OutputStream out;
                BufferedReader in;
                InputStream input = null;

                try {
                    System.out.println("Vou abrir em " + server + ":" + porta);
                    clientSocket = new Socket(server,porta);
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
                    //receber o resto da stream

                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                //metodos do vlc para reproduzir a stream

            }
        }
    }
}
