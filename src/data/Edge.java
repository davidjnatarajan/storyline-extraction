package data;

import java.util.List;

public class Edge {

    private double weight;
    private List<String> facts;

    public Edge(double weight, List<String> facts) {
        this.weight = weight;
        this.facts = facts;
    }

    public double getWeight() {
        return this.weight;
    }

    public List<String> getFacts() {
        return this.facts;
    }

}