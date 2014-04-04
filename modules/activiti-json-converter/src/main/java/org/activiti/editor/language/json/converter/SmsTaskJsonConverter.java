/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tomasz Skonieczny
 */
public class SmsTaskJsonConverter extends MailTaskJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_SMS, SmsTaskJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    // will be handled by ServiceTaskJsonConverter
  }
  
  protected String getStencilId(FlowElement flowElement) {
    return STENCIL_TASK_SMS;
  }
  
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
  	ServiceTask task = new ServiceTask();
  	task.setType(ServiceTask.SMS_TASK);
  	addField(PROPERTY_SMSTASK_TO, elementNode, task);
  	addField(PROPERTY_SMSTASK_FROM, elementNode, task);
  	addField(PROPERTY_SMSTASK_TEXT, elementNode, task);
  	addField(PROPERTY_SMSTASK_CHARSET, elementNode, task);
    
    return task;
  }
  
  protected void addField(String name, JsonNode elementNode, ServiceTask task) {
	    FieldExtension field = new FieldExtension();
	    field.setFieldName(name.substring(7));
	    String value = getPropertyValueAsString(name, elementNode);
	    if (StringUtils.isNotEmpty(value)) {
	      if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
	        field.setExpression(value);
	      } else {
	        field.setStringValue(value);
	      }
	      task.getFieldExtensions().add(field);
	    }
	  }
}
