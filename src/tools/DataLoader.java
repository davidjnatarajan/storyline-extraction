/*
The DataLoader is meant to load the topic and mapping data produced as an output of Topic Detection and Tracking System program refferenced in this Article:
https://link.springer.com/chapter/10.1007/978-3-319-69365-1_15
 */

package tools;

import data.TopicWave;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataLoader {

    public List<TopicWave> loadTopicDataAndMappings(String topicsPath, String mappingsPath) {
        List<TopicWave> topicRiver = new ArrayList<>();
        try {
            BufferedReader file = new BufferedReader(new FileReader(topicsPath));
            String terms;

            while ((terms = file.readLine()) != null) {
                topicRiver.add(new TopicWave(terms.split(";")));
            }
        } catch(IOException e) {
            System.out.println("There is no file at this location, please enter a valid path to the previously generated topic file");
        }
        try {
            BufferedReader file = new BufferedReader(new FileReader(mappingsPath));
            String day;

            while ((day = file.readLine()) != null) {
                //split by topic and remove percentages
                String[] temp = day.split(";");
                for (int i = 0 ; i < temp.length - 1; i++) {
                    List<String> prepared = new ArrayList<>(Arrays.asList(temp[i].split(":")[1].replaceAll("\\s+","").replaceAll(",+",",").split(",")));
                    prepared.removeAll(Collections.singleton(null));
                    prepared.removeAll(Collections.singleton(""));
                    topicRiver.get(i).addArticles(prepared);
                }
            }
        } catch(IOException e) {
            System.out.println("There is no file at this location, please enter a valid path to the previously generated mappings file");
        }
        return topicRiver;
    }

}