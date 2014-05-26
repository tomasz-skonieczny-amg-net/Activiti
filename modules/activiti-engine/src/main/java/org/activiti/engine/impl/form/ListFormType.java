package org.activiti.engine.impl.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.AbstractFormType;

public class ListFormType extends AbstractFormType {
    

    protected Map<String, String> values;
    
    public ListFormType() {
    	values = new HashMap<String, String>();
    }
    
    public ListFormType(Map<String, String> values, List<String> validators) {
      this.values = values;
      this.validators.addAll(validators);
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
        if(values != null && !values.containsKey(value) && !values.containsValue(value)) {
          throw new ActivitiIllegalArgumentException("Invalid value for list form property: " + value);
        }
      }
    }

  public Map<String, String> getValues() {
      return values;
  }
//
//  public List<String> getValidators() {
//    return validators;
//  }

}
