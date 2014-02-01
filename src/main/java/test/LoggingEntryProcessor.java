package test;

import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;

public class LoggingEntryProcessor extends AbstractEntryProcessor<String, Data> {

    private static final long serialVersionUID = 1L;

    public Object process(Entry<String, Data> entry) {
        System.out.println("Processing entry " + entry.getValue());
        return true;
    }

}
