package tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
This class extracts the relevant text information from the original .xml news articles of the RCV1 corpus
 */

public class ReutersXMLHandler {

    public void extractDocumentText(String inputPath, String outputPath, String unwantedTopicsPath, String stopwordPath) {

        // find all the .xml files in the corpus directory
        List<String> filePaths = findXMLfiles(inputPath);

        // for each .xml file
        for (String filePath : filePaths) {
            try {
                Boolean acceptArticle = true;
                StringBuilder textBuilder = new StringBuilder();

                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new File(filePath));

                // normalize text representation
                doc.getDocumentElement().normalize();

                // filter unwanted articles by Reuters predefined topics
//                NodeList metaNodes = doc.getElementsByTagName("codes");
//                for (int mNode = 0; mNode < metaNodes.getLength(); mNode++) {
//                    Node codesNode = metaNodes.item(mNode);
//                    if (codesNode.getAttributes().getNamedItem("class").getNodeValue().equals("bip:topics:1.0")) {
//
//                        Element codesElement = (Element) codesNode;
//                        NodeList codeNodes = codesElement.getElementsByTagName("code");
//
//                        for (int cNode = 0; cNode < codeNodes.getLength(); cNode++) {
//                            Node codeNode = codeNodes.item(cNode);
//                            if (matchesUnwantedTopic(codeNode.getAttributes().getNamedItem("code").getNodeValue(), unwantedTopicsPath)) {
//                                System.out.println(filePath + "is unwanted");
//                                acceptArticle = false;
//                            }
//                        }
//                    }
//                }

                // filter unwanted articles by checking for stopwords in the headline
                NodeList headlineNodes = doc.getElementsByTagName("headline");
                for (int iNode = 0; iNode < headlineNodes.getLength(); iNode++) {
                    Node headlineNode = headlineNodes.item(iNode);

                    if (headlineNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element headlineElement = (Element) headlineNode;
                        if (containsStopWords(headlineElement.getTextContent(), stopwordPath)) {
                            acceptArticle = false;
                        }
                    }
                }

                // If the article is acceptable, extract text information from header and main body
                if (acceptArticle) {
                    System.out.println(filePath + "is accepted");

                    // header info
                    for (int iNode = 0; iNode < headlineNodes.getLength(); iNode++) {
                        Node headlineNode = headlineNodes.item(iNode);

                        if (headlineNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element headlineElement = (Element) headlineNode;
                            textBuilder.append(headlineElement.getTextContent()).append("\n");
                        }
                    }


                    // main body info
                    NodeList textNodes = doc.getElementsByTagName("text");
                    for (int iNode = 0; iNode < textNodes.getLength(); iNode++) {
                        Node textNode = textNodes.item(iNode);

                        if (textNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element textElement = (Element) textNode;

                            NodeList pNodes = textElement.getElementsByTagName("p");
                            for (int jNode = 0; jNode < pNodes.getLength(); jNode++) {
                                Node pNode = pNodes.item(jNode);

                                if (pNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element pElement = (Element) pNode;
                                    String content = pElement.getTextContent();
                                    // do not write the paragraph if it includes the word "newsroom"
                                    if (!content.toLowerCase().contains("newsroom")) {
                                        textBuilder.append(content).append(" ");
                                    }
                                }
                            }
                        }
                    }
                    String text = textBuilder.toString();

                    // regex cleaning
                    // remove XML character entities
                    text = text.replaceAll("&\\S+;", "");
                    // replace ampersand with and
                    text = text.replaceAll("&", "and");
                    // remove .com
                    text = text.replaceAll("\\.com", "");
                    // remove commas between digits
                    //  text = text.replaceAll("(?<=[\\\\d])(,)(?=[\\\\d])", "");
                    String regex = "(?<=[\\d])(,)(?=[\\d])";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(text);
                    text = m.replaceAll("");
                    // remove unwanted special characters
                    text = text.replaceAll("[^A-Za-z0-9 ,.?!\\n]","");

                    String outputFile = Paths.get(filePath).getFileName().toString().replace("xml","txt");
                    String date = Paths.get(filePath).getParent().getFileName().toString();

                    // write the extracted and cleaned text to a new .txt file
                    writeFile(outputPath + "\\" + date, outputFile, text);
                }
            } catch (SAXException|IOException|ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFile(String outputDirectory, String documentName, String documentText) {
        Path parentDir = Paths.get(outputDirectory);
        if (!Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDirectory + "\\" + documentName))) {
            writer.write(documentText, 0, documentText.length());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private boolean matchesUnwantedTopic(String topicCode, String unwantedTopicsPath) {
        Path unwantedTopics = Paths.get(unwantedTopicsPath);
        List<String> codes = new ArrayList<>();
        try {
            codes = Files.readAllLines(unwantedTopics);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String code : codes) {
            //System.out.println(code);
            if (topicCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private List<String> findXMLfiles(String rootDirectory) {
        List<String> filePaths = new ArrayList<>();
        try {
            Finder finder = new Finder("*.xml", true);
            Files.walkFileTree(Paths.get(rootDirectory), finder);
            filePaths = finder.getFilePaths();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePaths;
    }

    private Boolean containsStopWords(String text, String stopwordPath) {
        Boolean containstopword = false;
        Path stopwordsFile = Paths.get(stopwordPath);
        List<String> stopwords = new ArrayList<>();
        try {
            stopwords = Files.readAllLines(stopwordsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String stopword : stopwords) {
            if (text.toLowerCase().contains(stopword)) {
                containstopword = true;
                System.out.println("contained stop phrase: " + stopword);
            }
        }
        return containstopword;
    }

}