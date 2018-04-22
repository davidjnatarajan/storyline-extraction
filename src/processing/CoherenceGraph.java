package processing;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import tools.Finder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/*
This class is meant to provide a means of understanding document relation.
Documents are represented as nodes/vertices in a graph (would like to position them according to publishing date.)
Documents are linked if they are coherent or share similar information.
Coherency is not binary and the links between documents, or the edges in the graph, are weighted. The higher the weight, the more related. (shown in orange boxes.)
Documents also have an importance ranking, determined by the number of neighbours it has. The more neighbours, the more important a document is. (shown as node size.)
Next to each node in the visualisation is an ID, this ID can be used to see which documents are related to which and what are the facts that relate them (as printed in the console.)
 */

public class CoherenceGraph {

    public CoherenceGraph(String factsPath, String corpusPath) {

        Graph graph = new SingleGraph("Coherence Graph");
        graph.setStrict(false);
        graph.setAutoCreate( true );
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        List<String> lines = new ArrayList<>();
        List<String> facts = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> headlines = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        LocalDate start = LocalDate.parse("19960820", BASIC_ISO_DATE);
        String documentPath;

        // read facts info per document into list
        try {
            BufferedReader file = new BufferedReader(new FileReader(factsPath));
            String line;
            while ((line = file.readLine()) != null) {
                lines.add(line);
            }
        } catch(IOException e) {
            System.out.println("There is no file at this location, please enter a valid path to the previously generated facts file");
        }

        // set adjacency matrix size to number of documents
//        adjacencyMatrix = new Edge[lines.size()][lines.size()];
//        documents = new ArrayList<>();

        // for each document:
        // - save document info
        // - check subsequent documents for similar facts
        for (int i = 0; i < lines.size(); i++) {
            String[] line1 = lines.get(i).split(",");
            names.add(line1[0]);
            documentPath = findDocument(line1[0], corpusPath);
            headlines.add(getHeadline(documentPath));
            dates.add(LocalDate.parse(line1[1], BASIC_ISO_DATE));
            for (int j = i+1; j < lines.size(); j++) {
                String[] line2 = lines.get(j).split(",");
                for (int x = 2; x < line1.length; x++) {
                    for (int y = 2; y < line2.length; y++) {
                        if (line1[x].equals(line2[y])) {
                            facts.add(line1[x]);
                        }
                    }
                }
                if (facts.size() > 0) {
                    // determine the inter-document coherence represented as weight
                    // here weight is calculated to be the number of facts the two documents share normalised and averaged
                    // this can be replaced by some other function/metric
                    double coherence = facts.size();
                    String node1 = Integer.toString(i);
                    String node2 = Integer.toString(j);
                    String edge = node1 + " " + node2;
                    graph.addEdge(edge, node1, node2);
                    System.out.println(node1 + "has" + (line1.length-2) + " " + node2 + "has" + (line2.length-2) + " together" + facts.size());
                    int ratio = (facts.size()*100)/(((line1.length-2)+(line2.length-2))/2); //((facts.size()/((line1.length-2)+(line2.length-2))/2)*100);
                    graph.getEdge(edge).setAttribute("ui.label", ratio + "%" + " of facts are shared by node " + node1 + " and " + node2);
                    graph.getEdge(edge).setAttribute("facts", facts);
                    graph.getEdge(edge).setAttribute("coherence", coherence);
                    // fill info into symmetrical adjacency matrix
//                    adjacencyMatrix[i][j] = new Edge(weight, facts);
//                    adjacencyMatrix[j][i] = new Edge(weight, facts);
                }
                facts = new ArrayList<>();
            }
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (Node n : graph) {
            n.setAttribute("ui.label", n.getId() + " " + headlines.get(Integer.parseInt(n.getId())));
            System.out.println((String) n.getAttribute("ui.label"));
            n.setAttribute("ui.size", n.getDegree() * 5);
            n.setAttribute("documentName", names.get(Integer.parseInt(n.getId())));
            int daysSince = (int) ChronoUnit.DAYS.between(start, dates.get(Integer.parseInt(n.getId())));
            Integer numberOfDocs = map.putIfAbsent(daysSince, 1);
            if (numberOfDocs != null) {
                n.setAttribute("y", numberOfDocs);
                map.replace(daysSince, numberOfDocs + 1);
            } else {
                n.setAttribute("y", 0);
            }
            n.setAttribute("x", daysSince);
//            System.out.println(n.getAttribute("x") + " " + n.getAttribute("y"));
//            documents.add(new Node(counter, names.get(i), dates.get(i)));
        }

        for (Edge e : graph.getEdgeSet()) {
            System.out.println((String) e.getAttribute("ui.label"));
            System.out.println("------------------");
            List<String> facts1 = e.getAttribute("facts");
            for (String aFacts1 : facts1) {
                System.out.println(aFacts1);
            }
            System.out.println();
        }

        String styleSheet = "node {" +
                            "	size-mode: dyn-size;" +
                            "   text-size: 20;" +
                            "   text-alignment: under;" +
                            "}" +
                            "edge {" +
                            //"   shape:cubic-curve;" +
                            "   text-size: 30;" +
                            "   text-padding: 3px, 2px; text-background-mode: rounded-box; text-background-color: #EB2; text-color: #222;" +
                            "}";

        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.stylesheet", styleSheet);

        Viewer viewer = graph.display();
        viewer.disableAutoLayout();
    }

    private String findDocument(String documentName, String rootDirectory) {
        List<String> filePaths = new ArrayList<>();
        try {
            Finder finder = new Finder(documentName, false);
            Files.walkFileTree(Paths.get(rootDirectory), finder);
            filePaths = finder.getFilePaths();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (filePaths.size() > 1) {
            return null;
        } else {
            return filePaths.get(0);
        }
    }

    private String getHeadline(String documentPath) {
        String headline = "";
        try {
            BufferedReader file = new BufferedReader(new FileReader(documentPath));
            headline = file.readLine();
        } catch(IOException e) {
            System.out.println("There is no file at this location, please enter a valid path to the previously generated facts file");
        }
        return headline;
    }

}