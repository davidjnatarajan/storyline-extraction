# storyline-extraction
A program for automatic storyline extraction from news corpora.
This program was developed as part of my bachelor thesis.

# Corpus
This program is tailored to work with the Reuters Corpus Volume 1 or RCV1. For the best results it is recommended to only use the RCV1 corpus with this program, however, if you wish to use a different corpus, you must alter ReutersXMLHandler.java and Preprocessor.java to  accomodate your corpus.

The corpus should be organised by date into the "storyline-extraction/lib/*corpus name here*" directory. News items should be organised into directories titled with their respective publishing dates. For example, "20151228/20151228_1.xml".

# Dependencies
For this software to work you must download some external libraries.

Required Libraries:
Stanford CoreNLP - https://stanfordnlp.github.io/CoreNLP/
GraphStream - http://graphstream-project.org/

please download and extract these libraries into "storyline-extraction/lib/"

# Documentation
For more information on how the program works and what it tries to accomplish please read the accompanying article "Bachelor_Thesis.pdf"
