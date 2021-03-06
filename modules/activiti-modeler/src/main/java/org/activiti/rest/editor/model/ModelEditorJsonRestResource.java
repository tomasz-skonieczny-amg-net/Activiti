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
package org.activiti.rest.editor.model;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amg.bpm.webflow.WebFlowDataManagerInterface;
import amg.bpm.webflow.model.DataModel;


/**
 * @author Tijs Rademakers
 */
public class ModelEditorJsonRestResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  
  @Get
  public ObjectNode getEditorJson() {
    ObjectNode modelNode = null;
    String modelId = (String) getRequest().getAttributes().get("modelId");
    
//    RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
//    Model model = repositoryService.getAMGModel(modelId);
    Context ctx = null;
    DataModel model = null;
    try {
        ctx = new InitialContext();
        WebFlowDataManagerInterface webFlowDataManager = (WebFlowDataManagerInterface) ctx.lookup("dynamo:/amg/bpm/flow/manager/WebFlowDataManager");
        model = webFlowDataManager.loadWebFlowData(modelId);
    } catch (NamingException e1) {
        e1.printStackTrace();
    }
      

    if (model != null) {
      try {
        if (StringUtils.isNotEmpty(model.getMetaInfo())) {
          modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
        } else {
          modelNode = objectMapper.createObjectNode();
          modelNode.put(MODEL_NAME, model.getName());
        }
        modelNode.put(MODEL_ID, model.getId());
        ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(new String(model.getBytes()));
        modelNode.put("model", editorJsonNode);
        
      } catch(Exception e) {
        LOGGER.error("Error creating model JSON", e);
        setStatus(Status.SERVER_ERROR_INTERNAL);
      }
    }
    return modelNode;
  }
}
