import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
             */
            String data = "";
            int flag = -1;
            String[] res = new String[0];
            try {
                byte[] tmp = ByteMessages.readBytes(input);
                data = new String(tmp);
                res = data.split(" ");
                flag = Integer.parseInt(res[0]);
                System.out.println("flag -> " + flag); //devo obter a flag aqui
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * Agora o que acontece depende da flag
             * são criadas threads da pool para responder a cada uma destas, caso seja necessário
             */


            if(flag == 0){
                // 0-> beacons (nodo -> servidor)
                // criar tabela com timestamps ? mas isto teria que ser mantido por uma thread, não?

                //atualizar coisas no bootstrap, se não estou enganado!

            }
            else if(flag == 1){
                //1-> pede stream (nodo(cliente) -> servidor)
                //tenho que iniciar a stream para esse cliente, para isso tenho que usar o bootstrapper para
                //saber qual o caminho
                //ao criar o stream para uma porta privada, não esquecer de verificar o seu status!!
                // neste caso enviar pacotes com a flag 2

                try {
                    //transforma o video em VideoStream
                    VideoStream video = new VideoStream("movie.Mjpeg");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String nome_file = res[1]; // se a ordem enviada for flag ++ fich


                //ip do proximo nodo, certo?
                Cliente cli = new Cliente("10.1.1.1", porta, "2 "+ nome_file);
                pool.execute(cli);
            }


            else if(flag == 3){
                //3-> id ficheiro, num chunk, bytes (ambos sentidos)

            }

            else if(flag == 4){
                //acabar ligação (...) (ambos sentidos)

            }

            else if(flag == 5){
                //5 -> (no cliente) termina ligação com porta do servidor (nodo(cliente) -> servidor)
                // esta porta vem inidicada no pacote? porque a porta que encontro aqui é a 81...
                // e não a privada que pretendo fechar..
                int port_to_close = 70420; //temporário...
                portas.put(port_to_close, 0); // porta passa a estar desactivada
            }

            else if(flag == 6){
                //6-> nodo apaga ficheiro da cache (nodo -> servidor)
                // vou atualizar informação no bootstrapper --
                // "o que vou querer é o IP de quem apagou da cache e o id do file"



            }

            else{
                // se chegou aqui é porque se calhar deu erro no parse.
                // ou não veio alguma destas flags
                System.out.println("alguma coisa correu mal.");
            }


        }




    }
}
