package uk.gov.ons.ctp.response.action.export.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.action.export.domain.TemplateMapping;
import uk.gov.ons.ctp.response.action.export.repository.TemplateMappingRepository;

/** The implementation of the TemplateMappingService */
@Service
public class TemplateMappingService {
  private static final Logger log = LoggerFactory.getLogger(TemplateMappingService.class);
  private final TemplateMappingRepository templateMappingRepository;

  @Autowired
  public TemplateMappingService(TemplateMappingRepository templateMappingRepository) {
    this.templateMappingRepository = templateMappingRepository;
  }

  public List<TemplateMapping> storeTemplateMappings(
      String actionType, List<TemplateMapping> templateMappingList) {

    for (TemplateMapping templateMapping : templateMappingList) {
      templateMapping.setActionType(actionType);
      templateMapping.setDateModified(new Date());
      templateMappingRepository.save(templateMapping);
    }

    return templateMappingList;
  }

  private List<TemplateMapping> retrieveAllTemplateMappings() {
    return templateMappingRepository.findAll();
  }

  public Map<String, List<TemplateMapping>> retrieveAllTemplateMappingsByFilename() {
    return retrieveAllTemplateMappings()
        .stream()
        .collect(Collectors.groupingBy(TemplateMapping::getFileNamePrefix));
  }
}
