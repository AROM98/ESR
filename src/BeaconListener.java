import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

class BeaconListener implements Runnable {

    public int beacon_port = 6870;
    public Bootstrapper bootstrapper;

    public BeaconListener(Bootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;
    }

    public void run() {

        ServerSocket serverSocket = null;
        Socket socket;
        InputStream input = null;

        try {
            serverSocket = new ServerSocket(beacon_port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {

            try {
                socket = serverSocket.accept();
                input = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] tmp = new byte[0];
            try {
                tmp = ByteMessages.readBytes(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String ipNode;
            String[] res;
            String data = new String(tmp);
            res = data.split(" ");

            ipNode = res[1];
            bootstrapper.addIPativo(ipNode);
        }
    }
}
