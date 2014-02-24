package test;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public class AssertNotNullProcessor implements EntryProcessor {

    public Object process(Entry entry) {
        assertNotNull(entry);
        assertNotNull(entry.getValue());
        return true;
    }

    public EntryBackupProcessor getBackupProcessor() {
        // TODO Auto-generated method stub
        return null;
    }

}
