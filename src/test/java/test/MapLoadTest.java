package test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.PartitionAwareKey;

/**
 * Copyright (c) 2015 Twilio inc.
 * @author tdominique
 *
 */
public class MapLoadTest implements MapLoader<PartitionAwareKey<String, String>, String>{

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * This tests tries to get an entry that doesn't exist in the map with a MapLoader that takes time. 
     * At the same time trying to get another existing entry in the same partition.
     * According to the documentation the map loader should run in a different thread than the partition thread
     * and not block the entire partition while loading one entry.
     */
    @Test
    public void testSlowMapLoader() throws Exception {
        final Config cfg = new Config();
        cfg.addMapConfig(new MapConfig("test").setMapStoreConfig(new MapStoreConfig()
            .setClassName(MapLoadTest.class.getCanonicalName())));
        cfg.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList("127.0.0.1")).setEnabled(true);
        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        final IMap<PartitionAwareKey<String, String>, String> map1 = instance1.getMap("test");
        map1.putTransient(new PartitionAwareKey<>("a", "p"), "A", 0, TimeUnit.SECONDS);
        //Entry "a" now exists in the map. Try to get "b", which will trigger the map loader, and then get "a"
        // in two different threads.
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final Future<String> futureB = executor.submit(() -> map1.get(new PartitionAwareKey<>("b", "p")));
        final Future<String> futureA = executor.submit(() -> map1.get(new PartitionAwareKey<>("a", "p")));
        System.out.println("submitted get jobs.");
        Thread.sleep(10);
        System.out.println("Now let's see if they're done.");
        assertTrue("What? Getting \"a\" is not done after 10ms. This is very bad!", futureA.isDone());
        assertEquals("A", futureA.get());
        assertTrue(futureB.isDone());
        assertEquals("whatever", futureB.get());
    }

    /* (non-Javadoc)
     * @see com.hazelcast.core.MapLoader#load(java.lang.Object)
     */
    public String load(PartitionAwareKey<String, String> key) {
        System.out.println("Loading key " + key + " which will take a loooooong time...");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "whatever";
    }

    /* (non-Javadoc)
     * @see com.hazelcast.core.MapLoader#loadAll(java.util.Collection)
     */
    public Map<PartitionAwareKey<String, String>, String> loadAll(Collection<PartitionAwareKey<String, String>> keys) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.hazelcast.core.MapLoader#loadAllKeys()
     */
    public Set<PartitionAwareKey<String, String>> loadAllKeys() {
        return null;
    }

}
