import lib.Tuple;
import org.json.simple.*;



import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

public class bootstrapper {

    private Reader ficheiro;
    private HashMap<String, HashMap> nodos;
    private HashMap<String, HashMap> clientes;
    private HashMap<String, Tuple<Integer,HashMap>> ativos;

    private int Nvizinhos = 2; //Numero de vizinhos que um IP pode ter

    public HashMap getNodos() {
        return nodos;
    }

    public void setNodos(HashMap nodos) {
        nodos = nodos;
    }

    public HashMap getClientes() {
        return clientes;
    }

    public void setClientes(HashMap clientes) {
        clientes = clientes;
    }

    public bootstrapper(String file){
        try {
            this.ficheiro = new FileReader(file);
            System.out.println(ficheiro);
        }catch (Exception e){
            e.getStackTrace();
        }

        this.nodos = new HashMap<>();
        this.clientes = new HashMap<>();
        this.ativos = new HashMap<>();

    }

    public void parser(){
        Object obj = JSONValue.parse(ficheiro);
        JSONObject objt = (JSONObject) obj;

        JSONArray nodos = (JSONArray) objt.get("nodos");

        int n=0;
        HashMap hash;

        while(n<nodos.size()){
            HashMap ligacao = new HashMap<>();

            JSONObject nodo = (JSONObject) nodos.get(n);
            Long aux = (Long) nodo.get("Rank");
            Integer rank = aux.intValue();

            if(rank==1){
                hash = this.nodos;
            }else{
                hash = this.clientes;
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
                hash.put(ip,ligacao);
            }
            n++;
        }
    }

    //metodo auxiliar recebe um Hashmap e um Objeto que é suposto ser um Integer xD e
    // vê se existe na hash uma key com um valor maior se sim substitui
    private HashMap<String, Integer> replaceIfBigger(HashMap hash, String key, Integer level){
        String aux="";
        Integer lvl=level;
        HashMap r = new HashMap<String, Integer> (hash);
        for(Object ip : r.entrySet()){
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
            if(this.ativos.containsKey((String) key)){ //o ip tem um vizinho ativo

                if(aux2.size()<Nvizinhos){ //ainda tem espaço
                    aux2.put(key, aux.get(key));
                }else{ //já tem os N vizinhos, temos de ficar com os melhores
                    for(Object keyAux: aux2.keySet()){
                        aux2 = new HashMap<>(replaceIfBigger(aux2, (String) key, (Integer) aux.get(key)));
                    }
                }
            }
        }
        return aux2;
    }


    //Tenho de ver o que vou returnar, pode dar jeito returnar IPS que sofreram alterações para os informar
    public void addIPativo(String ip){
        HashMap aux;


        if(this.ativos.containsKey(ip)){
            //timestamp

        }else{ //ip ainda não estava ativo

            //ATUALIZAR OS RESTANTES IPS ATIVOS PARA SABEREM DO NOVO NODO
            for(Object key: ativos.keySet()){
                if(ativos.get(key).getX()==1){ //trata-se de um nodo
                    if(nodos.get(key).containsKey(ip)){
                        ativos.get(key).setY(replaceIfBigger(ativos.get(key).getY(),ip,(Integer) nodos.get(key).get(ip))); //PODE FALHAR AQUI
                    }
                }
            }
            System.out.println("Não tinha ativos");

            //VER SE ESSE IP TEM IPS VIZINHOS ATIVOS
            if(this.nodos.containsKey(ip)){
                //é um IP de um nodo
                aux =new HashMap(this.nodos.get(ip)); //retorna um hashmap das possiveis ligações do ip

                //Já tenho lista de vizinho è adicionar aos vizinhos
                ativos.put(ip, new Tuple(1,vizinhosAtivosArray(aux)));

            }else
                if(this.clientes.containsKey(ip)){
                    //ver se esse ip tem ips vizinho ativos
                    if(this.nodos.containsKey(ip)){
                        //é um IP de um nodo
                        aux =new HashMap(this.clientes.get(ip)); //retorna um hashmap das possiveis ligações do ip

                        //Já tenho lista de vizinho è adicionar aos vizinhos
                        ativos.put(ip, new Tuple(0,vizinhosAtivosArray(aux)));
                    }
            }
        }
    }

    public static void main(String[] args) {
        bootstrapper strapper = new bootstrapper(args[0]);
        strapper.parser();

        System.out.println("Main print\nNodos:");
        System.out.println(strapper.nodos);
        System.out.println("Clientes:");
        System.out.println(strapper.clientes);

        strapper.addIPativo("10.0.7.1");
        strapper.addIPativo("10.0.10.1");
        strapper.addIPativo("10.0.1.1");
        strapper.addIPativo("10.0.7.2");

        System.out.println(strapper.ativos);
    }
}

