/*
Here is the main process for generating news stories
*/

package application;

import processing.CoherenceGraph;
import processing.Preprocessor;

public class Main {

    public static void main(String[] args) {

        // crucial directory and file paths.
        String xmlCorpus = "res\\RCV1_xml_organised";
        String textCorpus = "out\\corpus_cleaning\\RCV1";
        String tdtOutput = "out\\topic_detection_and_tracking\\nmf\\topics_and_mappings";
        String topicList = tdtOutput + "\\topics.csv";
        String topicMappings = tdtOutput + "\\mapping.csv";
        String unwantedTopics = "res\\reutersUnwantedTopics.txt";
        String stopwords = "res\\stopwords.txt";
        String factOutput = "out\\fact_extraction";

        // extract and preprocess original .xml news articles for better results.
//        Preprocessor.preprocess(xmlCorpus, textCorpus, unwantedTopics, stopwords);

        // load topic detection and tracking data.
//        List<TopicWave> topicRiver = new DataLoader().loadTopicDataAndMappings(topicList, topicMappings);

        // extract facts from topically related articles.
//        new FactExtractor().annotateTopicRiver(textCorpus, factOutput, topicRiver);

        new CoherenceGraph(factOutput + "\\facts.csv", textCorpus);
    }

}