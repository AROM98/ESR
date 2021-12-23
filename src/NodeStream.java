/* ------------------
   Cliente
   usage: java Cliente
   adaptado dos originais pela equipa docente de ESR (nenhumas garantias)
   colocar o cliente primeiro a correr que o servidor dispara logo!
   ---------------------- */

import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;


public class NodeStream {


    //Video constants:
    //------------------
    int imagenb = 0; //image nb of the image currently transmitted

    //RTP variables:
    //----------------
    InetAddress ipNodoVizinho; //Client IP address

    DatagramPacket rcvdp; //UDP packet received from the server (to receive)
    DatagramPacket senddp; //UDP packet containing the video frames (to send)
    DatagramSocket RTPsocketReceiver; //socket to be used to send and receive UDP packet
    DatagramSocket RTPsocketSender; //socket to be used to send and receive UDP packet
    int RTP_PORT; //port where the client will receive the RTP packets

    Timer nTimer; //timer used to receive data from the UDP socket and send it to a neighbour node
    byte[] dBuf; //buffer used to store data received from the server


    //--------------------------
    //Constructor
    //--------------------------
    public NodeStream(String ipNodoVizinho, int portaStream){


        RTP_PORT = portaStream;

        //init para a parte do cliente
        //--------------------------
        nTimer = new Timer(20, new nodeReceiverSender());
        nTimer.setInitialDelay(0);
        nTimer.setCoalesce(true);
        dBuf = new byte[15000]; //allocate enough memory for the buffer used to receive data from the server

        try {
            RTPsocketSender = new DatagramSocket(); //init RTP socket
            this.ipNodoVizinho = InetAddress.getByName(ipNodoVizinho);
            System.out.println("Servidor: socket " + ipNodoVizinho);

        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }

        try {
            // socket e video
            RTPsocketReceiver = new DatagramSocket(RTP_PORT); //init RTP socket (o mesmo para o cliente e servidor)
            RTPsocketReceiver.setSoTimeout(5000); // setimeout to 5s
        } catch (SocketException e) {
            System.out.println("Cliente: erro no socket: " + e.getMessage());
        }
        nTimer.start();
    }

    //------------------------------------
    //Handler for timer (para cliente)
    //------------------------------------

    class nodeReceiverSender implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            try{
                imagenb++;

                //-----------RECEIVING--------

                //Construct a DatagramPacket to receive data from the UDP socket
                rcvdp = new DatagramPacket(dBuf, dBuf.length);
                RTPsocketReceiver.receive(rcvdp);

                //create an RTPpacket object from the DP
                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                //print important header fields of the RTP packet received:
                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

                //print header bitstream:
                rtp_packet.printheader();

                //-----------SENDING--------

                //Construct a DatagramPacket to send data over the UDP socket
                senddp = new DatagramPacket(dBuf, dBuf.length, ipNodoVizinho, RTP_PORT);
                RTPsocketSender.send(senddp);

                System.out.println("Send frame #"+imagenb);
            }
            catch (InterruptedIOException iioe){
                System.out.println("Nothing to read");
            }
            catch (IOException ioe) {
                System.out.println("Exception caught: "+ioe);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

