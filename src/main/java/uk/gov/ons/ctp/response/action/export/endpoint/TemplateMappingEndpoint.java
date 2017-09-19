package uk.gov.ons.ctp.response.action.export.endpoint;

import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.representation.TemplateMappingDTO;
import uk.gov.ons.ctp.response.action.export.service.TemplateMappingService;

import java.net.URI;
import java.util.List;

/**
 * The REST endpoint controller for TemplateMappings.
 */
@RestController
@RequestMapping(value = "/templatemappings", produces = "application/json")
@Slf4j
public class TemplateMappingEndpoint {
  @Autowired
  private TemplateMappingService templateMappingService;

  @Qualifier("actionExporterBeanMapper")
  @Autowired
  private MapperFacade mapperFacade;

  /**
   * To retrieve all TemplateMappings
   *
   * @return a list of TemplateMappings
   */
  @RequestMapping(method = RequestMethod.GET)
  public List<TemplateMappingDTO> findAllTemplateMappings() {
    log.debug("Entering findAllTemplateMappings ...");
    List<TemplateMapping> templateMappings = templateMappingService
        .retrieveAllTemplateMappings();
    List<TemplateMappingDTO> results = mapperFacade.mapAsList(templateMappings,
        TemplateMappingDTO.class);
    return CollectionUtils.isEmpty(results) ? null : results;
  }

  /**
   * To retrieve a specific TemplateMapping
   *
   * @param actionType for the specific TemplateMapping to retrieve
   * @return the specific TemplateMapping
   * @throws CTPException if no TemplateMapping found
   */
  @RequestMapping(value = "/{actionType}", method = RequestMethod.GET)
  public TemplateMappingDTO findTemplateMapping(
      @PathVariable("actionType") final String actionType) throws CTPException {
    log.debug("Entering findTemplateMapping with {}", actionType);
    TemplateMapping result = templateMappingService.retrieveTemplateMappingByActionType(actionType);
    if (result == null) {
      throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, "Template Mapping not found for action type %s",
          actionType);
    }
    return mapperFacade.map(result, TemplateMappingDTO.class);
  }

  /**
   * To store TemplateMappings
   *
   * @param templateMappingDTOList the TemplateMapping content
   * @return 201 if created
   * @throws CTPException if the TemplateMapping can't be stored
   */
  @RequestMapping(value = "/{actionType}", method = RequestMethod.POST, consumes = "application/json")
  public ResponseEntity<List<TemplateMappingDTO>> storeTemplateMappings(@PathVariable("actionType") final String actionType, @RequestBody final List<TemplateMappingDTO> templateMappingDTOList)
      throws CTPException {
    log.debug("Entering storeTemplateMapping");

    List<TemplateMapping> mappingtemp = mapperFacade.mapAsList(templateMappingDTOList, TemplateMapping.class);

    List<TemplateMapping> mappings = templateMappingService.storeTemplateMappings(actionType, mappingtemp);

    return ResponseEntity.created(URI.create("TODO")).body(mapperFacade.mapAsList(mappings, TemplateMappingDTO.class));
  }

}