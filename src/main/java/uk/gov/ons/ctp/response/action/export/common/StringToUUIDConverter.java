package uk.gov.ons.ctp.response.action.export.common;

import java.util.UUID;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import net.sourceforge.cobertura.CoverageIgnore;

@CoverageIgnore
public class StringToUUIDConverter extends BidirectionalConverter<String, UUID> {
  public StringToUUIDConverter() {}

  public UUID convertTo(String source, Type<UUID> destinationType, MappingContext mappingContext) {
    return UUID.fromString(source);
  }

  public String convertFrom(
      UUID source, Type<String> destinationType, MappingContext mappingContext) {
    return source.toString();
  }
}
