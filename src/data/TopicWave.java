/*
A TopicWave is a collection of articles that share a relation to the set of terms defined by the topic
 */

package data;

import edu.stanford.nlp.pipeline.Annotation;

import java.util.ArrayList;
import java.util.List;

public class TopicWave {

    private String[] topic;
    private List<String> articles;
    private List<Annotation> annotations;

    public TopicWave(String[] newTopic) {
        this.topic = newTopic;
        this.articles = new ArrayList<>();
        this.annotations = new ArrayList<>();
    }

    public void addArticles(List<String> newArticles) {
        this.articles.addAll(newArticles);
    }

    public String[] getTopic() {
        return this.topic;
    }

    public List<String> getArticles() {
        return this.articles;
    }

    public List<Annotation> getAnnotations() { return this.annotations; }
}