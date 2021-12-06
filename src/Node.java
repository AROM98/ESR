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

        String vizinho1 = "10.0.0.1";

        Cliente cliente = new Cliente("");
        Server server = new Server("");

        new Thread(cliente).start();
        new Thread(server).start();

    }
}
