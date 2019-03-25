package uk.gov.ons.ctp.response.action.representation;

public enum ActionType {
  ICL1E("ICL1E"),

  private String name;

  ActionType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
