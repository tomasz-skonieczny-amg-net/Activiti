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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amg.bpm.webflow.WebFlowDataManagerInterface;
import amg.bpm.webflow.model.DataModel;


/**
 * @author Tijs Rademakers
 */
public class ModelSaveRestResource extends ServerResource implements ModelDataJsonConstants {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);

  @Put
  public void saveModel(Form modelForm) {
    ObjectMapper objectMapper = new ObjectMapper();
    String modelId = (String) getRequest().getAttributes().get("modelId");
    //System.out.println("json " + modelForm.getFirstValue("json_xml"));
    
    //WebFlowDataManager webFlowDataManager = (WebFlowDataManager) Nucleus.getGlobalNucleus().resolveName("/amg/bpm/flow/manager/WebFlowDataManager");
    
    
    try {

//        RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
//        Model model = repositoryService.getAMGModel(modelId);
        
      
        
        //webFlowDataManager.saveWebFlowDiagram(modelId, modelForm.getFirstValue("name"), modelForm.getFirstValue("json_xml").getBytes("utf-8"), modelJson.toString());
        
          Context ctx = null;
          try {
              ctx = new InitialContext();
              
              WebFlowDataManagerInterface webFlowDataManager = (WebFlowDataManagerInterface) ctx.lookup("dynamo:/amg/bpm/flow/manager/WebFlowDataManager");
              DataModel model = webFlowDataManager.loadWebFlowData(modelId);
              
              ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
              
              modelJson.put(MODEL_NAME, modelForm.getFirstValue("name"));
              modelJson.put(MODEL_DESCRIPTION, modelForm.getFirstValue("description"));
              model.setMetaInfo(modelJson.toString());
              model.setName(modelForm.getFirstValue("name"));
              model.setBytes(modelForm.getFirstValue("json_xml").getBytes("utf-8"));
              
              webFlowDataManager.saveWebFlowDiagram(modelId, modelForm.getFirstValue("name"), modelForm.getFirstValue("json_xml").getBytes("utf-8"), modelJson.toString());
          } catch (NamingException e) {
              e.printStackTrace();
          }
      
      
    } catch(Exception e) {
      LOGGER.error("Error saving model", e);
      setStatus(Status.SERVER_ERROR_INTERNAL);
    }
    
    
//    try {
//      
//      /*ObjectNode modelNode = (ObjectNode) objectMapper.readTree(modelForm.getFirstValue("json_xml"));
//      JsonToBpmnExport converter = new JsonToBpmnExport(modelNode);
//      byte[] bpmnBytes = converter.convert();
//      System.out.println("bpmn " + new String(bpmnBytes));*/
//      
//      RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();
//      Model model = repositoryService.getAMGModel(modelId);
//      
//      ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
//      
//      modelJson.put(MODEL_NAME, modelForm.getFirstValue("name"));
//      modelJson.put(MODEL_DESCRIPTION, modelForm.getFirstValue("description"));
//      model.setMetaInfo(modelJson.toString());
//      model.setName(modelForm.getFirstValue("name"));
//      model.setBytes(modelForm.getFirstValue("json_xml").getBytes("utf-8"));
//      
//      
//      repositoryService.saveAMGModel(model);
//      
//      
//    //  String xml = "<?xml version='1.0' encoding='UTF-8'?>\n<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:activiti=\"http://activiti.org/bpmn\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" typeLanguage=\"http://www.w3.org/2001/XMLSchema\" expressionLanguage=\"http://www.w3.org/1999/XPath\" targetNamespace=\"http://www.activiti.org/processdef\">\n  <process id=\"process\" isExecutable=\"true\">\n    <startEvent id=\"sid-2BB933AE-E0AE-48D2-9ACC-B5EC35AD3687\" />\n    <userTask id=\"sid-AFE2BB40-CF6F-4947-9DF9-2F1F80E34C43\" name=\"user task 1\" activiti:assignee=\"kermit\">\n      <extensionElements>\n        <activiti:formProperty id=\"number\" name=\"Number\" type=\"long\" />\n        <activiti:formProperty id=\"message\" name=\"Message\" type=\"string\" />\n      </extensionElements>\n    </userTask>\n    <sequenceFlow id=\"sid-974EF4BD-0E76-4983-B84D-FA92053B98DC\" sourceRef=\"sid-2BB933AE-E0AE-48D2-9ACC-B5EC35AD3687\" targetRef=\"sid-AFE2BB40-CF6F-4947-9DF9-2F1F80E34C43\" />\n    <exclusiveGateway id=\"sid-B074A0DD-934A-4053-A537-20ADF0781023\" />\n    <sequenceFlow id=\"sid-AFFB5C18-4C31-469B-919B-A08BE34542EA\" sourceRef=\"sid-AFE2BB40-CF6F-4947-9DF9-2F1F80E34C43\" targetRef=\"sid-B074A0DD-934A-4053-A537-20ADF0781023\" />\n    <userTask id=\"sid-03BC7128-4496-4027-88A9-E67D3DA63734\" name=\"User task 2\" activiti:assignee=\"kermit\" />\n    <userTask id=\"sid-7581049C-894E-4FF9-B861-7DF44B7229E3\" name=\"User task 3\" activiti:assignee=\"kermit\" />\n    <exclusiveGateway id=\"sid-6151821D-C3F9-4DFB-82EE-43885200535F\" />\n    <sequenceFlow id=\"sid-CBE1C51A-408E-4383-9D42-713450DD89BE\" sourceRef=\"sid-7581049C-894E-4FF9-B861-7DF44B7229E3\" targetRef=\"sid-6151821D-C3F9-4DFB-82EE-43885200535F\" />\n    <sequenceFlow id=\"sid-5DC9E5BB-634D-43BE-BE09-2A4D1A77AB3B\" sourceRef=\"sid-03BC7128-4496-4027-88A9-E67D3DA63734\" targetRef=\"sid-6151821D-C3F9-4DFB-82EE-43885200535F\" />\n    <intermediateCatchEvent id=\"sid-C102D215-8257-40B4-AEE6-99B223204F7B\">\n      <timerEventDefinition>\n        <timeDuration>PT5M</timeDuration>\n      </timerEventDefinition>\n    </intermediateCatchEvent>\n    <sequenceFlow id=\"sid-7A6FDAE1-C837-4148-AE9E-E36F9BD55C27\" sourceRef=\"sid-6151821D-C3F9-4DFB-82EE-43885200535F\" targetRef=\"sid-C102D215-8257-40B4-AEE6-99B223204F7B\" />\n    <endEvent id=\"sid-65043A85-6BAD-4616-AD1E-FF3FA8D64D4B\" />\n    <sequenceFlow id=\"sid-104B63DD-B61E-4D47-B65F-95A1B77AB041\" sourceRef=\"sid-C102D215-8257-40B4-AEE6-99B223204F7B\" targetRef=\"sid-65043A85-6BAD-4616-AD1E-FF3FA8D64D4B\" />\n    <sequenceFlow id=\"sid-07A7E174-8857-4DE9-A7CD-A041706D79C3\" sourceRef=\"sid-B074A0DD-934A-4053-A537-20ADF0781023\" targetRef=\"sid-03BC7128-4496-4027-88A9-E67D3DA63734\">\n      <conditionExpression xsi:type=\"tFormalExpression\"><![CDATA[${number > 1}]]></conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"sid-C2068B1E-9A82-41C9-B876-C58E2736C186\" sourceRef=\"sid-B074A0DD-934A-4053-A537-20ADF0781023\" targetRef=\"sid-7581049C-894E-4FF9-B861-7DF44B7229E3\">\n      <conditionExpression xsi:type=\"tFormalExpression\"><![CDATA[${number <= 1}]]></conditionExpression>\n    </sequenceFlow>\n  </process>\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_process\">\n    <bpmndi:BPMNPlane bpmnElement=\"process\" id=\"BPMNPlane_process\">\n      <bpmndi:BPMNShape bpmnElement=\"sid-2BB933AE-E0AE-48D2-9ACC-B5EC35AD3687\" id=\"BPMNShape_sid-2BB933AE-E0AE-48D2-9ACC-B5EC35AD3687\">\n        <omgdc:Bounds height=\"30.0\" width=\"30.0\" x=\"139.5\" y=\"130.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-AFE2BB40-CF6F-4947-9DF9-2F1F80E34C43\" id=\"BPMNShape_sid-AFE2BB40-CF6F-4947-9DF9-2F1F80E34C43\">\n        <omgdc:Bounds height=\"80.0\" width=\"100.0\" x=\"225.0\" y=\"105.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-B074A0DD-934A-4053-A537-20ADF0781023\" id=\"BPMNShape_sid-B074A0DD-934A-4053-A537-20ADF0781023\">\n        <omgdc:Bounds height=\"40.0\" width=\"40.0\" x=\"390.0\" y=\"125.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-03BC7128-4496-4027-88A9-E67D3DA63734\" id=\"BPMNShape_sid-03BC7128-4496-4027-88A9-E67D3DA63734\">\n        <omgdc:Bounds height=\"80.0\" width=\"100.0\" x=\"475.0\" y=\"30.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-7581049C-894E-4FF9-B861-7DF44B7229E3\" id=\"BPMNShape_sid-7581049C-894E-4FF9-B861-7DF44B7229E3\">\n        <omgdc:Bounds height=\"80.0\" width=\"100.0\" x=\"475.0\" y=\"180.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-6151821D-C3F9-4DFB-82EE-43885200535F\" id=\"BPMNShape_sid-6151821D-C3F9-4DFB-82EE-43885200535F\">\n        <omgdc:Bounds height=\"40.0\" width=\"40.0\" x=\"630.0\" y=\"125.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-C102D215-8257-40B4-AEE6-99B223204F7B\" id=\"BPMNShape_sid-C102D215-8257-40B4-AEE6-99B223204F7B\">\n        <omgdc:Bounds height=\"30.0\" width=\"30.0\" x=\"735.0\" y=\"130.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape bpmnElement=\"sid-65043A85-6BAD-4616-AD1E-FF3FA8D64D4B\" id=\"BPMNShape_sid-65043A85-6BAD-4616-AD1E-FF3FA8D64D4B\">\n        <omgdc:Bounds height=\"28.0\" width=\"28.0\" x=\"810.0\" y=\"131.0\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-5DC9E5BB-634D-43BE-BE09-2A4D1A77AB3B\" id=\"BPMNEdge_sid-5DC9E5BB-634D-43BE-BE09-2A4D1A77AB3B\">\n        <omgdi:waypoint x=\"575.0\" y=\"70.0\" />\n        <omgdi:waypoint x=\"650.0\" y=\"70.0\" />\n        <omgdi:waypoint x=\"650.0\" y=\"125.0\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-07A7E174-8857-4DE9-A7CD-A041706D79C3\" id=\"BPMNEdge_sid-07A7E174-8857-4DE9-A7CD-A041706D79C3\">\n        <omgdi:waypoint x=\"410.5\" y=\"125.5\" />\n        <omgdi:waypoint x=\"410.5\" y=\"70.0\" />\n        <omgdi:waypoint x=\"475.0\" y=\"70.0\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-C2068B1E-9A82-41C9-B876-C58E2736C186\" id=\"BPMNEdge_sid-C2068B1E-9A82-41C9-B876-C58E2736C186\">\n        <omgdi:waypoint x=\"410.5\" y=\"164.5\" />\n        <omgdi:waypoint x=\"410.5\" y=\"220.0\" />\n        <omgdi:waypoint x=\"475.0\" y=\"220.0\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-974EF4BD-0E76-4983-B84D-FA92053B98DC\" id=\"BPMNEdge_sid-974EF4BD-0E76-4983-B84D-FA92053B98DC\">\n        <omgdi:waypoint x=\"169.5\" y=\"145.0\" />\n        <omgdi:waypoint x=\"225.0\" y=\"145.0\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-CBE1C51A-408E-4383-9D42-713450DD89BE\" id=\"BPMNEdge_sid-CBE1C51A-408E-4383-9D42-713450DD89BE\">\n        <omgdi:waypoint x=\"575.0\" y=\"220.0\" />\n        <omgdi:waypoint x=\"650.5\" y=\"220.0\" />\n        <omgdi:waypoint x=\"650.5\" y=\"164.5\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-AFFB5C18-4C31-469B-919B-A08BE34542EA\" id=\"BPMNEdge_sid-AFFB5C18-4C31-469B-919B-A08BE34542EA\">\n        <omgdi:waypoint x=\"325.0\" y=\"145.18450184501845\" />\n        <omgdi:waypoint x=\"390.4259259259259\" y=\"145.42592592592592\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-104B63DD-B61E-4D47-B65F-95A1B77AB041\" id=\"BPMNEdge_sid-104B63DD-B61E-4D47-B65F-95A1B77AB041\">\n        <omgdi:waypoint x=\"765.0\" y=\"145.0\" />\n        <omgdi:waypoint x=\"810.0\" y=\"145.0\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge bpmnElement=\"sid-7A6FDAE1-C837-4148-AE9E-E36F9BD55C27\" id=\"BPMNEdge_sid-7A6FDAE1-C837-4148-AE9E-E36F9BD55C27\">\n        <omgdi:waypoint x=\"669.5959595959596\" y=\"145.40404040404042\" />\n        <omgdi:waypoint x=\"735.0001893855701\" y=\"145.07537593273582\" />\n      </bpmndi:BPMNEdge>\n    </bpmndi:BPMNPlane>\n  </bpmndi:BPMNDiagram>\n</definitions>";
//      
//      
//      InputStream svgStream = new ByteArrayInputStream(modelForm.getFirstValue("svg_xml").getBytes("utf-8"));
//      TranscoderInput input = new TranscoderInput(svgStream);
//      
//      PNGTranscoder transcoder = new PNGTranscoder();
//      // Setup output
//      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//      TranscoderOutput output = new TranscoderOutput(outStream);
//      
//      // Do the transformation
//      transcoder.transcode(input, output);
//      final byte[] result = outStream.toByteArray();
//      repositoryService.addModelEditorSourceExtra(model.getId(), result);
//      outStream.close();
//      
//    } catch(Exception e) {
//      LOGGER.error("Error saving model", e);
//      setStatus(Status.SERVER_ERROR_INTERNAL);
//    }
      
  }
}
