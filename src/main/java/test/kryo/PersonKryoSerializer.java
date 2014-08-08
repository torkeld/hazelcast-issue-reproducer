package test.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PersonKryoSerializer implements StreamSerializer<Person> {
    
    private static final ThreadLocal<Kryo> kryoThreadLocal 
            = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(Person.class);
            return kryo;
        }
    };

    public int getTypeId() {
        return 2;
    }

    public void write(ObjectDataOutput objectDataOutput, Person person) 
            throws IOException {
        Kryo kryo = kryoThreadLocal.get();
        Output output = new Output((OutputStream) objectDataOutput);
        kryo.writeObject(output, person.getFirst());
        kryo.writeObject(output, person.getLast());
        kryo.writeObject(output, person.getChildren());
        output.flush();
    }

    public Person read(ObjectDataInput objectDataInput) 
            throws IOException {
        InputStream in = (InputStream) objectDataInput;
        Input input = new Input(in);
        Kryo kryo = kryoThreadLocal.get();
        return new Person(kryo.readObject(input, String.class), kryo.readObject(input, String.class));
    }

    public void destroy() {
    }
}