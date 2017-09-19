package uk.gov.ons.ctp.response.action.export.service;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.ons.ctp.response.action.export.repository.TemplateMappingRepository;
import uk.gov.ons.ctp.response.action.export.service.impl.TemplateMappingServiceImpl;

/**
 * To unit test TemplateMappingServiceImpl
 */
//@RunWith(MockitoJUnitRunner.class)
public class TemplateMappingServiceImplTest {

  @InjectMocks
  private TemplateMappingServiceImpl templateMappingService;

  @Mock
  private TemplateMappingRepository repository;

  /**
   * Tests store with template mapping as null
   */
/*  @Test
  public void testStoreNullTemplateMapping() {
    boolean exceptionThrown = false;
    try {
      templateMappingService.storeTemplateMappings(null);
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(EXCEPTION_STORE_TEMPLATE_MAPPING, e.getMessage());
    }
    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateMapping.class));
  }*/

  /**
   * Tests store with template mapping empty
   */
/*  @Test
  public void testStoreEmptyTemplateMapping() {
    boolean exceptionThrown = false;
    try {
      templateMappingService.storeTemplateMappings(
          getClass().getResourceAsStream("/templates/freemarker/empty_template_mapping.json"));
    } catch (CTPException e) {
      exceptionThrown = true;
      assertEquals(CTPException.Fault.SYSTEM_ERROR, e.getFault());
      assertEquals(EXCEPTION_STORE_TEMPLATE_MAPPING, e.getMessage());
    }
    assertTrue(exceptionThrown);
    verify(repository, times(0)).save(any(TemplateMapping.class));
  }*/

  /**
   * Tests store with template mapping as valid
   */
/*  @Test
  public void testStoreValidTemplateMapping() throws CTPException {
    templateMappingService.storeTemplateMappings(
        getClass().getResourceAsStream("/templates/freemarker/valid_template_mapping.json"));
    verify(repository, times(1)).save(anyListOf(TemplateMapping.class));
  }*/
  /*
   * @Test public void testRetrieveMapFromNonExistingTemplateMappingDocument() {
   * Map<String, String> result =
   * templateMappingService.retrieveMapFromTemplateMappingDocument(
   * TEMPLATE_MAPPING_NAME); assertNotNull(result);
   * assertTrue(result.isEmpty()); verify(repository,
   * times(1)).findOne(TEMPLATE_MAPPING_NAME); }
   * 
   * 
   * @Test public void testRetrieveMapFromExistingTemplateMappingDocument() {
   * TemplateMappingDocument templateMappingDocument = new
   * TemplateMappingDocument();
   * templateMappingDocument.setName(TEMPLATE_MAPPING_NAME);
   * templateMappingDocument.setDateModified(new Date());
   * templateMappingDocument.setContent("{\n" + "  \"ICL1\":\"curltest1\",\n" +
   * "  \"ICL2\":\"curltest2\",\n" + "  \"ICL2W\":\"curltest3\"}");
   * when(repository.findOne(TEMPLATE_MAPPING_NAME)).thenReturn(
   * templateMappingDocument);
   * 
   * Map<String, String> result =
   * templateMappingService.retrieveMapFromTemplateMappingDocument(
   * TEMPLATE_MAPPING_NAME); assertNotNull(result); assertEquals(3,
   * result.size()); assertEquals("curltest1", result.get("ICL1"));
   * assertEquals("curltest2", result.get("ICL2")); assertEquals("curltest3",
   * result.get("ICL2W")); verify(repository,
   * times(1)).findOne(TEMPLATE_MAPPING_NAME); }
   */
}