import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor principal onde estão hospedados os ficheiros
 * o stream será feito apartir daqui inicialmente.
 *
 *
 *
 * coisas que esta classe deve poder fazer:
 * -> quando inicia lê um txt com os nomes do ficheiros / paths  ---- DONE
 * -> esta informação é guardada numa hashmap<nome_fich, id> ---- DONE
 * -> deve poder receber pedidos externos de nodes. ---- DONE
 * -> deve poder responder adequadamente a estes pedidos
 * -> e mais importante que tudo, deve conseguir fazer stream
 */
public class ServerF {


    /**
     * Metedo para popular um hashmap de portas disponiveis com o seu devido estado.
     */
    public static void PopulatePorts(HashMap<Integer, Integer> portas){
        int min_port = 70000;

        //encher com 30k portas disponiveis.
        for(int i = 0; i < 30000; i++){
            portas.put(min_port, 0);
            min_port++;
        }
    }

    /**
     * devolve como Integer a porta disponivel e atualiza o hashmap
     * para que esta porta fique como "ocupada"
     */
    public static Integer GetAvailablePort(HashMap<Integer, Integer> portas){
        Integer res_port = -1;

        for (Integer port : portas.keySet()){
            if (portas.get(port) == 0){
                res_port = port;
                portas.put(port, 1); // atualiza para ocupado
                break;
            }
        }

        return res_port;
    }

    /**
     * lê ficheiro, faz parse e guarda informação no hashmap
     */
    public static void readNstore(String filename, HashMap<String, Integer> res, Integer id){
        File myObj = new File(filename);
        Scanner myReader = null;
        System.out.println(filename);
        if(myObj.exists()){
            System.out.println("encontrei o ficheiro");
            try {
                myReader = new Scanner(myObj);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                res.put(data, id);
                id++;
                System.out.println(data);
            }
        }
        else{
            System.out.println("não encontrei o ficheiro");
        }
    }


    /**
     * Recebe como argumento o nome do ficheiro txt ------ DONE
     *
     * Criar um hashMAP <String , id>
     *     string -> nome do file ----------- DONE
     *     id -> integer
     *
     *
     * pool threads
     *
     *
     * flags
     *   0-> beacons (nodo -> servidor)
     *   1-> pede stream (nodo(cliente) -> servidor)
     *   2-> vizinho de quem recebe, vizinho para quem mandar stream, porta, id ficheiro (ambos sentidos)
     *   3-> id ficheiro, num chunk, bytes (ambos sentidos)
     *   4 -> acabar ligação (...) (ambos sentidos)
     *   5 -> (no cliente) termina ligação com porta do servidor (nodo(cliente) -> servidor)
     *   6-> nodo apaga ficheiro da cache (nodo -> servidor)
     *
     *
     *   portas 70000 -> +++
     *   não voltar a utilizar portas (!!!) ate voltar a receber 5.
     *   fazer gestão de portas basicamente. ---- penso que está OK
     *
     */
    public static void main(String[] args) throws IOException {
        int porta = 81;
        String path = "/files/";
        String path2 = "/../Rede.json";
        String path3 = "Rede1.json";

        Integer porta_counter = 0;
        Integer file_id = 0;

        HashMap<String, Integer> ficheiros = new HashMap<>();
        HashMap<Integer, Integer> portas = new HashMap<>();
        PopulatePorts(portas);

        if(args[0] != null){
            System.out.println("vou ler ficheiro");
            readNstore(args[0], ficheiros, file_id);
        }
        else{
            System.out.println("dar como input o ficheiro!");
        }

        //Bootstrap bootstrapper = new Bootstrap(path3); //remendar depois de alterar o nome.

        /**
         * inicialização feita
         * agora iniciar servidor
         */
        Bootstrapper bootstrapper = new Bootstrapper(path3);
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = null;
        Socket socket;
        InputStream input = null;
        InetAddress ip_origem = null;
        Socket clientSocket = null;
        OutputStream out;


        /**
         * Abertura do server-socket
         */
        try {
            serverSocket = new ServerSocket(porta);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //System.out.println();
        //System.out.println(bootstrapper.getNodos());

        while (true){
            try {
                System.out.println("Server started");
                System.out.println("Waiting for a new connection... ");

                socket = serverSocket.accept();
                ip_origem = socket.getInetAddress(); // Guarda IP de origem do cliente

                System.out.println("Client accepted from " + ip_origem.toString());

                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


            /**
             * Vou receber input em byte[]
             * estou a espera de alguma coisa do genero: flag ++ ip_nodo ++ dados (dividido por ' ')
             * vou converter os byte[] para string e fazer parse por ' ' para obter a flag
             *
             * isto se calhar vai mudar com a implementação dos pacotes customizados
             * o que muda é como retiro e flag do dito cujo, a utilização da flag nao muda
             *
             * ---- 0 ---
             *  flag
             * -----1-----
             * flag + fich + ip
             *------5-----
             * flag + porta_fechar
             *

             */

            byte[] tmp = ByteMessages.readBytes(input);
            ServerAnswerThread thread_tmp = new ServerAnswerThread(bootstrapper, tmp, portas);
            pool.execute(thread_tmp);

        }
    }
}
