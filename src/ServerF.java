import com.sun.jdi.Bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Servidor principal onde estão hospedados os ficheiros
 * o stream será feito apartir daqui inicialmente.
 *
 *
 *
 * coisas que esta classe deve poder fazer:
 * -> quando inicia lê um txt com os nomes do ficheiros / paths
 * -> esta informação é guardada numa lista (?)
 * -> deve poder receber pedidos externos de nodes.
 * -> deve poder responder adequadamente a estes pedidos
 * -> e mais importante que tudo, deve conseguir fazer stream
 */
public class ServerF {


    /**
     * lê ficheiro, faz parse e guarda informação no array
     */
    public static void readNstore(String filename, ArrayList<String> res){
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
                res.add(data);
                System.out.println(data);
            }
        }
        else{
            System.out.println("não encontrei o ficheiro");
        }
    }


    /**
     * Recebe como argumento o nome do ficheiro txt
     *
     * Criar um hashMAP <String , id>
     *     string -> nome do file
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
     *   3-> id ficheiro, num chunk, resto dos bytes (ambos sentidos)
     *   4 -> acabar ligação (...) (ambos sentidos)
     *   5 -> (no cliente) termina ligação com porta do servidor (nodo(cliente) -> servidor)
     *   6-> nodo apaga ficheiro da cache (nodo -> servidor)
     *
     *
     *   portas 70000 -> +++
     *   não voltar a utilizar portas (!!!) ate voltar a receber 5.
     *   fazer gestão de portas basicamente.
     *
     */
    public static void main(String[] args) throws IOException {
        String path = "/files/";

        String path2 = "/../Rede.json";
        String path3 = "/../Rede1.json";

        ArrayList<String> ficheiros = new ArrayList<>();

        if(args[0] != null){
            System.out.println("vou ler ficheiro");
            readNstore(args[0], ficheiros);
        }
        else{
            System.out.println("dar como input o ficheiro!");
        }


        Bootstrap bootstrapper = new Bootstrap(path3); //remendar depois de alterar o nome.

        /**
         * inicialização feita
         * agora iniciar servidor
         */

    }
}
