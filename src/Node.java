//cd /../../../home/falape/Projetos/ESR/src/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.net.InetAddress;



/**
 * Um Node será um Server e um Cliente ao mesmo tempo.
 * não pode é receber as suas proprias mensagens xd....
 */
public class Node {


    public static void main(String[] args) throws IOException{

        String bootstrapper = "";
        String nodeIp = InetAddress.getLocalHost().getHostAddress();
        int porta = 6666;

        Beacon beacon = new Beacon(bootstrapper,nodeIp,porta);
        //BEACONS

        //threads para poder enviar e receber para outros nodos

        if(args.length != 1){
            System.out.println("Insira o ip do bootstrapper nos argumentos"); //e o proprio ip do node");
        }
        else{
            bootstrapper = args[0];
            //nodeIp = args[1];

            System.out.println("O bootstrapper é: " + bootstrapper);
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
                PrintWriter out;
                BufferedReader in;

                try {
                    System.out.println("Vou abrir em " + bootstrapper + ":" + porta);
                    clientSocket = new Socket(bootstrapper,porta);
                    System.out.println("Abri cli-socket em " + bootstrapper + ":" + porta);

                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    out.println(nodeIp + " " + 1); //flag 1 request da stream

                    /**
                     * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
                     */

                    String resp = in.readLine();

                    //confirmaçao do servidor que vai mandar a stream pelo nodo X
                    System.out.println("resposta: " + resp);

                    String nodeVizinho = resp;

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
