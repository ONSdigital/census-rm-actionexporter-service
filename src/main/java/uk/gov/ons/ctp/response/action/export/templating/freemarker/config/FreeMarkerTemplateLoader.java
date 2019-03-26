package uk.gov.ons.ctp.response.action.export.templating.freemarker.config;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import freemarker.cache.TemplateLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.action.export.domain.TemplateExpression;
import uk.gov.ons.ctp.response.action.export.repository.TemplateRepository;

/** TemplateLoader to load templates stored in MongopDB */
@Component
public class FreeMarkerTemplateLoader implements TemplateLoader {
  private static final Logger log = LoggerFactory.getLogger(FreeMarkerTemplateLoader.class);

  @Autowired private TemplateRepository templateRepository;

  @Override
  public Object findTemplateSource(String name) throws IOException {
    log.with("template_name", name).debug("Retrieving template");

    if (name.equals("icl1e")) {
      String content = getContentForTemplate(name);
      return new TemplateExpression("icl1e", content, new Date());
    }

    return null;
  }

  private String getContentForTemplate(String filePrefix) {
    String filepath = "./templates/" + filePrefix + ".ftl";

    try {
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      InputStream is = classloader.getResourceAsStream(filepath);

      return convertInputSteamToString(is);
    } catch (Exception e) {
      return null;
    }
  }

  private String convertInputSteamToString(InputStream inputStream) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  @Override
  public long getLastModified(Object templateSource) {
    TemplateExpression template = (TemplateExpression) templateSource;
    String name = template.getName();
    log.with("template_name", name).debug("Retrieving last modified time for template");
    template = templateRepository.findOne(name);
    return template.getDateModified().getTime();
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    // TODO encoding will be UTF-8 - do we need to do anything with it?
    return new StringReader(((TemplateExpression) templateSource).getContent());
  }

  /** Used to close Template Source */
  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {}
}
