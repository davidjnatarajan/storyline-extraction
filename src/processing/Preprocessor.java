package processing;

import tools.ReutersXMLHandler;

public class Preprocessor {

    // In this case all preprocessing is done via the ReutersXMLHandler class.
    // If a corpus with a different format is used, the user must make their own preprocessing methods.
    public static void preprocess(String xmlCorpusPath, String textCorpusPath, String unwantedTopicsPath, String stopwordPath) {
        ReutersXMLHandler xmlHandler = new ReutersXMLHandler();
        xmlHandler.extractDocumentText(xmlCorpusPath, textCorpusPath, unwantedTopicsPath, stopwordPath);
    }

}
