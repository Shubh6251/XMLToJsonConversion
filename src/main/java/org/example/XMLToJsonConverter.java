package org.example;

import org.json.JSONObject;
import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import static com.sun.tools.javac.resources.CompilerProperties.Warnings.Warning;

public class XMLToJsonConverter {

    private static final Logger LOGGER = Logger.getLogger(XMLToJsonConverter.class.getName());
    private static final String MATCH_SCORE_TAG = "Score";
    private static final int INDENT_FACTOR = 4;

    /**
     * Converts XML file to JSON with a custom field "MatchSummary.TotalMatchScore".
     *
     * @param xmlFilePath Path to the XML file.
     * @return JSONObject representing the XML data with the custom field.
     * @throws Exception if file reading, XML parsing, or JSON conversion fails.
     */
    public static JSONObject convertXmlToJsonWithTotalScore(String xmlFilePath) throws Exception {
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists() || !xmlFile.canRead()) {
            throw new IOException("Cannot read file: " + xmlFilePath);
        }

        String xmlContent = new String(Files.readAllBytes(xmlFile.toPath()));
        Document xmlDoc = parseXml(xmlContent);

        int totalMatchScore = calculateTotalScore(xmlDoc);
        JSONObject jsonObject = XML.toJSONObject(xmlContent);

        // Add custom field for TotalMatchScore
        addTotalMatchScore(jsonObject, totalMatchScore);

        return jsonObject;
    }

    /**
     * Parses XML content into a Document object.
     *
     * @param xmlContent XML content as a String.
     * @return Document object representing the XML.
     * @throws Exception if parsing fails.
     */
    private static Document parseXml(String xmlContent) throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlContent)));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse XML content", e);
            throw e;
        }
    }

    /**
     * Calculates the total score from the specified XML nodes.
     *
     * @param xmlDoc Document object of the XML data.
     * @return int representing the total score.
     * @throws Exception if score data is invalid or exceeds integer limits.
     */
    private static int calculateTotalScore(Document xmlDoc) throws Exception {
        NodeList scoreNodes = xmlDoc.getElementsByTagName(MATCH_SCORE_TAG);
        int totalScore = 0;

        for (int i = 0; i < scoreNodes.getLength(); i++) {
            try {
                String scoreText = scoreNodes.item(i).getTextContent();
                int score = Integer.parseInt(scoreText.trim());

                // Check if adding score exceeds Integer limit
                if (Integer.MAX_VALUE - totalScore < score) {
                    throw new ArithmeticException("Score exceeds Integer limit when summed.");
                }
                totalScore += score;

            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid score format at node index " + i, e);
            }
        }
        LOGGER.log(Level.INFO, "Calculated Total Score: {0}", totalScore);
        return totalScore;
    }

    /**
     * Adds TotalMatchScore to the JSON object under MatchSummary.
     *
     * @param jsonObject JSONObject to add the field to.
     * @param totalScore Total match score to be added.
     */
    private static void addTotalMatchScore(JSONObject jsonObject, int totalScore) {
        try {
            JSONObject matchSummary = new JSONObject();
            matchSummary.put("TotalMatchScore", totalScore);
            jsonObject.getJSONObject("Response").getJSONObject("ResultBlock").put("MatchSummary", matchSummary);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add TotalMatchScore to JSON object", e);
        }
    }

    public static void main(String[] args) {
        String xmlFilePath = "src/main/java/data/data1.xml";
        try {
            JSONObject jsonOutput = convertXmlToJsonWithTotalScore(xmlFilePath);
            System.out.println(jsonOutput.toString(INDENT_FACTOR));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Conversion failed", e);
        }
    }
}

