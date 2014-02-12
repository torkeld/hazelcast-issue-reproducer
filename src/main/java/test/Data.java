package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class Data implements DataSerializable{
    
    public static AtomicInteger serializationCount = new AtomicInteger();
    public static AtomicInteger deserializationCount = new AtomicInteger();

    private String attr1;
    private String attr2;
    
    public Data() {
        //For deserialization...
    }
    
    public Data(String attr1, String attr2) {
        this.attr1 = attr1;
        this.attr2 = attr2;
    }
    
    public String getAttr1() {
        return attr1;
    }
    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }
    public String getAttr2() {
        return attr2;
    }
    public void setAttr2(String attr2) {
        this.attr2 = attr2;
    }

    @Override
    public String toString() {
        return "[" + attr1 + " " + attr2 + "]";
    }

    public void writeData(ObjectDataOutput out) throws IOException {
        System.out.println("Serializing object " + this);
        serializationCount.incrementAndGet();
        out.writeObject(attr1);
        out.writeObject(attr2);
    }

    public void readData(ObjectDataInput in) throws IOException {
        attr1 = in.readObject();
        attr2 = in.readObject();
        System.out.println("Deserializing object " + this);
        deserializationCount.incrementAndGet();
    }
}
