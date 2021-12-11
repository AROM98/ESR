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

        Socket clientSocket = null;
        OutputStream out;
        BufferedReader in;


        /**
         *  tenta fazer a ligação.
         */
        try {
            System.out.println("vou abrir em "+ip+":"+porta);
            clientSocket = new Socket(ip, porta);
            System.out.println("Abri cli-socket em "+ip+":"+porta);
            out = clientSocket.getOutputStream();
            //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            /**
             * Converter a mensagem para byte[] e enviar
             */
            byte[] tmp = msg.getBytes();
            sendBytes(tmp, out);

            //out.println("olá diz o cliente");

            /**
             * Se for preciso ficar a espera de resposta, então retirar comentario das seguintes linhas.
             */
            //String resp = in.readLine();
            //System.out.println("resposta: "+resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendBytes(byte[] myByteArray, OutputStream out) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length, out);
    }

    public void sendBytes(byte[] myByteArray, int start, int len, OutputStream out) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        // Other checks if needed.

        // May be better to save the streams in the support class;
        // just like the socket variable.
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }


    public static void main(String args[]){
        Cliente cli = new Cliente("127.0.0.1", 81);
        cli.run();
    }
}
