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

    //private int Nvizinhos = 2; //Numero de vizinhos que um IP pode ter
    public String serverIP;
    //private ArrayList<String> vizitados;

    public Bootstrapper(String file){
        try {
            this.ficheiro = new FileReader(file);
            System.out.println(ficheiro);
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

    public HashMap getNodos() {
        return nodos;
    }

    public HashMap getClientes() {
        return clientes;
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

    //metodo auxiliar recebe um Hashmap e uma key(ip) e um Integer (level)
    // vê se existe na hash um ip com level maior se sim substitui
    private HashMap<String, Integer> replaceIfBigger(HashMap hash, String key, Integer level){
        String aux="";
        Integer lvl=level;
        HashMap r = new HashMap<String, Integer> (hash);
        for(Object ip : r.keySet()){
            if((Integer) r.get(ip)>lvl){
                lvl= (Integer) r.get(ip);
                aux= (String) ip;
            }
        }
        if(!lvl.equals(level)){
            r.remove(aux);
            r.put(key,level);
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

                //if(aux2.size()<Nvizinhos){ //ainda tem espaço
                aux2.put(key, aux.get(key));
                //}else{ //já tem os N vizinhos, temos de ficar com os melhores
                //aux2 = new HashMap<>(replaceIfBigger(aux2, (String) key, (Integer) aux.get(key)));
                //}
            }
        }
        return aux2;
    }

    //gera string json para envio ao nodo
    private String writeLigacoes(HashMap hash){
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        for (Object key: hash.keySet()){
            ja.add(key);
        }
        jo.put("Connections",ja);
        return jo.toString();
    }

    public HashMap<String,String> getFilesToSend(){
        HashMap r = new HashMap<String,String>();

        for(Object fkey: ativos.keySet()){
            r.put(serverLigacoes.get(fkey),writeLigacoes(ativos.get(fkey).getY()));
        }

        return r;
    }

    public ArrayList<Integer> getNodoIDarray(ArrayList<String> vizitados){
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
        Tuple<String, Integer> tuplo = null;
        ArrayList<Integer> visitado = new ArrayList<>(getNodoIDarray(vizitados));
        //System.out.println("O ipdestino: " + ipDestino);
        //System.out.println("O ipOrigem: s" + ipOrigem);
        int nNodo = nodoID.get(ipDestino);
        int auxNodo;
        boolean flagServer=false;
        //System.out.println("Entra if: " + ativos.containsKey(nNodo));

        if(ipOrigem.equals(serverIP)){
            flagServer=true;
        }

        if(nodos.containsKey(nNodo)){
            hash= new HashMap<>(nodos.get(nNodo));
        }else{
            hash= new HashMap<>(clientes.get(nNodo));
        }

        if(flagServer){  //se for logo o vizinho a origem
            //System.out.println("O IP: " + ipDestino +"não tem: " + ipOrigem +"? " + hash.get(ipOrigem));
            if(hash.get(ipOrigem)==1){

                return new Tuple<>(ipOrigem,1);
            }
            //System.out.println("O server está longe");
        }

        //System.out.println("Nodos ativos para o o destino: " + ativos.get(nNodo));
        for(Object key : ativos.get(nNodo).getY().keySet()) {
            auxNodo = nodoID.get(key);
            //System.out.println(ativos.get(auxNodo));
            //System.out.println("O caralho do visitado "+visitado);
            if (!visitado.contains(auxNodo)) {
                //System.out.println("Ainda não foi visitado");
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
                        //System.out.println();
                        pesoOrigem = getPesoFromAtivos(ativos.get(auxNodo).getY(),ipOrigem);
                        //System.out.println("merda aqui: " + pesoOrigem);
                    }
                }
                //System.out.println(hash.get(ipOrigem));
                pesoTotal = pesoVizinho + pesoOrigem;
                //System.out.println("O vizinho tem peso: " + pesoVizinho + " peso origem " + pesoOrigem);
                //System.out.println("O pesoTotal tem peso: " + pesoTotal + " Para a key: " + key);
                //System.out.println("O pesoFinal " + pesoFinal + " >= " + pesoTotal + " pesoTotal");
                //System.out.println("O pesoVizinhoAtual " + pesoVizinhoAux + " >= " + pesoVizinho + " peso Do possivel vizinho");
                if (((pesoFinal >= pesoTotal && pesoVizinhoAux >= pesoVizinho && ativos.get(auxNodo).getX() == 1) || (pesoFinal > pesoTotal && pesoVizinhoAux < pesoVizinho)&& ativos.get(auxNodo).getX() == 1) ) {  //
                    pesoFinal = pesoTotal;
                    pesoVizinhoAux = pesoVizinho;
                    vizinho = (String) key;
                    //System.out.println("Novo Vizinho: " + vizinho);
                }//else
                //System.out.println("não foi adicionado: " + key );
            } //else
            //System.out.println("já foi visitado");
        }
        //por fim
        if (flagServer) {  //se for logo o vizinho a origem
            if ((Integer) hash.get(ipOrigem) < pesoVizinhoAux) {
                //System.out.println("o ip tanga xD: " + ipOrigem);
                //System.out.println(hash);
                //System.out.println("Tanga do caralho:\n peso na hash: " + hash.get(ipOrigem) + " O pesoTotal: " + pesoTotal);

                return new Tuple<>(ipOrigem, 1);
            }
        } else {
            if (getPesoFromAtivos(ativos.get(nNodo).getY(),ipOrigem) < pesoOrigem) {  //caso super especifico
                //System.out.println("Tanga do caralho2:\n peso na hash: " + ativos.get(nNodo).getY().get(ipOrigem) + " O pesoTotal: " + pesoTotal);

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
            //System.out.println(tuplo.getX());
            vizinho= tuplo.getX();
            //System.out.println(vizinho);
            peso = peso+tuplo.getY();
            list.add(vizinho);
        }
        Tuple <Integer,ArrayList<String>> r =new Tuple<>(peso,list);
        return r;
    }

    //recebe lista do melhor caminho da wantToSendFile e converte-a para poder ser dada ao server
    private ArrayList<String> listToSend(ArrayList<String> lista){
        ArrayList<String> r=new ArrayList<>();
        //String destino = lista.get(0);
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
                    //System.out.println("O IP existe no node ID:"+key);
                    if(!key.equals(serverIP) && nodoID.get((String) key)==idDestino){
                        r.add((String) key);
                        //ystem.out.println("return: "+r);
                    }else{
                        //System.out.println("O IP NÂO existe no node ID:"+key);
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

    
    //atualiza nodosComm file quando um nodo deixa de ter um ficheiro.
    public void removeNodosComFile(String ip, Integer fileID){
        if(nodosComFile.containsKey(fileID)){
            if(nodosComFile.get(fileID).contains(nodoID.get(ip))){
                nodosComFile.get(fileID).remove(nodoID.get(ip));
            }
        }

    }
    public void removeNodoDesativo(String ip){
        removeNodoDesativoaux(nodoID.get(ip));
    }

    private void removeNodoDesativoaux(Integer nodo){
        ativos.remove(nodo); //removo da lista
        for (Object key: ativos.keySet()){
            //System.out.println("print da key " + key + " ao remover o nodo: " + nodo);
            for(Object key1:ativos.get(key).getY().keySet()){
                //System.out.println("para o nodo " + key + " vejo: " + key1);
                //System.out.println("merda que falha: " +  ativos.get(key).getY().get(key1));
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
    

    public ArrayList<String> wantToSendFile(Integer fileID, String nodoDestino){
        Tuple <Integer,ArrayList<String>> r = null;
        Tuple <Integer,ArrayList<String>> aux;

        if(nodosComFile.containsKey(fileID) && !nodosComFile.get(fileID).isEmpty()){
            //Já existem nodos com o ficheiro
            String origem="";
            for(Object key:nodosComFile.get(fileID)){

                //System.out.println("PROBLEMA:");
                //System.out.println("Entra no IF:" +clientes.containsKey(nodoID.get(nodoDestino)));
                if(clientes.containsKey(nodoID.get(nodoDestino))){ //IPs que o ipDestino consegue alcançar
                    for(Object skey: clientes.get(nodoID.get(nodoDestino)).keySet()){
                        //System.out.println("Vou testar: " + skey);
                        //System.out.println("Se " + skey + " pertencer ao mesmo nodo que key: " + key);
                        if(!skey.equals(serverIP) && nodoID.get((String) skey).equals(key)){
                            origem = (String) skey;
                            //System.out.println("IP de origem: " + skey);
                        }
                    }
                }
                aux = new Tuple<>(bestRoute(origem, nodoDestino));
                if(r==null || r.getX()> aux.getX()){
                    //if(r!=null)
                    //    System.out.println("Este caminho anterio: " + r.getY());
                    //System.out.println("Este caminho é melhor que o anterio: " + aux.getY() + "pois tem peso: " + aux.getX());
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
        updateNodosComFile(r.getY(),fileID);
        return listToSend(r.getY());
    }


    //Tenho de ver o que vou returnar, pode dar jeito returnar IPS que sofreram alterações para os informar
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
                //System.out.println("key:" + key + " é nó? " + ativos.get(key).getX());
                if(ativos.get(key).getX()==1){ //trata-se de um nodo
                    //System.out.println("nodo: " + nodos.get(key) + " o ip q quero " + ip);
                    String nodoIP = containsIPNodo(nodos.get(key),ip);
                    //System.out.println("IP pertence a nodo " + nodoIP);
                    if(!nodoIP.equals("")){
                        //System.out.println("Vai atualizar ativos");
                        //if(ativos.get(key).getY().size()<Nvizinhos){
                        ativos.get(key).getY().put(nodoIP, nodos.get(key).get(nodoIP));
                        //}else{
                        //ativos.get(key).setY(append(ativos.get(key).getY(),nodoIP,(Integer) nodos.get(key).get(nodoIP))); //PODE FALHAR AQUI
                        //}
                    }
                }
            }
            System.out.println("Nova hash de ativos: "+ativos);
        }
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
        Bootstrapper strapper = new Bootstrapper(args[0]);
        //strapper.parser();

        System.out.println("Main print");
        System.out.println("Server:");
        System.out.println(strapper.serverLigacoes);
        System.out.println("Nodos:");
        System.out.println(strapper.nodos);
        System.out.println("Clientes:");
        System.out.println(strapper.clientes);

        //Para a rede
        strapper.addIPativo("10.0.4.1");   //2
        strapper.addIPativo("10.0.5.2");   //3
        strapper.addIPativo("10.0.18.1");  //7
        strapper.addIPativo("10.0.16.1");  //6
        //strapper.addIPativo("10.0.13.1");  //5
        strapper.addIPativo("10.0.0.20");  //11

        strapper.addIPativo("10.0.2.20");   //5
        strapper.addIPativo("10.0.11.21");  //11
        //strapper.addIPativo("10.0.3.21");  //11


         /*                                 //# nodos para a rede1
        strapper.addIPativo("10.0.7.1");  //0
        strapper.addIPativo("10.0.10.1"); //não existe
        strapper.addIPativo("10.0.1.1");  //0
        strapper.addIPativo("10.0.4.2");  //2
        strapper.addIPativo("10.0.7.2");  //1
        strapper.addIPativo("10.0.2.2");  //3

*/
        System.out.println("\nNodosID" + strapper.nodoID + "\n");

        System.out.println("Ativos: \n" + strapper.ativos);

        //System.out.println(strapper.getFilesToSend() + "\n");
        //strapper.getVizinhos(strapper.getFilesToSend().get("10.0.8.2"));

        //System.out.println("Ligação: " + strapper.wantToSendFile(1,"10.0.4.10", "10.0.3.21"));
        //System.out.println("Ligação: " + strapper.wantToSendFile(1,"10.0.4.10", "10.0.0.20"));
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
    }
}

