package test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ExecuteOnKeysTest {

    /**
     * Reproducer for https://github.com/hazelcast/hazelcast/issues/1764 
     * When executing on keys with object in-memory format with a processor 
     * that updates the entry, you get a ClassCastException
     */
    @Test
    public void testUpdate() {
        Config cfg = new Config();
        cfg.getMapConfig("test").setInMemoryFormat(InMemoryFormat.OBJECT);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        //HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance1.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.put("b", new Data("abc", "123"));
            Set<String> keys = new HashSet<String>();
            keys.add("a");
            map.executeOnKeys(keys, new UpdatingEntryProcessor("test"));
        } finally {
            instance1.shutdown();
          //  instance2.shutdown();
        }
    }

    /**
     * Reproducer for https://github.com/hazelcast/hazelcast/issues/???? 
     * When executing on keys, any keys that don't exist cause the entry
     * processor to be executed with null for each value (verify this).
     */
    @Test
    public void testNotExist() {
        Config cfg = new Config();
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        //HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance1.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.put("b", new Data("abc", "123"));
            Set<String> keys = new HashSet<String>();
            keys.add("c");
            keys.add("d");
            map.executeOnKeys(keys, new UpdatingEntryProcessor("test"));
        } finally {
            instance1.shutdown();
          //  instance2.shutdown();
        }
    }
}
