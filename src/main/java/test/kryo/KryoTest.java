package test.kryo;

import java.util.Arrays;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class KryoTest {

    public static void main(final String[] args) {
        Config conf = new Config();
        SerializerConfig c = new SerializerConfig();
        c.setClass(PersonKryoSerializer.class);
        c.setTypeClass(Person.class);
        conf.getSerializationConfig().addSerializerConfig(c);
        conf.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList("127.0.0.1")).setEnabled(true);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(conf);
        IMap<String, Person> map = hz.getMap("test");
        Person p = new Person("Kalle", "Bajs");
        while (true) {
            System.out.println("Putting: " + p);
            map.put("apa", p);
            System.out.println("Got: " + map.get("apa"));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
