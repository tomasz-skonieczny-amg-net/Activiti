package org.activiti.engine.impl.form;

import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.AbstractFormType;

public class ListFormType extends AbstractFormType {
    

    protected Map<String, String> values;

    public ListFormType(Map<String, String> values) {
      this.values = values;
    }

    public String getName() {
      return "list";
    }
    
    @Override
    public Object getInformation(String key) {
      if (key.equals("values")) {
        return values;
      }
      return null;
    }

    @Override
    public Object convertFormValueToModelValue(String propertyValue) {
      validateValue(propertyValue);
      return propertyValue;
    }

    @Override
    public String convertModelValueToFormValue(Object modelValue) {
      if(modelValue != null) {
        if(!(modelValue instanceof String)) {
          throw new ActivitiIllegalArgumentException("Model value should be a String");
        }
        validateValue((String) modelValue);
      }
      return (String) modelValue;
    }
    
    protected void validateValue(String value) {
      if(value != null) {
        if(values != null && !values.containsKey(value)) {
          throw new ActivitiIllegalArgumentException("Invalid value for enum form property: " + value);
        }
      }
    }

  public Map<String, String> getValues() {
      return values;
  }


}
