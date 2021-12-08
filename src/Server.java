//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    public int flag;
    public String ip;
    public int porta;

    //mudar args
    public Server(int flag, String ip, int porta){
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

        if(flag==1){
            Socket clientSocket = null;
            PrintWriter out1=null;
            BufferedReader in;
            try {
                System.out.println("vou abrir em "+ip+":"+porta);
                clientSocket = new Socket(ip, porta);
                System.out.println("Abri cli-socket em "+ip+":"+porta);
                out1 = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out1.println("olá diz o cliente");

                /**
                 * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
                 */
                //String resp = in.readLine();
                //System.out.println("resposta: "+resp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out1.println("olá diz o cliente");
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = "";


            /**
             * enquanto houver input na stream imprime.
             *  implementar o while para ler mais linhas...
             */
            //while (line != null)
            try {
                line = reader.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line != null) {
                System.out.println(">> "+line);
            }

            /**
             * vou responder ao cliente
             * Então vou criar um thread da pool (Client) para responder
             */
            // NOVA CLIENT THREAD
            Thread_Cliente cli = new Thread_Cliente(ip_origem.toString(), porta_origem, "O servidor diz OLA");
            pool.execute(cli);
            //out = new PrintWriter(socket.getOutputStream(), true);
            //out.println("O servidor diz OLA");
        }
    }

    public static void main(String args[]){
        Server svr = new Server(Integer.parseInt(args[0]),args[1],81);
        svr.run();
    }


}
