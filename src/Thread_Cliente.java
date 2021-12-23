//cd /../../../home/falape/Projetos/ESR/src/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Thread_Cliente implements Runnable{


    public String ip;
    public int porta;
    String msg;

    //mudar args
    public Thread_Cliente(String ip, int porta){
        this.ip = ip;
        this.porta = porta;
    }


    public Thread_Cliente(String ip, int porta, String mensagem){
        this.ip = ip;
        this.porta = porta;
        this.msg = mensagem;
    }

    public void run() {

        Socket clientSocket = null;
        PrintWriter out;
        BufferedReader in;

        /**
         *  tenta fazer a ligação.
         */
        try {
            System.out.println("vou abrir em "+ip+":"+porta);
            ip=ip.replace("/","");
            System.out.println(ip);
            clientSocket = new Socket(ip, porta);
            System.out.println("Abri cli-socket em "+ip+":"+porta);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("olá diz o cliente");

            /**
             * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
             */
            //String resp = in.readLine();
            //System.out.println("resposta: "+resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String args[]){
        Cliente cli = new Cliente("10.0.0.1", 6868);
        cli.run();
    }
}
