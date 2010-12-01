package org.activiti.examples.bpmn.servicetask;

import java.io.Serializable;

/**
 * @author Christian Stettler
 */
public class ValueBean implements Serializable {

  private final String value;

  public ValueBean(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}