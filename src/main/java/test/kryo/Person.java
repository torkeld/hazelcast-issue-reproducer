package test.kryo;

import java.util.Arrays;
import java.util.List;

public class Person {
    private String first;
    private String last;
    private int age = 2;
    private List<Child> children = Arrays.asList(new Child("girl", 20), new Child("boy", 30));
    
    public Person(){}
    
    public Person(final String first, final String last) {
        this.setFirst(first);
        this.setLast(last);
    }
    
    public String toString() {
        return getFirst() + " " + getLast() + " is " + getAge() + "years old and has a " + children.get(0) + " and a " + children.get(1);
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<Child> getChildren() {
        return children;
    }
}
