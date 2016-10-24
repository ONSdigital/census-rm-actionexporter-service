package uk.gov.ons.ctp.response.action.export.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.ActionRequestDocument;
import uk.gov.ons.ctp.response.action.export.domain.SftpMessage;
import uk.gov.ons.ctp.response.action.export.repository.ActionRequestRepository;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;
import uk.gov.ons.ctp.response.action.export.service.TemplateService;
import uk.gov.ons.ctp.response.action.export.service.TransformationService;

/**
 * The implementation of TransformationService
 */
@Named
@Slf4j
public class TransformationServiceImpl implements TransformationService {

  public static final String ERROR_RETRIEVING_FREEMARKER_TEMPLATE = "Could not find FreeMarker template.";

  private static final String TEMPLATE_MAPPING = "templateMapping";

  @Inject
  private freemarker.template.Configuration configuration;

  @Inject
  private ActionRequestRepository actionRequestRepo;

  @Inject
  private TemplateService templateService;

  @Inject
  private TemplateMappingService templateMappingService;

  @Override
  public File fileMe(List<ActionRequestDocument> actionRequestDocumentList, String templateName, String path)
      throws CTPException {
    File resultFile = new File(path);
    Writer fileWriter = null;
    try {
      Template template = giveMeTemplate(templateName);
      fileWriter = new FileWriter(resultFile);
      template.process(buildDataModel(actionRequestDocumentList), fileWriter);
    } catch (IOException e) {
      log.error("IOException thrown while templating for file...", e.getMessage());
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
    } catch (TemplateException f) {
      log.error("TemplateException thrown while templating for file...", f.getMessage());
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, f.getMessage());
    } finally {
      if (fileWriter != null) {
        try {
          fileWriter.close();
        } catch (IOException e) {
          log.error("IOException thrown while closing the file writer...", e.getMessage());
        }
      }
    }

    return resultFile;
  }

  @Override
  public ByteArrayOutputStream streamMe(List<ActionRequestDocument> actionRequestDocumentList, String templateName)
      throws CTPException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writer outputStreamWriter = null;
    try {
      Template template = giveMeTemplate(templateName);
      outputStreamWriter = new OutputStreamWriter(outputStream);
      template.process(buildDataModel(actionRequestDocumentList), outputStreamWriter);
      outputStreamWriter.close();
    } catch (IOException e) {
      log.error("IOException thrown while templating for stream...", e.getMessage());
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, e.getMessage());
    } catch (TemplateException f) {
      log.error("TemplateException thrown while templating for stream...", f.getMessage());
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, f.getMessage());
    } finally {
      if (outputStreamWriter != null) {
        try {
          outputStreamWriter.close();
        } catch (IOException e) {
          log.error("IOException thrown while closing the output stream writer...", e.getMessage());
        }
      }
    }

    return outputStream;
  }

  @Override
  public SftpMessage applyTemplatesStreamMe() {
    Map<String, ByteArrayOutputStream> outputStreams = new HashMap<String, ByteArrayOutputStream>();
    Map<String, List<String>> actionIds = new HashMap<String, List<String>>();
    SftpMessage sftpMessage = new SftpMessage(actionIds, outputStreams);
    String timeStamp = new SimpleDateFormat("ddMMyyyy_HH:mm").format(Calendar.getInstance().getTime());
    List<ActionRequestDocument> requests = actionRequestRepo.findByDateSentIsNullOrderByActionTypeDesc();
    if (requests.isEmpty()) {
      log.warn("No Action Export requests to process.");
      return sftpMessage;
    }
    Map<String, String> mapping = templateMappingService.retrieveMaoFromTemplateMappingDocument(TEMPLATE_MAPPING);
    Map<String, Map<String, List<ActionRequestDocument>>> templateRequests = requests.stream()
        .collect(Collectors.groupingBy(ActionRequestDocument::getActionPlan,
            Collectors.groupingBy(ActionRequestDocument::getActionType)));
    templateRequests.forEach((actionPlan, actionPlans) -> {
      actionPlans.forEach((actionType, actionRequests) -> {
        if (mapping.containsKey(actionType)) {
          try {
            outputStreams.put(actionPlan + "_" + actionType + "_" + timeStamp + ".csv",
                streamMe(actionRequests, mapping.get(actionType)));
            List<String> addActionIds = new ArrayList<String>();
            actionIds.put(actionPlan + "_" + actionType + "_" + timeStamp + ".csv", addActionIds);
            actionRequests.forEach((actionRequest) -> {
              addActionIds.add(actionRequest.getActionId().toString());
            });
          } catch (CTPException e) {
            log.error("Error generating actionType : {}.", actionType);
          }
        } else {
          log.warn("No mapping for actionType : {}.", actionType);
        }
      });
    });
    return sftpMessage;
  }

  /**
   * This returns the FreeMarker template required for the transformation.
   *
   * @param templateName the FreeMarker template to use
   * @return the FreeMarker template
   * @throws IOException if issue creating the FreeMarker template
   * @throws CTPException if problem getting Freemarker template with name given
   */
  private Template giveMeTemplate(String templateName) throws CTPException, IOException {
    log.debug("Entering giveMeTemplate with templateName {}", templateName);
    Template template = configuration.getTemplate(templateName);
    log.debug("template = {}", template);
    if (template == null) {
      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, ERROR_RETRIEVING_FREEMARKER_TEMPLATE);
    }
    return template;
  }

  /**
   * This builds the data model required by FreeMarker
   *
   * @param actionRequestDocumentList the list of action requests
   * @return the data model map
   */
  private Map<String, Object> buildDataModel(List<ActionRequestDocument> actionRequestDocumentList) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("actionRequests", actionRequestDocumentList);
    return result;
  }
}
