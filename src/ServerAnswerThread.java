import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerAnswerThread implements Runnable{


    private Bootstrapper bootstrapper;
    private byte[] dados;
    private int porta;
    private int portaNode;
    private HashMap<Integer, Integer> portas;
    private HashMap<String, Integer> ficheiros;

    /**
     * Recebe argumentos inicias para conseguir responder
     */
    public ServerAnswerThread(Bootstrapper b, byte[] dados, HashMap<Integer, Integer> portas, HashMap<String, Integer> ficheiros){
        this.porta = 6868;
        this.portaNode = 6869;
        this.bootstrapper = b;
        this.dados = dados;
        this.portas = portas;
        this.ficheiros = ficheiros;
    }

    public void run() {

        int flag;
        String ipNode;
        String[] res;
        String data = new String(dados);
        res = data.split(" ");
        flag = Integer.parseInt(res[0]);

        switch (flag){
            case 0 :
                // 0-> beacons (nodo -> servidor)
                // criar tabela com timestamps ? mas isto teria que ser mantido por uma thread, não?
                //atualizar coisas no bootstrap, se não estou enganado!
                ipNode = res[1];
                bootstrapper.addIPativo(ipNode);


                break;
            case 1:

                //1-> pede stream (nodo(cliente) -> servidor)
                //tenho que iniciar a stream para esse cliente, para isso tenho que usar o bootstrapper para
                //saber qual o caminho

                String ficheiro = res[1];
                ipNode = res[2];

                Socket clientSocket = null;
                OutputStream out = null;
                try {
                    clientSocket = new Socket(ipNode, portaNode);
                    out = clientSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //buscar porta disponivel e meter na portaStream
                int port = Server.GetAvailablePort(portas);
                String portaStream = Integer.toString(port); //porta do upd;
                byte[] tmp = portaStream.getBytes();
                try {
                    ByteMessages.sendBytes(tmp, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //mandar a mensagem aos nodos intermediarios
                ArrayList<String> trajeto = new ArrayList<>(bootstrapper.wantToSendFile(ficheiros.get(ficheiro),ipNode)); //trajeto do servidor ao cliente
                //trajeto.add("10.0.4.1");
                //trajeto.add("10.0.18.1");
                //trajeto.add(ipNode);

                for (String t: trajeto
                     ) {
                    System.out.println("NODO: " + t);
                }

                for (int i = 0; i < trajeto.size() - 1; i++){
                    try {
                        clientSocket = new Socket(trajeto.get(i), portaNode);
                        out = clientSocket.getOutputStream();

                        String tmpV = portaStream + " " + trajeto.get(i+1);
                        tmp = tmpV.getBytes();
                        ByteMessages.sendBytes(tmp, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                StreamSender s = new StreamSender(ficheiro,trajeto.get(0),Integer.parseInt(portaStream));
                trajeto.clear();

                break;

            case 4:
                //acabar ligação (...) (ambos sentidos)

                break;
            case 5:
                int port_to_close = Integer.parseInt(res[1]);
                portas.put(port_to_close, 0); // porta passa a estar desactivada
                System.out.println("Desbloquei a porta: " + port_to_close);
                break;

            case 6:
                //6-> nodo apaga ficheiro da cache (nodo -> servidor)
                // vou atualizar informação no bootstrapper --
                // "o que vou querer é o IP de quem apagou da cache e o id do file"

                break;

            default:
                // se chegou aqui é porque se calhar deu erro no parse.
                // ou não veio alguma destas flags
                System.out.println("alguma coisa correu mal.");
        }
    }
}
