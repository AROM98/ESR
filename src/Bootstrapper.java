import org.json.simple.*;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Bootstrapper {

    private Reader ficheiro;
    private HashMap<String, Integer> nodoID;
    private HashMap<Integer, String> serverLigacoes;
    private HashMap<Integer, HashMap> nodos;
    private HashMap<Integer, HashMap> clientes;
    private HashMap<Integer, Tuple<Integer,HashMap>> ativos;
    private HashMap<Integer, ArrayList> nodosComFile;
    private HashMap<Integer, Timestamp> nodosTimeStamp;

    public String serverIP;


    public Bootstrapper(String file){
        try {
            this.ficheiro = new FileReader(file);
        }catch (Exception e){
            e.getStackTrace();
        }

        this.nodos = new HashMap<>();
        this.clientes = new HashMap<>();
        this.ativos = new HashMap<>();
        this.nodoID = new HashMap<>();
        this.serverLigacoes = new HashMap<>();
        this.nodosComFile = new HashMap<>();
        this.nodosTimeStamp = new HashMap<>();
        this.serverIP = "";

        parser();
    }

    private void parser(){
        Object obj = JSONValue.parse(ficheiro);
        JSONObject objt = (JSONObject) obj;
        JSONArray nodos = (JSONArray) objt.get("nodos");

        JSONObject server = null;
        HashMap hash;

        for(int n=0;n<nodos.size();n++){
            HashMap ligacao = new HashMap<>();

            JSONObject nodo = (JSONObject) nodos.get(n);
            Long aux = (Long) nodo.get("Rank");
            Integer rank = aux.intValue();

            if(rank==1){
                hash = this.nodos;
            }else
            if(rank==0){
                hash = this.clientes;
            }else{ //tratar do server
                server=(JSONObject) nodos.get(n);
                continue;
            }
            //tratar dos ips que o nodo pode tomar
            JSONArray ligacoes = (JSONArray) nodo.get("Connections");
            for(int i = 0; i<ligacoes.size(); i++){
                JSONObject tuplo = (JSONObject) ligacoes.get(i);
                String id = (String) tuplo.get("id");
                Long levelJson = (Long) tuplo.get("level");
                int level = levelJson.intValue();

                ligacao.put(id,level);

            }

            JSONArray ips = (JSONArray) nodo.get("IPs");
            for(int i = 0; i<ips.size(); i++){
                String ip = (String) ips.get(i);
                nodoID.put(ip,n);
            }

            hash.put(n,ligacao);
        }

        //tratar do server
        if(server!=null) {
            JSONArray IP = (JSONArray) server.get("IPs");
            serverIP =(String) IP.get(0);
            JSONArray ligacoes = (JSONArray) server.get("Connections");

            for (int i = 0; i < ligacoes.size(); i++) {
                JSONObject tuplo = (JSONObject) ligacoes.get(i);
                String id = (String) tuplo.get("id");

                serverLigacoes.put(nodoID.get(id),id);
            }
        }
    }


    //verifica se um ip(String) pertence algum nodo cujo os ips de acesso estão dentro de uma HashMap(com ips String),
    // convertendo os primeiro para o seu numero de Nodo
    private String containsIPNodo(HashMap hash, String ip){
        String r = "";
        Integer node= nodoID.get(ip);
        for(Object key : hash.keySet()){
            if(nodoID.get((String) key)==node){
                r=(String) key;
            }
        }
        return r;
    }


    private HashMap<String, Integer> append(HashMap hash, String key, Integer level){
        HashMap r = new HashMap<String, Integer> (hash);

        r.put(key,level);
        return r;
    }


    //Auxiar da addTPativo, que irá verificar se um IP já se encontra ativo e se não se encontrar criar um
    // hashmap com os seu vizinhos ativos
    private HashMap<String, Integer> vizinhosAtivosArray(HashMap vizinhos){
        HashMap aux2 =new HashMap<String, Integer>();
        HashMap aux =new HashMap(vizinhos);

        for(Object key : aux.keySet()){
            Integer nIP = nodoID.get(key);
            if(this.ativos.containsKey(nIP)){ //o ip tem um vizinho ativo

                aux2.put(key, aux.get(key));
            }
        }
        return aux2;
    }


    private ArrayList<Integer> getNodoIDarray(ArrayList<String> vizitados){
        ArrayList<Integer> r = new ArrayList<>();
        for (String value : vizitados){
            r.add(nodoID.get(value));
        }
        return r;
    }


    private Integer getPesoFromAtivos(HashMap<String,Integer> hash, String ip){
        Integer peso = 100000;
        Integer idIP = nodoID.get(ip);
        for(Object key : hash.keySet()){
            if(nodoID.get((String) key) == idIP){
                return hash.get(key);
            }
        }
        return peso;
    }


    //ipOrigem é o server/nodo que vai enviar os ficheiros ipDestino é o que vai receber
    //ipOrigem é sempre o mesmo, vou avançando com o destino para mais próximo da origem
    private Tuple<String,Integer> bestVizinho( ArrayList<String> vizitados,String ipOrigem, String ipDestino) {
        int pesoOrigem = 0;
        int pesoTotal = 0;
        int pesoVizinho = 0;
        int pesoVizinhoAux = 100000;
        int pesoFinal = 100000;
        String vizinho = "";
        HashMap <String, Integer> hash = null;
        ArrayList<Integer> visitado = new ArrayList<>(getNodoIDarray(vizitados));
        int nNodo = nodoID.get(ipDestino);
        int auxNodo;
        boolean flagServer=false;


        if(ipOrigem.equals(serverIP)){
            flagServer=true;
        }

        if(nodos.containsKey(nNodo)){
            hash= new HashMap<>(nodos.get(nNodo));
        }else{
            hash= new HashMap<>(clientes.get(nNodo));
        }

        if(flagServer){  //se for logo o vizinho a origem

            if(hash.get(ipOrigem)==1){
                return new Tuple<>(ipOrigem,1);
            }

        }


        for(Object key : ativos.get(nNodo).getY().keySet()) {
            auxNodo = nodoID.get(key);

            if (!visitado.contains(auxNodo)) {

                if (key.equals(ipOrigem) && (Integer) ativos.get(nNodo).getY().get(key) == 1) {  //caso super especifico
                    return new Tuple<>(ipOrigem, 1);
                }

                pesoVizinho = (Integer) ativos.get(nNodo).getY().get(key); //peso de usar a key

                if (flagServer) {  //como é para o server vai buscar o peso desse vizinho até a ele
                    if (ativos.get(auxNodo).getX() == 1) { //só quero que sejam nodos do tipo nodo e não cliente
                        if (nodos.containsKey(auxNodo)) {
                            pesoOrigem = (Integer) nodos.get(auxNodo).get(ipOrigem);
                        } else {
                            pesoOrigem = (Integer) clientes.get(auxNodo).get(ipOrigem);
                        }
                    }
                }else{

                    if (ativos.get(auxNodo).getX() == 1) { //só quero que sejam nodos do tipo nodo e não cliente
                        pesoOrigem = getPesoFromAtivos(ativos.get(auxNodo).getY(),ipOrigem);
                    }
                }

                pesoTotal = pesoVizinho + pesoOrigem;

                if (((pesoFinal >= pesoTotal && pesoVizinhoAux >= pesoVizinho && ativos.get(auxNodo).getX() == 1) || (pesoFinal > pesoTotal && pesoVizinhoAux < pesoVizinho)&& ativos.get(auxNodo).getX() == 1) ) {  //
                    pesoFinal = pesoTotal;
                    pesoVizinhoAux = pesoVizinho;
                    vizinho = (String) key;
                }
            }
        }
        //por fim
        if (flagServer) {  //se for logo o vizinho a origem
            if ((Integer) hash.get(ipOrigem) < pesoVizinhoAux) {

                return new Tuple<>(ipOrigem, 1);
            }
        } else {
            if (getPesoFromAtivos(ativos.get(nNodo).getY(),ipOrigem) < pesoOrigem) {  //caso super especifico

                return new Tuple<>(ipOrigem, 1);
            }
        }
        return new Tuple<>(vizinho,pesoFinal);
    }


    private Tuple<Integer,ArrayList<String>> bestRoute(String ipOrigem, String ipDestino){
        ArrayList list = new ArrayList<String>();
        String vizinho = ipDestino;
        int peso=0;
        Tuple <String,Integer> tuplo;

        list.add(vizinho);
        while(!vizinho.equals(ipOrigem)){
            tuplo = new Tuple<>(bestVizinho(list,ipOrigem, vizinho));
            vizinho= tuplo.getX();
            peso = peso+tuplo.getY();
            list.add(vizinho);
        }
        Tuple <Integer,ArrayList<String>> r =new Tuple<>(peso,list);
        return r;
    }

    //recebe lista do melhor caminho da wantToSendFile e converte-a para poder ser dada ao server
    private ArrayList<String> listToSend(ArrayList<String> lista){
        ArrayList<String> r=new ArrayList<>();

        Collections.reverse(lista);
        for(int i=0; i<lista.size()-1; i++){
            if(i==0 && !lista.get(i).equals(serverIP)){
                r.add(serverLigacoes.get(nodoID.get(lista.get(i))));
            }else{
                if(i==0 && lista.get(i).equals(serverIP)) {
                    r.add(serverLigacoes.get(nodoID.get(lista.get(i+1))));
                    continue;
                }
            }
            int idOrigem = nodoID.get(lista.get(i));
            int idDestino =nodoID.get(lista.get(i+1));
            if(nodos.containsKey(idOrigem)){
                for(Object key:nodos.get(idOrigem).keySet()){

                    if(!key.equals(serverIP) && nodoID.get((String) key)==idDestino){
                        r.add((String) key);

                    }
                }
            }
        }
        return r;
    }

    //Atualiza os nodos que passaram a ter o ficheiro
    private void updateNodosComFile(ArrayList<String> list, Integer fileID){
        if(!nodosComFile.containsKey(fileID)){
            ArrayList<Integer> r = new ArrayList<>();
            for(String value: list){
                if (!value.equals(serverIP) && !clientes.containsKey(nodoID.get(value)))
                    r.add(nodoID.get(value));
            }
            nodosComFile.put(fileID,r);
        }else{
            for(String value : list){
                Integer nodo = nodoID.get(value);
                if(!nodosComFile.get(fileID).contains(nodo) && !value.equals(serverIP) && !clientes.containsKey(nodoID.get(value))){
                    nodosComFile.get(fileID).add(nodo);
                }
            }
        }
    }

    private void removeNodoDesativoaux(Integer nodo){
        ativos.remove(nodo); //removo da lista
        for (Object key: ativos.keySet()){

            for(Object key1:ativos.get(key).getY().keySet()){

                if(nodoID.get(key1).equals(nodo)){
                    HashMap aux = new HashMap<>(ativos.get(key).getY());
                    aux.remove(key1);
                    ativos.put((Integer) key, new Tuple<Integer,HashMap>(ativos.get(key).getX(),aux));
                }
            }
        }


        //Atualizo o nodosComFile caso esse nodo tenha algum ficheiro
        for(Object file : nodosComFile.keySet()){
            if(nodosComFile.get(file).contains(nodo)){
                nodosComFile.get(file).remove(nodo);
            }
        }
    }

    private void checkTimeStamps(){
        for(Object key:nodosTimeStamp.keySet()){
            if(new Date().getTime()-nodosTimeStamp.get((key)).getTime()>=12000){ //caducado
                removeNodoDesativoaux((Integer) key);
            }
        }
    }


    //#########PUBLIC METHODS##############

    //atualiza nodosComfile quando um nodo deixa de ter um ficheiro. )
    public void removeNodosComFile(String ip, Integer fileID){
        if(nodosComFile.containsKey(fileID)){
            if(nodosComFile.get(fileID).contains(nodoID.get(ip))){
                nodosComFile.get(fileID).remove(nodoID.get(ip));
            }
        }

    }

    //Remove um Nodo Desativo(utilizado pelo timestamp), publico caso seja necessário
    public void removeNodoDesativo(String ip){
        removeNodoDesativoaux(nodoID.get(ip));
    }
    
    //Enviar um ficheiro para um destino, devolve melhor caminho, e se a origem deverá ser um nodo ou  o servidor
    public ArrayList<String> wantToSendFile(Integer fileID, String nodoDestino){
        Tuple <Integer,ArrayList<String>> r = null;
        Tuple <Integer,ArrayList<String>> aux;

        if(nodosComFile.containsKey(fileID) && !nodosComFile.get(fileID).isEmpty() && 1==0){ // Funciona mas não foi implementado no resto do trabalho
                                                                                    // foi adicionado 1==0 para falhar

            //Já existem nodos com o ficheiro
            String origem="";
            for(Object key:nodosComFile.get(fileID)){

                if(clientes.containsKey(nodoID.get(nodoDestino))){ //IPs que o ipDestino consegue alcançar
                    for(Object skey: clientes.get(nodoID.get(nodoDestino)).keySet()){

                        if(!skey.equals(serverIP) && nodoID.get((String) skey).equals(key)){
                            origem = (String) skey;
                        }
                    }
                }
                aux = new Tuple<>(bestRoute(origem, nodoDestino));
                if(r==null || r.getX()> aux.getX()){
                    r=new Tuple<>(aux);
                }
            }
            //Ver se é melhor ir buscar a esses nodos
            // ou ir ao servidor
            aux = new Tuple<>(bestRoute(serverIP, nodoDestino));
            if(r!=null && aux.getX()< r.getX()){
                r = new Tuple<>(aux);
            }
        }else{
            //Não existem nodos com o ficheiro logo vai ao servidor
            r = new Tuple<>(bestRoute(serverIP, nodoDestino));
        }
        //updateNodosComFile(r.getY(),fileID);  // Funciona pela mesma razão a cima não foi implementado no resto do trabalho
        return listToSend(r.getY());
    }


    //Adiciona um ip, caso seja novo é adicionado aos ativos e recebe timeStamp, caso seja ativo, é atualizado o timeStamp
    public void addIPativo(String ip){
        Integer nIP = nodoID.get(ip);

        if(this.ativos.containsKey(nIP)){
            //timestamp
            java.util.Date date = new java.util.Date();
            nodosTimeStamp.put(nIP, new Timestamp(date.getTime()));
            checkTimeStamps();
            System.out.println("Já era ativo, mesma hash: "+ativos);
        }else{ //ip ainda não estava ativo

            //VER SE ESSE IP TEM IPS VIZINHOS ATIVOS
            if(this.nodos.containsKey(nIP)){
                //é um IP de um nodo
                //Vou ver quais os vizinhos a diciona-los ao tuplo
                ativos.put(nIP, new Tuple(1,vizinhosAtivosArray(nodos.get(nIP))));
                
            }else
            if(this.clientes.containsKey(nIP)){
                //é um IP de um cliente
                //Vou ver quais os vizinhos a diciona-los ao tuplo
                ativos.put(nIP, new Tuple(0,vizinhosAtivosArray(clientes.get(nIP))));
            }
            //Atualiza timestamps
            java.util.Date date = new java.util.Date();
            nodosTimeStamp.put(nIP, new Timestamp(date.getTime()));
            //ATUALIZAR OS RESTANTES IPS ATIVOS PARA SABEREM DO NOVO NODO
            for(Object key: ativos.keySet()){

                if(ativos.get(key).getX()==1){ //trata-se de um nodo
                    String nodoIP = containsIPNodo(nodos.get(key),ip);

                    if(!nodoIP.equals("")){
                        ativos.get(key).getY().put(nodoIP, nodos.get(key).get(nodoIP));
                    }
                }
            }
            System.out.println("Nova hash de ativos: "+ativos);
        }
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
        Bootstrapper strapper = new Bootstrapper(args[0]);

        System.out.println("Main print");
        System.out.println("Server:");
        System.out.println(strapper.serverLigacoes);
        System.out.println("Nodos:");
        System.out.println(strapper.nodos);
        System.out.println("Clientes:");
        System.out.println(strapper.clientes);

        //Para a rede
        /*
        strapper.addIPativo("10.0.4.1");   //2
        strapper.addIPativo("10.0.5.2");   //3
        strapper.addIPativo("10.0.18.1");  //7
        strapper.addIPativo("10.0.16.1");  //6
        //strapper.addIPativo("10.0.13.1");  //5
        strapper.addIPativo("10.0.0.20");  //11

        strapper.addIPativo("10.0.2.20");   //5
        strapper.addIPativo("10.0.11.21");  //11
        //strapper.addIPativo("10.0.3.21");  //11
*/

                                          //# nodos para a rede1
        //strapper.addIPativo("10.0.3.20");  //cliente
        strapper.addIPativo("10.0.10.1"); //não existe
        strapper.addIPativo("10.0.1.1");  //1
        //strapper.addIPativo("10.0.4.2");  //3
        //strapper.addIPativo("10.0.7.2");  //2
        strapper.addIPativo("10.0.2.2");  //4
        strapper.addIPativo("10.0.9.20");  //cliente


        System.out.println("\nNodosID" + strapper.nodoID + "\n");

        System.out.println("Ativos: \n" + strapper.ativos);
        strapper.addIPativo("10.0.7.2");  //2
        System.out.println("Ativos: \n" + strapper.ativos);
        /*
        System.out.println("Ligação: " + strapper.wantToSendFile(1, "10.0.0.20"));
        System.out.println("FILES COM O ID: " + strapper.nodosComFile);
        System.out.println("Ligação: " + strapper.wantToSendFile(1, "10.0.11.21"));
        strapper.removeNodoDesativo("10.0.5.2");
        System.out.println("Ativos: \n" + strapper.ativos);
        //strapper.removeNodosComFile("10.0.5.2",1);
        strapper.removeNodosComFile("10.0.4.1",1);
        System.out.println("FILES COM O ID: " + strapper.nodosComFile);
        System.out.println("Ligação: " + strapper.wantToSendFile(1, "10.0.0.20"));
        System.out.println("Ligação: " + strapper.wantToSendFile(1, "10.0.11.21"));
        System.out.println("FILES COM O ID: " + strapper.nodosComFile);
         */
    }
}

