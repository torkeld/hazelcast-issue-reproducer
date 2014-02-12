package test;

import org.junit.Test;

import static org.junit.Assert.*;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapIndexConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class ExecuteOnEntriesWithPredicateTest {

    /**
     * Reproducer for https://github.com/hazelcast/hazelcast/issues/1719 
     * When executing an entry processor with a predicate the index isn't used
     * for that predicate.
     */
    @Test
    public void test() {
        Config cfg = new Config();
        cfg.getMapConfig("test").addMapIndexConfig(new MapIndexConfig("attr1", false));
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance1.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.put("b", new Data("abc", "123"));
            TestPredicate predicate = new TestPredicate("foo");
            map.executeOnEntries(new LoggingEntryProcessor(), predicate);
            assertFalse("The predicate shouldn't be applied if indexing works!", predicate.didApply());
        } finally {
            instance1.shutdown();
            instance2.shutdown();
        }
    }
}
