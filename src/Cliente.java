//cd /../../../home/falape/Projetos/ESR/src/
import java.io.*;
import java.net.Socket;

public class Cliente implements Runnable{


    public String ip;
    public int porta;
    String msg;

    //mudar args
    public Cliente(String ip, int porta){
        this.ip = ip;
        this.porta = porta;
    }


    public Cliente(String ip, int porta, String mensagem){
        this.ip = ip;
        this.porta = porta;
        this.msg = mensagem;
    }

    public void run() {

        Socket clientSocket;
        OutputStream out;
        InputStream in;


        /**
         *  tenta fazer a ligação.
         */
        try {
            System.out.println("vou abrir em "+ip+":"+porta);
            ip=ip.replace("/","");
            clientSocket = new Socket(ip, porta);
            System.out.println("Abri cli-socket em "+ip+":"+porta);
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();

            /**
             * Converter a mensagem para byte[] e enviar
             */
            byte[] tmp = msg.getBytes();
            ByteMessages.sendBytes(tmp, out);

            /**
             * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
             */
            //tmp = ByteMessages.readBytes(in);
            //String data = new String(tmp);
            //System.out.println(">>> "+data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]){
        Cliente cli = new Cliente("127.0.0.1", 6868);
        cli.run();
    }
}
