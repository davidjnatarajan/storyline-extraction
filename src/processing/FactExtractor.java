package processing;

import data.TopicWave;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import tools.Finder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.temporal.ChronoUnit.DAYS;

public class FactExtractor {

    public void annotateTopicRiver(String corpusPath, String outputPath, List<TopicWave> topicRiver) {
        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "openie.resolve_coref", "true",
                        "ner.useSUTime", "false",
                        "annotators", "tokenize,ssplit,pos,lemma,depparse,parse,natlog,ner,mention,coref,openie"));
//        for (TopicWave topicWave : topicRiver) {
//            coreferencedOpenIE(corpusPath, topicWave, pipeline);
//            System.out.println(topicWave.getArticles().size());
//        }
        coreferencedOpenIE(corpusPath, outputPath, topicRiver.get(0), pipeline);
    }

    private void coreferencedOpenIE(String corpusPath, String outputPath, TopicWave topic, StanfordCoreNLP pipeline) {

        for (int i = 0; i < topic.getArticles().size(); i++) {

            String article = topic.getArticles().get(i);
            write(outputPath, article + ",");
            Path path = Paths.get(findDocument(article, corpusPath));
            write(outputPath, path.getParent().getFileName().toString() + ",");
            String text = "";

            try {
                text = Files.readAllLines(path, StandardCharsets.UTF_8).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Annotation doc = new Annotation(text);
            pipeline.annotate(doc);

            List<String> facts = new ArrayList<>();

            // Loop over sentences in the document
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                // Get the OpenIE triples for the sentence
                // Print the triples
                for (RelationTriple triple : sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class)) {
                    if (triple.confidence == 1) {
                        facts.add(triple.subjectLemmaGloss() + "." + triple.relationLemmaGloss() + "." + triple.objectLemmaGloss());
                    }
                }
            }

            write(outputPath, String.join(",", facts));
            writeNewLine(outputPath);

            if (i < topic.getArticles().size() - 1) {
                LocalDate date1 = LocalDate.parse(path.getParent().getFileName().toString(), BASIC_ISO_DATE);
                LocalDate date2 = LocalDate.parse(Paths.get(findDocument(topic.getArticles().get(i+1),corpusPath)).getParent().getFileName().toString(), BASIC_ISO_DATE);
                if (DAYS.between(date1,date2) > 1) {
                    return;
                }
            }
        }
    }

    private String findDocument(String documentName,String rootDirectory) {
        String filePath = rootDirectory;
        try {
            Finder finder = new Finder(documentName, false);
            Files.walkFileTree(Paths.get(rootDirectory), finder);
            filePath = finder.getFilePaths().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private void write(String outputPath, String text) {
        Path facts = Paths.get(outputPath + "\\facts.csv");
        Charset charset = Charset.forName("UTF-8");
        try (BufferedWriter writer = Files.newBufferedWriter(facts, charset, APPEND, CREATE, WRITE)) {
            writer.write(text);
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private void writeNewLine(String outputPath) {
        Path facts = Paths.get(outputPath + "\\facts.csv");
        Charset charset = Charset.forName("UTF-8");
        try (BufferedWriter writer = Files.newBufferedWriter(facts, charset, APPEND, CREATE, WRITE)) {
            writer.newLine();
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

}