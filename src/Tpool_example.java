import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tpool_example {



    public void run(){
        // cria uma pool
        ExecutorService pool = Executors.newCachedThreadPool();

        //vou criar X threads de T_poool_runnable a partir da pool
        for(int i = 0; i < 10; i++){
            T_pool_runnable threadX = new T_pool_runnable();
            pool.execute((Runnable) threadX);
        }

    }


    public static void main(String args[]){
        Tpool_example t_pool = new Tpool_example();
        t_pool.run();
    }
}
