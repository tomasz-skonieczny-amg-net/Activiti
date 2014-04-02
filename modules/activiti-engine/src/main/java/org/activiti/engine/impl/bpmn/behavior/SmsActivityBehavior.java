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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Tomasz Skonieczny
 */
public class SmsActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;
  
  protected Expression to;
  protected Expression from;
  protected Expression text;
  protected Expression charset;

  public void execute(ActivityExecution execution) {
    String toStr = getStringFromField(to, execution);
    String fromStr = getStringFromField(from, execution);
    String textStr = getStringFromField(text, execution);
    String charSetStr = getStringFromField(charset, execution);

    System.out.println(String.format("Sending SMS to: %s, from: %s, text: %s, charset: %s", toStr, fromStr, textStr, charSetStr));
    	
    leave(execution);
  }
 
  protected String[] splitAndTrim(String str) {
    if (str != null) {
      String[] splittedStrings = str.split(",");
      for (int i = 0; i < splittedStrings.length; i++) {
        splittedStrings[i] = splittedStrings[i].trim();
      }
      return splittedStrings;
    }
    return null;
  }

  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if(expression != null) {
      Object value = expression.getValue(execution);
      if(value != null) {
        return value.toString();
      }
    }
    return null;
  }

}
