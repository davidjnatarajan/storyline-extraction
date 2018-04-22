package data;

import java.time.LocalDate;

public class Node {

    private double relevence;
    private String documentName;
    private LocalDate documentDate;

    public Node(double relevence, String documentName, LocalDate documentDate) {
        this.relevence = relevence;
        this.documentName = documentName;
        this.documentDate = documentDate;
    }

    public double getRelevence() {
        return this.relevence;
    }

    public String getDocumentName() {
        return this.documentName;
    }

    public LocalDate getDocumentDate() {
        return this.documentDate;
    }
}
