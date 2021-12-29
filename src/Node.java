public class Node{

    public static void main(String[] args){

        String server = "";
        String nodeIp = "";
        int portaBeacon = 6870;
        int receber_input = 0;


        if (args.length != 3) {
            System.out.println("Insira o ip do server e flag nodo/cliente nos argumentos");
        } else {
            server = args[0];
            nodeIp = args[2];
            receber_input = Integer.parseInt(args[1]);
            System.out.println("O server é: " + server);
            System.out.println("O ip do Node é: " + nodeIp);
        }

        Thread beaconThread = new Thread(new Beacon(server, nodeIp, portaBeacon));
        beaconThread.start();

        NodeAux nodeAux = new NodeAux(server,nodeIp);

        if(receber_input == 1) {
            nodeAux.NodeInput();
        }
        else {
            nodeAux.NodeRelay();
        }
    }
}
