import lib.Tuple;
import org.json.simple.*;



import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;

public class Bootstrapper {

    private Reader ficheiro;
    private HashMap<String, Integer> nodoID;
    private HashMap<Integer, String> serverLigacoes;
    private HashMap<Integer, HashMap> nodos;
    private HashMap<Integer, HashMap> clientes;
    private HashMap<Integer, Tuple<Integer,HashMap>> ativos;

    private int Nvizinhos = 2; //Numero de vizinhos que um IP pode ter

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
    }

    public HashMap getNodos() {
        return nodos;
    }

    public HashMap getClientes() {
        return clientes;
    }


    public void parser(){
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
            JSONArray ligacoes = (JSONArray) server.get("Connections");

            for (int i = 0; i < ligacoes.size(); i++) {
                JSONObject tuplo = (JSONObject) ligacoes.get(i);
                String id = (String) tuplo.get("id");

                serverLigacoes.put(nodoID.get(id),id);
            }
        }
    }

    //verifica se um ip(String) pertence algum nodo cujo os ips de acesso estão dentro de uma HashMap(com ips String),
    // convertendo os primeiro para o seu nmero de Nodo
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
        if(lvl!=level){
            r.remove(aux);
            r.put(key,level);
        }
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

                if(aux2.size()<Nvizinhos){ //ainda tem espaço
                    aux2.put(key, aux.get(key));
                }else{ //já tem os N vizinhos, temos de ficar com os melhores
                    aux2 = new HashMap<>(replaceIfBigger(aux2, (String) key, (Integer) aux.get(key)));
                }
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

    //Não pertence aqui, vai para o nodo
    public void getVizinhos(String json){
        Object obj = JSONValue.parse(json);
        JSONObject objt = (JSONObject) obj;
        JSONArray array = (JSONArray) objt.get("Connections");
        for(int i=0; i<array.size(); i++){
            System.out.println(array.get(i));
        }
    }

    //Tenho de ver o que vou returnar, pode dar jeito returnar IPS que sofreram alterações para os informar
    public void addIPativo(String ip){
        HashMap aux;
        Integer nIP = nodoID.get(ip);

        if(this.ativos.containsKey(nIP)){
            //timestamp

        }else{ //ip ainda não estava ativo

            //VER SE ESSE IP TEM IPS VIZINHOS ATIVOS
            if(this.nodos.containsKey(nIP)){
                //é um IP de um nodo
                aux =new HashMap(this.nodos.get(nIP)); //retorna um hashmap das possiveis ligações do ip

                //Já tenho lista de vizinho è adicionar aos vizinhos
                ativos.put(nIP, new Tuple(1,vizinhosAtivosArray(aux)));

            }else
                if(this.clientes.containsKey(nIP)){
                    //ver se esse ip tem ips vizinho ativos
                    if(this.nodos.containsKey(nIP)){
                        //é um IP de um nodo
                        aux =new HashMap(this.clientes.get(nIP)); //retorna um hashmap das possiveis ligações do ip

                        //Já tenho lista de vizinho è adicionar aos vizinhos
                        ativos.put(nIP, new Tuple(0,vizinhosAtivosArray(aux)));
                    }
            }
            //ATUALIZAR OS RESTANTES IPS ATIVOS PARA SABEREM DO NOVO NODO
            for(Object key: ativos.keySet()){
                //System.out.println("key:" + key + " é nó? " + ativos.get(key).getX());
                if(ativos.get(key).getX()==1){ //trata-se de um nodo
                    //System.out.println("nodo: " + nodos.get(key) + " o ip q quero " + ip);
                    String nodoIP = containsIPNodo(nodos.get(key),ip);
                    //System.out.println("IP pertence a nodo " + nodoIP);
                    if(nodoIP != ""){
                        //System.out.println("Vai atualizar ativos");
                        if(ativos.get(key).getY().size()<Nvizinhos){
                            HashMap auxAtivos = ativos.get(key).getY();
                            auxAtivos.put(nodoIP, nodos.get(key).get(nodoIP));
                        }else{
                            ativos.get(key).setY(replaceIfBigger(ativos.get(key).getY(),nodoIP,(Integer) nodos.get(key).get(nodoIP))); //PODE FALHAR AQUI
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(args[0]);
        Bootstrapper strapper = new Bootstrapper(args[0]);
        strapper.parser();

        System.out.println("Main print");
        System.out.println("Server:");
        System.out.println(strapper.serverLigacoes);
        System.out.println("Nodos:");
        System.out.println(strapper.nodos);
        System.out.println("Clientes:");
        System.out.println(strapper.clientes);

        //Para a rede
        strapper.addIPativo("10.0.4.1");  //2
        strapper.addIPativo("10.0.5.2"); //3
        strapper.addIPativo("10.0.18.1");  //7
        strapper.addIPativo("10.0.16.1");  //6
        strapper.addIPativo("10.0.13.1");  //5
        strapper.addIPativo("10.0.0.21");  //11


         /*                                 //# nodos para a rede1
        strapper.addIPativo("10.0.7.1");  //0
        strapper.addIPativo("10.0.10.1"); //não existe
        strapper.addIPativo("10.0.1.1");  //0
        strapper.addIPativo("10.0.4.2");  //2
        strapper.addIPativo("10.0.7.2");  //1
        strapper.addIPativo("10.0.2.2");  //3
*/
        System.out.println(strapper.ativos);

        System.out.println(strapper.getFilesToSend() + "\n");

        //strapper.getVizinhos(strapper.getFilesToSend().get("10.0.8.2"));
    }
}

