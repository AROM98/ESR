import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server_test implements Runnable{

    public int flag;
    public String ip;
    public int porta;

    //mudar args
    public Server_test(int flag, String ip, int porta){
        this.flag=flag;
        this.ip = ip;
        this.porta = porta;
    }

    public void run() {
        /**
         * Variaveis iniciais do servidor
         */
        ExecutorService pool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream input = null;
        PrintWriter out = null;
        InetAddress ip_origem = null;
        int porta_origem = 0;

        /**
         * Abertura do server-socket
         */
        try {
            serverSocket = new ServerSocket(porta);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * As seguintes linhas são a unica diferença entre esta classe e a clase Server
         * Apenas servem para cenários de TESTE de comunicação entre nodos
         * Este nodo irá iniciar
         */
        if(flag==1){
            Socket clientSocket = null;
            OutputStream out1=null;
            BufferedReader in;
            try {
            //System.out.println("vou abrir em "+ip+":"+porta);
            clientSocket = new Socket(ip, porta);
            //System.out.println("Abri cli-socket em "+ip+":"+porta);
            out1 = clientSocket.getOutputStream();

            /**
             *Converter a mensagem para byte[] e enviar
             */
            byte[] tmp = "COisas".getBytes();
            ByteMessages.sendBytes(tmp, out1);

            /**
            * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
            */
            //String resp = in.readLine();
            //System.out.println("resposta: "+resp);
            } catch (IOException e) {
            e.printStackTrace();
            }
        }


        while (true) {

            /**
             * servidor fica a espera de pedido
             * recebe input do stream
             */
            try {

                System.out.println("Server started");
                System.out.println("Waiting for a client ...");

                socket = serverSocket.accept();
                ip_origem = socket.getInetAddress(); // Guarda IP de origem
                porta_origem = 81;

                System.out.println("Client accepted from "+ ip_origem.toString());
                System.out.println(socket.getPort());

                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * Vou receber input em byte[]
             *  e como exemplo vou converter para string
             */
            try {
                byte[] tmp = ByteMessages.readBytes(input);
                String data = new String(tmp);
                System.out.println(">>> "+data);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /**
             * vou responder ao cliente
             * Então vou criar um thread da pool (Client) para responder
             */
            // NOVA CLIENT THREAD
            Thread_Cliente cli = new Thread_Cliente(ip_origem.toString(), porta_origem, "O servidor diz OLA");
            pool.execute(cli);
        }
    }

    /**
     * RECEBE COMO ARGUMENTO O IP DO DESTINO,
     * APENAS PORQUE IRÁ MANDAR UMA MENSAGEM (primeiro)
     */
    public static void main(String args[]){
        Server_test svr = new Server_test(1,args[0],81);
        svr.run();
    }
}
