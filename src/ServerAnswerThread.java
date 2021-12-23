import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ServerAnswerThread implements Runnable{


    private Bootstrapper bootstrapper;
    private byte[] dados;
    private int porta;
    private int portaNode;
    private ReentrantLock lock;
    private HashMap<Integer, Integer> portas;

    /**
     * Recebe argumentos inicias para conseguir responder
     */
    public ServerAnswerThread(Bootstrapper b, byte[] dados, HashMap<Integer, Integer> portas){
        this.porta = 6868;
        this.portaNode = 6869;
        this.bootstrapper = b;
        this.dados = dados;
        this.portas = portas;
    }

    public void run() {

        int flag;
        String ipNode;
        String[] res;
        String data = new String(dados);
        res = data.split(" ");
        flag = Integer.parseInt(res[0]);
        //System.out.println("flag -> " + flag); //devo obter a flag aqui

        switch (flag){
            case 0 :
                // 0-> beacons (nodo -> servidor)
                // criar tabela com timestamps ? mas isto teria que ser mantido por uma thread, não?
                //atualizar coisas no bootstrap, se não estou enganado!
                ipNode = res[1];



                break;
            case 1:

                //1-> pede stream (nodo(cliente) -> servidor)
                //tenho que iniciar a stream para esse cliente, para isso tenho que usar o bootstrapper para
                //saber qual o caminho
                //ao criar o stream para uma porta privada, não esquecer de verificar o seu status!!
                // neste caso enviar pacote com a porta da stream

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
/*
                //mandar a mensagem aos nodos intermediarios
                ArrayList<String> trajeto = new ArrayList<>(); //trajeto do servidor ao cliente
                trajeto.add("10.0.18.1");
                for (String ipNodeInter : trajeto) {

                    try {
                        clientSocket = new Socket(ipNodeInter, porta);
                        out = clientSocket.getOutputStream();

                        String tmpV = portaStream + " " + ipNodeInter;
                        tmp = tmpV.getBytes();
                        ByteMessages.sendBytes(tmp, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
                StreamSender s = new StreamSender(ficheiro,ipNode,Integer.parseInt(portaStream));

                System.out.println("Acabou a stream");
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case 4:
                //acabar ligação (...) (ambos sentidos)

                break;
            case 5:
                lock.lock();
                int port_to_close = Integer.parseInt(res[1]);
                portas.put(port_to_close, 0); // porta passa a estar desactivada
                System.out.println("Desbloquei a porta: " + port_to_close);
                lock.unlock();
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
