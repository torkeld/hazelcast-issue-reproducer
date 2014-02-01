package test;

import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

public class DeleteEntryTest {

    /**
     * Reproducer for https://github.com/hazelcast/hazelcast/issues/1687 When an
     * entry is deleted in an entry processor it's never removed from the backup
     * store. This test will start two nodes, add an entry, run an entry
     * processor that deletes the entry, and kill the primary node. At this
     * point the backup which still contains the nulled value becomes primary.
     * Now running an entry processor with a predicate will cause an
     * exception...
     */
    @Test
    public void test() {
        Config cfg = new Config();
        cfg.getMapConfig("test").setBackupCount(1);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance1.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.executeOnEntries(new LoggingEntryProcessor(), Predicates.equal("attr1", "foo"));
            map.executeOnEntries(new DeleteEntryProcessor(), Predicates.equal("attr1", "foo"));
            // Now the entry has been removed from the primary store but not the backup.
            // Let's kill the primary and execute the logging processor again...
            String a_member_uiid = instance1.getPartitionService().getPartition("a").getOwner().getUuid();
            HazelcastInstance newPrimary;
            if (a_member_uiid.equals(instance1.getCluster().getLocalMember().getUuid())) {
                instance1.shutdown();
                newPrimary = instance2;
            } else {
                instance2.shutdown();
                newPrimary = instance1;
            }
            IMap<String, Data> map2 = newPrimary.getMap("test");
            map2.executeOnEntries(new LoggingEntryProcessor(), Predicates.equal("attr1", "foo"));
        } finally {
            instance1.shutdown();
            instance2.shutdown();
        }
    }

    /**
     * Same as above but executes the delete processor on key instead of predicate.
     * Triggers the same bug in a different class
     */
    @Test
    public void testNoPred() {
        Config cfg = new Config();
        cfg.getMapConfig("test").setBackupCount(1);
        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(cfg);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(cfg);
        IMap<String, Data> map = instance1.getMap("test");
        try {
            map.put("a", new Data("foo", "bar"));
            map.executeOnEntries(new LoggingEntryProcessor(), Predicates.equal("attr1", "foo"));
            map.executeOnKey("a", new DeleteEntryProcessor());
            // Now the entry has been removed from the primary store but not the backup.
            // Let's kill the primary and execute the logging processor again...
            String a_member_uiid = instance1.getPartitionService().getPartition("a").getOwner().getUuid();
            HazelcastInstance newPrimary;
            if (a_member_uiid.equals(instance1.getCluster().getLocalMember().getUuid())) {
                instance1.shutdown();
                newPrimary = instance2;
            } else {
                instance2.shutdown();
                newPrimary = instance1;
            }
            IMap<String, Data> map2 = newPrimary.getMap("test");
            map2.executeOnEntries(new LoggingEntryProcessor(), Predicates.equal("attr1", "foo"));
        } finally {
            instance2.shutdown();
            instance1.shutdown();
        }
    }
}