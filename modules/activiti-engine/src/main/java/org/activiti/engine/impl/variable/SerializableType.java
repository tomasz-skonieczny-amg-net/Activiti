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
package org.activiti.engine.impl.variable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.beans.BeanUtils;

/**
 * @author Tom Baeyens
 * @author Marcus Klimstra (CGI)
 */
public class SerializableType extends ByteArrayType {

  public static final String TYPE_NAME = "serializable";
  
  private static final long serialVersionUID = 1L;
  
  private static final boolean useProxying = true;
  
  public String getTypeName() {
    return TYPE_NAME;
  }

  public Object getValue(ValueFields valueFields) {
    Object cachedObject = valueFields.getCachedValue();
    if (cachedObject != null) {
      return cachedObject;
    }
    
    byte[] bytes = (byte[]) super.getValue(valueFields);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    try {
      ObjectInputStream ois = createObjectInputStream(bais);
      final Object origDeserializedObject = ois.readObject();
      Object deserializedObject = origDeserializedObject;
      valueFields.setCachedValue(deserializedObject);
      
      if (valueFields instanceof VariableInstanceEntity) {
          
    	  if(useProxying) {
	          if(! origDeserializedObject.getClass().isPrimitive() && ! String.class.equals(origDeserializedObject.getClass())) {
	        	  System.out.println("Creating proxy for: " + origDeserializedObject);
	        	  //Object proxy = createProxy(deserializedObject);
	        	  Object proxy = createProxy(origDeserializedObject.getClass(), new RecurProxyMethodInterceptor(origDeserializedObject));
	        	  
	        	  deserializedObject = proxy;
	          }
    	  }
          
          // we need to register the deserialized object for dirty checking, 
          // so that it can be serialized again if it was changed. 
          Context.getCommandContext()
            .getDbSqlSession()
            .addDeserializedObject(deserializedObject, bytes, (VariableInstanceEntity) valueFields);
      }
      
      return deserializedObject;
    } catch (Exception e) {
      throw new ActivitiException("Couldn't deserialize object in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(bais);
    }
  }

  public void setValue(Object value, ValueFields valueFields) {
    byte[] byteArray = serialize(value, valueFields);
    valueFields.setCachedValue(value);
    
    if (valueFields.getBytes() == null) {
      // TODO why the null check? won't this cause issues when setValue is called the second this with a different object?
      if (valueFields instanceof VariableInstanceEntity) {
        // register the deserialized object for dirty checking.
        Context.getCommandContext()
          .getDbSqlSession()
          .addDeserializedObject(valueFields.getCachedValue(), byteArray, (VariableInstanceEntity)valueFields);
      }
    }
        
    super.setValue(byteArray, valueFields);
  }

  public static byte[] serialize(Object value, ValueFields valueFields) {
    if (value == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = null;
    Object serValue = value;
    try {
      oos = createObjectOutputStream(baos);
      
      if(Enhancer.isEnhanced(value.getClass())) {
    	  try {
    		  Object obj = ReflectUtil.invoke(value, "getUnderlyingSource", new Object[]{});
    		  serValue = obj;
    	  } catch(Exception e) {
    		  e.printStackTrace();
    	  }
    	  /*InvocationHandler interceptor = Proxy.getInvocationHandler(value);
    	  if(interceptor instanceof SetterInvocationHandler) {
    		  serValue = ((SetterInvocationHandler) interceptor).getOriginalObject();
    	  }*/
      }
    	  oos.writeObject(serValue);
    } catch (Exception e) {
      throw new ActivitiException("Couldn't serialize value '"+value+"' in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(oos);
    }
    return baos.toByteArray();
  }
  
/*  @SuppressWarnings("unchecked")
  public static <T> T createProxy(final T object) {
	  return (T) Proxy.newProxyInstance
	          (SerializableType.class.getClassLoader(),
	                new Class[] { object.getClass() }, new SetterInvocationHandler(object));
  }
  */
  
  @SuppressWarnings("unchecked")
  private static <T> T createProxy(final Class<? extends Object> classToMock,
          final MethodInterceptor interceptor) {
      final Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(classToMock);
      enhancer.setInterfaces(new Class[]{Serializable.class, amg.aop.IProxied.class});
      enhancer.setCallbackType(interceptor.getClass());

      final Class<?> proxyClass = enhancer.createClass();
      Enhancer.registerCallbacks(proxyClass, new Callback[] { interceptor });
      return (T) BeanUtils.instantiate(proxyClass);
  }
  
  private static class RecurProxyMethodInterceptor implements MethodInterceptor {
	  
	  Object origDeserializedObject;
	  
	  public RecurProxyMethodInterceptor(Object origDeserializedObject) {
		this.origDeserializedObject = origDeserializedObject;  
	  }
	  
	  @Override
	  public Object intercept(Object obj, Method method, Object[] args,
			  MethodProxy proxy) throws Throwable {
		  ////TODO zmienić sprawdzanie czy setter na inteligentniejsze
		  if("getUnderlyingSource".equals(method.getName())) {
			  return origDeserializedObject;
		  } else if("toString".equals(method.getName())) {
			  return "Proxied ( " + method.invoke(origDeserializedObject, args) + " )";
		  } else if(method.getName().startsWith("get") || method.getName().startsWith("is")) {
			  System.out.println("Wywołano getter na obiekcie: " + origDeserializedObject);
			  Object getterResult = method.invoke(origDeserializedObject, args);
			  if(getterResult != null && !Enhancer.isEnhanced(getterResult.getClass()) && ! getterResult.getClass().isPrimitive() && ! "String".equals(getterResult.getClass())) {
				  return createProxy(getterResult.getClass(), new RecurProxyMethodInterceptor(getterResult));
			  } else {
				  return getterResult;
			  }
		  } else if (method.getName().startsWith("set")) {
			  System.out.println("Wywołano setter na obiekcie: " + origDeserializedObject);
			  //wykonać walidację obiektu (lub tylko setowanego pola)
		  }
		  return method.invoke(origDeserializedObject, args);
	  }
  }	  
  
  /*public static class SetterInvocationHandler implements InvocationHandler {
	   
	  private Object originalObject;
	  
	  public SetterInvocationHandler(Object orig) {
		  originalObject = orig;
	  }
	  
      @Override
      public Object invoke(Object proxy, Method method, Object[] args)
              throws Throwable {
    	  
    	  ////TODO zmienić sprawdzanie czy setter na inteligentniejsze
          if (method.getName().startsWith("set")) {
              System.out.println("Wywołano setter na obiekcie: " + proxy);
              //wykonać walidację obiektu (lub tylko setowanego pola)
          }
          return method.invoke(originalObject, args);
      }

      public Object getOriginalObject() {
		return originalObject;
      }
  }*/
  
  

  public boolean isAbleToStore(Object value) {
    // TODO don't we need null support here?
    return value instanceof Serializable;
  }

  protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
    return new ObjectInputStream(is) {
      protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        return ReflectUtil.loadClass(desc.getName());
      }
    };
  }
  
  private static ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
    return new ObjectOutputStream(os);
  }
}
