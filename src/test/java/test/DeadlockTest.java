package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public class DeadlockTest {

    private static void log(String msg) {
        System.out.println("" + new Date().toString() + " [" + Thread.currentThread().getName() + "] " + msg);
    }
    
    private static class OuterProcessor implements EntryProcessor<String, String>, HazelcastInstanceAware {

        private static final long serialVersionUID = 1L;
        private HazelcastInstance hz;
        private final InnerProcessor ep;

        public OuterProcessor(InnerProcessor ep) {
            this.ep = ep;
        }

        public Object process(Entry<String, String> entry) {
            hz.getMap("test2").executeOnKey(entry.getKey(), ep);
            return null;
            
        }

        public EntryBackupProcessor<String, String> getBackupProcessor() {
            return null;
        }

        public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
            this.hz = hazelcastInstance;
        }
        
    }
    
    private static class InnerProcessor implements EntryProcessor<String, String>, EntryBackupProcessor<String, String> {

        private static final long serialVersionUID = 1L;

        public Object process(Entry<String, String> entry) {
            log("process for " + entry.getKey() +", sleeping 50ms.");
            try {
                //To increase the chance of deadlock we add a sleep here. 
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log("process done for " + entry.getKey());            
            return null;
        }

        public EntryBackupProcessor<String, String> getBackupProcessor() {
            return this;
        }

        public void processBackup(Entry<String, String> entry) {
            log("process backup for " + entry.getKey());            
        }

    }

    private Thread runProcessorThread(final IMap<String, String> map, final String key, final EntryProcessor<String, String> ep) {
        return new Thread(new Runnable() {
            
            public void run() {
                log("Executing ep for " + key);
                map.executeOnKey(key, ep);
                log("Done executing ep for " + key);
            }
        });
    }

    /**
     * Reproducing a deadlock caused by executing an entry processor within another entry processor.
     * The scenario is as follows:
     * 
     * 1. Two entry processors are executed on two different nodes (hence on difference partitions) simultaneously.
     * 2. Each processor calls a sub-processor with the same key, so that the sub-processor is executed  on the same node.
     * 3. When the sub-processor is done, its backup processor will be sent to the other node to be executed.
     * 4. When the backup sub-processor is completed the initial entry processor completes (it has no backup processor).
     * 
     * This scenario will cause a deadlock if the two keys are chosen so that they end up on different partitions and 
     * different nodes but the same thread. After 5s the operation will timeout and the execution will return for both
     * processors.
     * 
     * If only a single entry processor with backup is used (no nested processor) this deadlock doesn't occur.
     * 
     * This test may need to be run several times to reproduce, since it's impossible to deterministically choose keys
     * that will satisfy all the above conditions.
     * 
     * Observe the log output of the inner processor. When the two processors are executed on difference nodes,
     * the deadlock will be triggered:
     * [hz._hzInstance_1_dev.operation.thread-4] process for lihld, sleeping 50ms.
     * [hz._hzInstance_2_dev.operation.thread-4] process for 7uneetb, sleeping 50ms.
     */
    @Test
    public void testDeadlock() {
        Config cfg = new Config();
        cfg.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList("127.0.0.1")).setEnabled(true);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, String> map1 = instance1.getMap("test");
        try {
            log("Launching test.");
            long s1 = System.currentTimeMillis();
//            final Thread t1 = runProcessorThread(map1, "lihld", new InnerProcessor());
            final Thread t1 = runProcessorThread(map1, "lihld", new OuterProcessor(new InnerProcessor()));
            t1.start();
//            final Thread t2 = runProcessorThread(map1, "7uneetb", new InnerProcessor());
            final Thread t2 = runProcessorThread(map1, "7uneetb", new OuterProcessor(new InnerProcessor()));
            t2.start();
            log("Started all threads.");
            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long duration = System.currentTimeMillis() - s1;
            log("Test done in " + duration + " ms.");
            assertTrue("It shouldn't take this long!!!", duration < 5000);
        } finally {
            instance1.shutdown();
            instance2.shutdown();
        }
    }

}
