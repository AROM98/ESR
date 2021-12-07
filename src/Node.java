import java.io.IOException;
import java.security.NoSuchAlgorithmException;


/**
 * Um Node será um Server e um Cliente ao mesmo tempo.
 * não pode é receber as suas proprias mensagens xd....
 */
public class Node {


    public static void main(String args[]) throws IOException, NoSuchAlgorithmException {

/*
        for (vizinho: args) {
            //trata dos vizinhos
        }*/


        Cliente cliente = new Cliente("127.0.0.1", 6666); // recebe ip (destino) e porta
        Server server = new Server(6666); // recebe apenas uma porta

        new Thread(cliente).start();
        new Thread(server).start();

    }
}
