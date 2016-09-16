package uk.gov.ons.ctp.response.action.export.utility;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestDocument;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to investigate FreeMarker
 * TODO Delete once implemented in Spring Boot app and testable
 */
public class TemplateInvestigation {
  public static void main(String[] args) throws IOException, TemplateException {
    /**
     * Step - Get the action requests from the MongoDB
     */
    List<ActionRequestDocument> actionRequestDocumentList = buildMeListOfActionRequestDocuments();
    System.out.println(String.format("We have %d action requests...", actionRequestDocumentList.size()));

    /**
     * Step - Configure FreeMarker
     * Configuration instances are meant to be application-level singletons.
     */
    // Freemarker configuration object - do this only once at the beginning of the application (possibly servlet) life-cycle
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
    cfg.setDirectoryForTemplateLoading(new File("actionexporter/src/main/resources/templates"));  // non-file-system sources are possible too: see setTemplateLoader();
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.setLogTemplateExceptions(false);

    // Build the data model
    Map<String, Object> root = new HashMap<String, Object>();
    root.put("actionRequests", actionRequestDocumentList);

    Template template = cfg.getTemplate("csvExport.ftl"); // Configuration caches Template instances

    // Console output
    Writer out = new OutputStreamWriter(System.out);
    template.process(root, out);
    out.flush();

    // File output
    Writer file = new FileWriter(new File("actionexporter/src/main/resources/forPrinter.csv"));
    template.process(root, file);
    file.flush();
    file.close();
  }

  /**
   * TODO This will be replaced by a actionRequestRepo.findAll or similar
   */
  private static List<ActionRequestDocument> buildMeListOfActionRequestDocuments() {
    List<ActionRequestDocument> result = new ArrayList<>();
    for (int i = 1; i < 51; i++) {
      result.add(buildAMeActionRequestDocument(i));
    }
    return result;
  }

  private static ActionRequestDocument buildAMeActionRequestDocument(int i) {
    ActionRequestDocument result =  new ActionRequestDocument();
    result.setActionId(new BigInteger(new Integer(i).toString()));
    result.setActionType("testActionType");
    result.setIac("testIac");
    result.setAddress(buildActionAddress());
    return result;
  }

  private static ActionAddress buildActionAddress() {
    ActionAddress actionAddress = new ActionAddress();
    actionAddress.setLine1("1 High Street");
    actionAddress.setTownName("Southampton");
    actionAddress.setPostcode("SO16 0AS");
    return actionAddress;
  }
}