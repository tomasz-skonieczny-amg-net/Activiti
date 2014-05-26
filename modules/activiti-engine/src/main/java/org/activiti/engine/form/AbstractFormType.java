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

package org.activiti.engine.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;



/**
 * Custom form types should extend this abstract class.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class AbstractFormType implements FormType {

  protected List<String> validators = new ArrayList<String>(); 
	
  public abstract Object convertFormValueToModelValue(String propertyValue);

  public abstract String convertModelValueToFormValue(Object modelValue);

  public Object getInformation(String key) {
    return null;
  }
  
  public List<String> getValidators() {
      return validators;
  }
  
  public Map<String, String> getValues() {
      return Collections.EMPTY_MAP;
  }

}
