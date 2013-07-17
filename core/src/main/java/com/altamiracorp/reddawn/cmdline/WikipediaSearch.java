package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.util.RKPatternMatcher;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class WikipediaSearch extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaSearch.class.getName());
    private static final int TOTAL_PAGE_COUNT = 13640000;
    private final XPathExpression titleXPath;
    private File outputDirectory;
    private File inputFile;
    private RKPatternMatcher patternMatcher;
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    private Object outputLock = new Object();
    private LinkedBlockingQueue<Work> workQueue = new LinkedBlockingQueue<Work>(10000);
    private LinkedBlockingQueue<ArrayList<XMLEvent>> xmlEventArrayPool = new LinkedBlockingQueue<ArrayList<XMLEvent>>(100);
    private int pageCount;
    private int writtenPageCount;
    private Date startTime;
    private boolean complete;

    public WikipediaSearch() throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        titleXPath = xpath.compile("/page/title/text()");
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new WikipediaSearch(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) throws IOException {
        if (cmd.getOptionValue("in") == null) {
            throw new RuntimeException("No input file specified");
        }
        this.inputFile = new File(cmd.getOptionValue("in"));
        if (!this.inputFile.exists()) {
            throw new RuntimeException("Input file does not exist");
        }

        if (cmd.getOptionValue("outdir") == null) {
            throw new RuntimeException("No output directory specified");
        }
        this.outputDirectory = new File(cmd.getOptionValue("outdir"));
        this.outputDirectory.mkdirs();

        if (cmd.getOptionValue("wordlist") == null) {
            throw new RuntimeException("No wordlist file specified");
        }
        File wordListFile = new File(cmd.getOptionValue("wordlist"));

        String[] patterns = readWordList(wordListFile);
        this.patternMatcher = new RKPatternMatcher(patterns);
    }

    private String[] readWordList(File wordListFile) throws IOException {
        InputStream wordListFileIn = new FileInputStream(wordListFile);
        BufferedReader wordListReader = new BufferedReader(new InputStreamReader(wordListFileIn));
        String line;
        ArrayList<String> words = new ArrayList<String>();
        while ((line = wordListReader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            words.add(line);
        }
        return words.toArray(new String[0]);
    }

    @Override
    protected Options getOptions() {
        Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withLongOpt("in")
                        .withDescription("The wikipedia file")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("outdir")
                        .withDescription("The output directory")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("directory")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("wordlist")
                        .withDescription("The file containing the word list")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("wordlistFile")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        complete = false;
        InputStream in = new FileInputStream(this.inputFile);
        if (this.inputFile.getName().endsWith("bz2")) {
            in = new BZip2CompressorInputStream(in);
        }

        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();

                    while (!complete) {
                        Work work = workQueue.take();
                        if (complete) {
                            break;
                        }

                        StringWriter writer = new StringWriter();
                        XMLEventWriter xmlEventWriter = xmlOutputFactory.createXMLEventWriter(writer);
                        for (XMLEvent event : work.getXmlEvents()) {
                            xmlEventWriter.add(event);
                        }
                        String pageXml = writer.toString();

                        if (shouldAdd(pageXml)) {
                            writtenPageCount++;
                            addPage(pageXml);
                            printProgressBar();
                        }
                        pageCount++;
                        if ((pageCount % 100) == 0) {
                            printProgressBar();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("workerThread error", e);
                }
            }
        });
        workerThread.start();

        Thread xmlEventArrayPoolThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!complete) {
                        xmlEventArrayPool.put(new ArrayList<XMLEvent>(10000));
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("xmlEventArrayPoolThread error", e);
                }
            }
        });
        xmlEventArrayPoolThread.start();

        LOGGER.info("Starting search of " + this.inputFile.getPath());

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(in);
        ArrayList<XMLEvent> xmlEvents = null;
        pageCount = 0;
        writtenPageCount = 0;
        startTime = new Date();
        while (xmlEventReader.hasNext()) {
            XMLEvent e = xmlEventReader.nextEvent();
            if (isStartPageEvent(e)) {
                xmlEvents = xmlEventArrayPool.take();
                xmlEvents.add(xmlEventFactory.createStartDocument());
                QName name = new QName("http://www.mediawiki.org/xml/export-0.8/", "page");
                ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
                namespaces.add(xmlEventFactory.createNamespace("http://www.mediawiki.org/xml/export-0.8/"));
                ArrayList<Attribute> attributes = new ArrayList<Attribute>();
                attributes.add(xmlEventFactory.createAttribute("version", "0.8"));
                xmlEvents.add(xmlEventFactory.createStartElement(name, attributes.iterator(), namespaces.iterator()));
            } else if (xmlEvents != null) {
                xmlEvents.add(e);
            }
            if (xmlEvents != null && isEndPageEvent(e)) {
                xmlEvents.add(xmlEventFactory.createEndDocument());
                workQueue.put(new Work(xmlEvents));
                xmlEvents = null;
            }
        }
        complete = true;

        // force threads to loop one more time
        workQueue.put(new Work(null));
        xmlEventArrayPool.take();
        synchronized (workerThread) {
            workerThread.wait();
        }
        synchronized (xmlEventArrayPoolThread) {
            xmlEventArrayPoolThread.wait();
        }

        System.out.println();
        System.out.println("page count: " + pageCount);
        System.out.println("written page count: " + writtenPageCount);

        return 0;
    }

    private void printProgressBar() {
        long time = new Date().getTime() - startTime.getTime();
        double percentComplete = ((double) pageCount) / ((double) TOTAL_PAGE_COUNT);
        int totalBarLength = 50;
        int completeBarLength = (int) (percentComplete * (double) totalBarLength);
        int toWorkOnBarLength = totalBarLength - completeBarLength;
        double rate = (double) pageCount / (double) time * 1000.0;
        synchronized (outputLock) {
            System.out.print('\r');
            for (int i = 0; i < completeBarLength; i++) {
                System.out.print('=');
            }
            if (completeBarLength != totalBarLength) {
                System.out.print('>');
            }
            for (int i = 0; i < toWorkOnBarLength; i++) {
                System.out.print(' ');
            }
            System.out.printf("%.1f%%  %d/%d %.1f pages/s (found: %d)", percentComplete * 100.0, pageCount, TOTAL_PAGE_COUNT, rate, writtenPageCount);
        }
    }

    private void addPage(String pageXml) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document dom = documentBuilder.parse(new InputSource(new StringReader(pageXml)));
        String fileName = (String) titleXPath.evaluate(dom, XPathConstants.STRING);
        fileName = fileName.replaceAll(":", "_");
        fileName = fileName.replaceAll("/", "_");
        fileName = fileName.replaceAll(" ", "_");
        File xmlFile = new File(this.outputDirectory, fileName + ".xml");

        FileOutputStream xmlOut = new FileOutputStream(xmlFile);
        xmlOut.write(pageXml.getBytes());
        xmlOut.close();
    }

    private boolean shouldAdd(String pageXml) {
        return this.patternMatcher.hasMatch(pageXml.toLowerCase());
    }

    private boolean isEndPageEvent(XMLEvent e) {
        if (!e.isEndElement()) {
            return false;
        }
        EndElement endElement = (EndElement) e;
        return endElement.getName().getLocalPart().equals("page");
    }

    private boolean isStartPageEvent(XMLEvent e) {
        if (!e.isStartElement()) {
            return false;
        }
        StartElement startElement = (StartElement) e;
        return startElement.getName().getLocalPart().equals("page");
    }

    private static class Work {

        private final ArrayList<XMLEvent> xmlEvents;

        public Work(ArrayList<XMLEvent> xmlEvents) {
            this.xmlEvents = xmlEvents;
        }

        private ArrayList<XMLEvent> getXmlEvents() {
            return xmlEvents;
        }
    }
}
