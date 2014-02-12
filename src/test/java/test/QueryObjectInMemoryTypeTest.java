package test;

import org.junit.Test;

import static org.junit.Assert.*;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

public class QueryObjectInMemoryTypeTest {

    /**
     * Reproducer for https://github.com/hazelcast/hazelcast/issues/1765 
     * When querying for entries with in-memory-type OBJECT using the built-in
     * predicates (Predicates.AbstractPredicate), the object seems
     * to be serialized as a result of calling getAttribute from the predicate.
     * This seems sub-optimal as the object is available in deserialized form already. 
     */
    @Test
    public void test() {
        Config cfg = new Config();
        cfg.getMapConfig("test").setInMemoryFormat(InMemoryFormat.OBJECT);
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.put("b", new Data("abc", "123"));
            System.out.println("values...");
            map.values(Predicates.equal("attr1", "foo"));
            System.out.println("values again...");
            map.values(Predicates.equal("attr1", "abc"));
            //Each entry should be serialized once and deserialized once.
            assertEquals(2, Data.serializationCount.get());
            assertEquals(2, Data.deserializationCount.get());
        } finally {
            instance.shutdown();
        }
    }
}
