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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomasz Skonieczny
 */
public class SmsActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected static final Logger LOGGER = LoggerFactory.getLogger(BpmnXMLConverter.class);
  
  protected Expression to;
  protected Expression from;
  protected Expression text;
  protected Expression charset;

  public void execute(ActivityExecution execution) {
    String toStr = getStringFromField(to, execution);
    String fromStr = getStringFromField(from, execution);
    String textStr = getStringFromField(text, execution);
    String charSetStr = getStringFromField(charset, execution);

    
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("Sending SMS to: %s, from: %s, text: %s, charset: %s", toStr, fromStr, textStr, charSetStr));
    }
    callSmsRestService(Context.getProcessEngineConfiguration().getSmsServiceUrl(), toStr, fromStr, textStr);
    	
    leave(execution);
  }
  
  protected boolean callSmsRestService(String endpoint, String to, String from, String text) {
	  try {
		  
		URL url = new URL(endpoint);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
 
		String input = String.format("{\"to\":\"%s\",\"from\":\"%s\",\"text\":\"%s\"}", to, from, text);
 
		OutputStream os = conn.getOutputStream();
		os.write(input.getBytes());
		os.flush();
 
		if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new ActivitiException("SMS rest service failed! HTTP error code : "
				+ conn.getResponseCode());
		}
 
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
 
		String output;
		if (LOGGER.isDebugEnabled()) {
		      LOGGER.debug("Output from Server:");
		}
		while ((output = br.readLine()) != null) {
			if (LOGGER.isDebugEnabled()) {
			      LOGGER.debug("\t" + output);
			}
		}
 
		conn.disconnect();
		
		return true;
	  } catch (MalformedURLException e) {
		e.printStackTrace();
	  } catch (IOException e) {
		e.printStackTrace();
	 }
	  
	 return false;
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
