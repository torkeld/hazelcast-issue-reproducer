package test;

import java.util.Map.Entry;

import com.hazelcast.map.AbstractEntryProcessor;

public class UpdatingEntryProcessor extends AbstractEntryProcessor<String, Data> {

    private static final long serialVersionUID = 1L;
    private String newValue;
    
    public UpdatingEntryProcessor(String newValue) {
        this.newValue = newValue;
    }

    public Object process(Entry<String, Data> entry) {
        System.out.println("Updating entry " + entry.getValue());
        Data data = entry.getValue();
        data.setAttr1(newValue);
        entry.setValue(data);
        return true;
    }

}
