package test;

import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;

/**
 * @author tdominique
 *
 */
public class DeleteEntryProcessor extends AbstractEntryProcessor<String, Data> {

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see com.hazelcast.map.EntryProcessor#process(java.util.Map.Entry)
     */
    public Object process(Entry<String, Data> entry) {
        System.out.println("Deleting entry: " + entry);
        entry.setValue(null);
        return true;
    }

}
