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
     */
    public static void main(String[] args) throws IOException {
        String path = "/files/";
        ArrayList<String> ficheiros = new ArrayList<>();

        if(args[0] != null){
            System.out.println("vou ler ficheiro");
            readNstore(args[0], ficheiros);

        }
        else{
            System.out.println("dar como input o ficheiro!");
        }


        /**
         * inicialização feita
         * agora iniciar servidor
         */

    }
}
