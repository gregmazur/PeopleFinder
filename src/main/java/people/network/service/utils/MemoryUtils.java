package people.network.service.utils;

/**
 *
 *
 * @author Mazur G <a href="mailto:mazur@ibis.ua">mazur@ibis.ua</a>
 **/
public class MemoryUtils {

    public static void printMemoryStat() {

        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        System.out.println("###############   Heap utilization statistics [MB]   ###############");
        System.out.println(String.format(
                "Total Memory=%d, Used Memory=%d, Free Memory=%d, Max Memory=%d ",
                totalMemory / mb,
                (totalMemory - freeMemory) / mb,
                freeMemory / mb,
                maxMemory / mb));
        System.out.println("####################################################################");
    }
}
