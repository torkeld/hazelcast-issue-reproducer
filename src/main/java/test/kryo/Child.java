package test.kryo;

public class Child {
    private String gender;
    private int weight;
    
    public Child(){}
    public Child(String gender, int weight) {
        this.setGender(gender);
        this.setWeight(weight);
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String toString() {
        return gender + " that weighs " + weight;
    }
    public int getWeight() {
        return weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
}
