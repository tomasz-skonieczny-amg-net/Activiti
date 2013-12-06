package org.activiti.engine;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;

/** 
* A utility class that allows one to bind a non-serializable object into a
* local JNDI context. The binding will only be valid for the lifetime of the
* VM in which the JNDI InitialContext lives. An example usage code snippet is:
*
<pre>
   // The non-Serializable object to bind
   Object nonserializable = ...;
   // An arbitrary key to use in the StringRefAddr. The best key is the jndi
   // name that the object will be bound under.
   String key = ...;
   // This places nonserializable into the NonSerializableFactory hashmap under key
   NonSerializableFactory.rebind(key, nonserializable);

   Context ctx = new InitialContext();
   // Bind a reference to nonserializable using NonSerializableFactory as the ObjectFactory
   String className = nonserializable.getClass().getName();
   String factory = NonSerializableFactory.class.getName();
   StringRefAddr addr = new StringRefAddr("nns", key);
   Reference memoryRef = new Reference(className, addr, factory, null);
   ctx.rebind(key, memoryRef);
</pre>
*
* Or you can use the {@link #rebind(Context, String, Object)} convenience
* method to simplify the number of steps to:
*
<pre>
   Context ctx = new InitialContext();
   // The non-Serializable object to bind
   Object nonserializable = ...;
   // The jndiName that the object will be bound into ctx with
   String jndiName = ...;
   // This places nonserializable into the NonSerializableFactory hashmap under key
   NonSerializableFactory.rebind(ctx, jndiName, nonserializable);
</pre>
* 
* To unbind the object, use the following code snippet:
* 
<pre>
 new InitialContext().unbind(key);
 NonSerializableFactory.unbind(key);
</pre>
*
* @see javax.naming.spi.ObjectFactory
* @see #rebind(Context, String, Object)
*
* @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
* @version $Revision: 2975 $
*/
@SuppressWarnings("unchecked")
public class NonSerializableFactory implements ObjectFactory
{
   private static Map wrapperMap = Collections.synchronizedMap(new HashMap());

   /** Place an object into the NonSerializableFactory namespace for subsequent
   access by getObject. There cannot be an already existing binding for key.

   @param key the name to bind target under. This should typically be the
   name that will be used to bind target in the JNDI namespace, but it does
   not have to be.
   @param target the non-Serializable object to bind.
   @throws NameAlreadyBoundException thrown if key already exists in the
    NonSerializableFactory map
   */
   public static synchronized void bind(String key, Object target) throws NameAlreadyBoundException
   {
       if( wrapperMap.containsKey(key) == true )
           throw new NameAlreadyBoundException(key+" already exists in the NonSerializableFactory map");
       wrapperMap.put(key, target);
   }
   /** Place or replace an object in the NonSerializableFactory namespce
    for subsequent access by getObject. Any existing binding for key will be
    replaced by target.

   @param key the name to bind target under. This should typically be the
   name that will be used to bind target in the JNDI namespace, but it does
   not have to be.
   @param target the non-Serializable object to bind.
   */
   public static void rebind(String key, Object target)
   {
       wrapperMap.put(key, target);
   }

   /** Remove a binding from the NonSerializableFactory map.

   @param key the key into the NonSerializableFactory map to remove.
   @throws NameNotFoundException thrown if key does not exist in the
    NonSerializableFactory map
   */
   public static void unbind(String key) throws NameNotFoundException
   {
       if( wrapperMap.remove(key) == null )
           throw new NameNotFoundException(key+" was not found in the NonSerializableFactory map");
   }
   /** Remove a binding from the NonSerializableFactory map.

   @param name the name for the key into NonSerializableFactory map to remove.
    The key is obtained as name.toString().
   @throws NameNotFoundException thrown if key does not exist in the
    NonSerializableFactory map
   */
   public static void unbind(Name name) throws NameNotFoundException
   {
       String key = name.toString();
       if( wrapperMap.remove(key) == null )
           throw new NameNotFoundException(key+" was not found in the NonSerializableFactory map");
   }

   /** Lookup a value from the NonSerializableFactory map.
   * @param key 
   @return the object bound to key is one exists, null otherwise.
   */
   public static Object lookup(String key)
   {
       Object value = wrapperMap.get(key);
       return value;
   }
   /** Lookup a value from the NonSerializableFactory map.
   * @param name 
   @return the object bound to key is one exists, null otherwise.
   */
   public static Object lookup(Name name)
   {
       String key = name.toString();
       Object value = wrapperMap.get(key);
       return value;
   }

   /** A convenience method that simplifies the process of rebinding a
    non-serializable object into a JNDI context.

   @param ctx the JNDI context to rebind to.
   @param key the key to use in both the NonSerializableFactory map and JNDI. It
       must be a valid name for use in ctx.bind().
   @param target the non-Serializable object to bind.
   @throws NamingException thrown on failure to rebind key into ctx.
   */
   public static synchronized void rebind(Context ctx, String key, Object target) throws NamingException
   {
       NonSerializableFactory.rebind(key, target);
       // Bind a reference to target using NonSerializableFactory as the ObjectFactory
       String className = target.getClass().getName();
       String factory = NonSerializableFactory.class.getName();
       StringRefAddr addr = new StringRefAddr("nns", key);
       Reference memoryRef = new Reference(className, addr, factory, null);
       ctx.rebind(key, memoryRef);
   }

  /** A convenience method that simplifies the process of rebinding a
   non-serializable object into a JNDI context. This version binds the
   target object into the default IntitialContext using name path.

  @param name the name to use as JNDI path name. The key into the
   NonSerializableFactory map is obtained from the toString() value of name.
   The name parameter cannot be a 0 length name.
   Any subcontexts between the root and the name must exist.
  @param target the non-Serializable object to bind.
  @throws NamingException thrown on failure to rebind key into ctx.
  */
  public static synchronized void rebind(Name name, Object target) throws NamingException
  {
     rebind(name, target, false);
  }

  /** A convenience method that simplifies the process of rebinding a
   non-serializable object into a JNDI context.

  @param ctx the JNDI context to rebind to.
  @param key the key to use in both the NonSerializableFactory map and JNDI. It
       must be a valid name for use in ctx.bind().
  @param target the non-Serializable object to bind.
  @param createSubcontexts a flag indicating if subcontexts of name should
   be created if they do not already exist.
  @throws NamingException thrown on failure to rebind key into ctx.
  */
  public static synchronized void rebind(Context ctx, String key, 
     Object target, boolean createSubcontexts) throws NamingException
  {
    Name name = ctx.getNameParser("").parse(key);
      if( createSubcontexts == true && name.size() > 1 )
      {
         int size = name.size() - 1;
         Util.createSubcontext(ctx, name.getPrefix(size));
      }
      rebind(ctx, key, target);
  }
  
  /** 
   * A convenience method that simplifies the process of rebinding a 
   * non-serializable object into a JNDI context. This version binds the
   * target object into the default IntitialContext using name path.
   * 
   * @param name the name to use as JNDI path name. The key into the 
   *   NonSerializableFactory map is obtained from the toString() value of name. 
   *   The name parameter cannot be a 0 length name.
   * @param target the non-Serializable object to bind.
   * @param createSubcontexts a flag indicating if subcontexts of name should 
   *   be created if they do not already exist.
   * @throws NamingException thrown on failure to rebind key into ctx.
   * @author Matt Carter
   **/
  public static synchronized void rebind(Name name, Object target, boolean createSubcontexts) throws NamingException
  {
     String key = name.toString();
     InitialContext ctx = new InitialContext();
     if (createSubcontexts == true && name.size() > 1)
     {
        int size = name.size() - 1;
        Util.createSubcontext(ctx, name.getPrefix(size));
     }
     rebind(ctx, key, target);
  }

//--- Begin ObjectFactory interface methods
   /** Transform the obj Reference bound into the JNDI namespace into the
   actual non-Serializable object.

   @param obj the object bound in the JNDI namespace. This must be an implementation
   of javax.naming.Reference with a javax.naming.RefAddr of type "nns" whose
   content is the String key used to location the non-Serializable object in the 
   NonSerializableFactory map.
   @param name ignored.
   @param nameCtx ignored.
   @param env ignored.

   @return the non-Serializable object associated with the obj Reference if one
   exists, null if one does not.
   */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable env)
       throws Exception
   { // Get the nns value from the Reference obj and use it as the map key
       Reference ref = (Reference) obj;
       RefAddr addr = ref.get("nns");
       String key = (String) addr.getContent();
       Object target = wrapperMap.get(key);
       return target;
   }
//--- End ObjectFactory interface methods
}


/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/


/**
* A static utility class for common JNDI operations.
* 
* @author Scott.Stark@jboss.org
* @author adrian@jboss.com
* @version $Revision: 2787 $
*/
@SuppressWarnings("unchecked")
class Util {
 /**
  * Create a subcontext including any intermediate contexts.
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx of the subcontext.
  * @return The new or existing JNDI subcontext
  * @throws javax.naming.NamingException
  *           on any JNDI failure
  */
 public static Context createSubcontext(Context ctx, String name) throws NamingException {
   Name n = ctx.getNameParser("").parse(name);
   return createSubcontext(ctx, n);
 }

 /**
  * Create a subcontext including any intermediate contexts.
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx of the subcontext.
  * @return The new or existing JNDI subcontext
  * @throws NamingException
  *           on any JNDI failure
  */
 public static Context createSubcontext(Context ctx, Name name) throws NamingException {
   Context subctx = ctx;
   for (int pos = 0; pos < name.size(); pos++) {
     String ctxName = name.get(pos);
     try {
       subctx = (Context) ctx.lookup(ctxName);
     } catch (NameNotFoundException e) {
       subctx = ctx.createSubcontext(ctxName);
     }
     // The current subctx will be the ctx for the next name component
     ctx = subctx;
   }
   return subctx;
 }

 /**
  * Bind val to name in ctx, and make sure that all intermediate contexts exist
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx where value will be bound
  * @param value
  *          the value to bind.
  * @throws NamingException
  *           for any error
  */
 public static void bind(Context ctx, String name, Object value) throws NamingException {
   Name n = ctx.getNameParser("").parse(name);
   bind(ctx, n, value);
 }

 /**
  * Bind val to name in ctx, and make sure that all intermediate contexts exist
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx where value will be bound
  * @param value
  *          the value to bind.
  * @throws NamingException
  *           for any error
  */
 public static void bind(Context ctx, Name name, Object value) throws NamingException {
   int size = name.size();
   String atom = name.get(size - 1);
   Context parentCtx = createSubcontext(ctx, name.getPrefix(size - 1));
   parentCtx.bind(atom, value);
 }

 /**
  * Rebind val to name in ctx, and make sure that all intermediate contexts
  * exist
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx where value will be bound
  * @param value
  *          the value to bind.
  * @throws NamingException
  *           for any error
  */
 public static void rebind(Context ctx, String name, Object value) throws NamingException {
   Name n = ctx.getNameParser("").parse(name);
   rebind(ctx, n, value);
 }

 /**
  * Rebind val to name in ctx, and make sure that all intermediate contexts
  * exist
  * 
  * @param ctx
  *          the parent JNDI Context under which value will be bound
  * @param name
  *          the name relative to ctx where value will be bound
  * @param value
  *          the value to bind.
  * @throws NamingException
  *           for any error
  */
 public static void rebind(Context ctx, Name name, Object value) throws NamingException {
   int size = name.size();
   String atom = name.get(size - 1);
   Context parentCtx = createSubcontext(ctx, name.getPrefix(size - 1));
   parentCtx.rebind(atom, value);
 }

 /**
  * Unbinds a name from ctx, and removes parents if they are empty
  * 
  * @param ctx
  *          the parent JNDI Context under which the name will be unbound
  * @param name
  *          The name to unbind
  * @throws NamingException
  *           for any error
  */
 public static void unbind(Context ctx, String name) throws NamingException {
   unbind(ctx, ctx.getNameParser("").parse(name));
 }

 /**
  * Unbinds a name from ctx, and removes parents if they are empty
  * 
  * @param ctx
  *          the parent JNDI Context under which the name will be unbound
  * @param name
  *          The name to unbind
  * @throws NamingException
  *           for any error
  */
 public static void unbind(Context ctx, Name name) throws NamingException {
   ctx.unbind(name); // unbind the end node in the name
   int sz = name.size();
   // walk the tree backwards, stopping at the domain
   while (--sz > 0) {
     Name pname = name.getPrefix(sz);
     try {
       ctx.destroySubcontext(pname);
     } catch (NamingException e) {
       System.out.println("Unable to remove context " + pname + e);
       break;
     }
   }
 }

 /**
  * Lookup an object in the default initial context
  * 
  * @param name
  *          the name to lookup
  * @param clazz
  *          the expected type
  * @return the object
  * @throws Exception
  *           for any error
  */
 public static Object lookup(String name, Class<?> clazz) throws Exception {
   InitialContext ctx = new InitialContext();
   try {
     return lookup(ctx, name, clazz);
   } finally {
     ctx.close();
   }
 }

 /**
  * Lookup an object in the default initial context
  * 
  * @param name
  *          the name to lookup
  * @param clazz
  *          the expected type
  * @return the object
  * @throws Exception
  *           for any error
  */
 public static Object lookup(Name name, Class<?> clazz) throws Exception {
   InitialContext ctx = new InitialContext();
   try {
     return lookup(ctx, name, clazz);
   } finally {
     ctx.close();
   }
 }

 /**
  * Lookup an object in the given context
  * 
  * @param context
  *          the context
  * @param name
  *          the name to lookup
  * @param clazz
  *          the expected type
  * @return the object
  * @throws Exception
  *           for any error
  */
 public static Object lookup(Context context, String name, Class clazz) throws Exception {
   Object result = context.lookup(name);
   checkObject(context, name, result, clazz);
   return result;
 }

 /**
  * Lookup an object in the given context
  * 
  * @param context
  *          the context
  * @param name
  *          the name to lookup
  * @param clazz
  *          the expected type
  * @return the object
  * @throws Exception
  *           for any error
  */
 public static Object lookup(Context context, Name name, Class clazz) throws Exception {
   Object result = context.lookup(name);
   checkObject(context, name.toString(), result, clazz);
   return result;
 }

 /**
  * Create a link
  * 
  * @param fromName
  *          the from name
  * @param toName
  *          the to name
  * @throws NamingException
  *           for any error
  */
 public static void createLinkRef(String fromName, String toName) throws NamingException {
   InitialContext ctx = new InitialContext();
   createLinkRef(ctx, fromName, toName);
 }

 /**
  * Create a link
  * 
  * @param ctx
  *          the context
  * @param fromName
  *          the from name
  * @param toName
  *          the to name
  * @throws NamingException
  *           for any error
  */
 public static void createLinkRef(Context ctx, String fromName, String toName)
     throws NamingException {
   LinkRef link = new LinkRef(toName);
   Context fromCtx = ctx;
   Name name = ctx.getNameParser("").parse(fromName);
   String atom = name.get(name.size() - 1);
   for (int n = 0; n < name.size() - 1; n++) {
     String comp = name.get(n);
     try {
       fromCtx = (Context) fromCtx.lookup(comp);
     } catch (NameNotFoundException e) {
       fromCtx = fromCtx.createSubcontext(comp);
     }
   }

   System.out.println("atom: " + atom);
   System.out.println("link: " + link);

   fromCtx.rebind(atom, link);

   System.out.println("Bound link " + fromName + " to " + toName);
 }

 /**
  * Remove the link ref
  * 
  * @param name
  *          the name of the link binding
  * @throws NamingException
  *           for any error
  */
 public static void removeLinkRef(String name) throws NamingException {
   InitialContext ctx = new InitialContext();
   removeLinkRef(ctx, name);
 }

 /**
  * Remove the link ref
  * 
  * @param ctx
  *          the context
  * @param name
  *          the name of the link binding
  * @throws NamingException
  *           for any error
  */
 public static void removeLinkRef(Context ctx, String name) throws NamingException {
   System.out.println("Unbinding link " + name);
   ctx.unbind(name);
 }

 /**
  * Checks an object implements the given class
  * 
  * @param context
  *          the context
  * @param name
  *          the name to lookup
  * @param object
  *          the object
  * @param clazz
  *          the expected type
  * @throws Exception
  *           for any error
  */
 protected static void checkObject(Context context, String name, Object object, Class clazz)
     throws Exception {
   Class objectClass = object.getClass();
   if (clazz.isAssignableFrom(objectClass) == false) {
     StringBuffer buffer = new StringBuffer(100);
     buffer.append("Object at '").append(name);
     buffer.append("' in context ").append(context.getEnvironment());
     buffer.append(" is not an instance of ");
     appendClassInfo(buffer, clazz);
     buffer.append(" object class is ");
     appendClassInfo(buffer, object.getClass());
     throw new ClassCastException(buffer.toString());
   }
 }

 /**
  * Append Class Info
  * 
  * @param buffer
  *          the buffer to append to
  * @param clazz
  *          the class to describe
  */
 protected static void appendClassInfo(StringBuffer buffer, Class clazz) {
   buffer.append("[class=").append(clazz.getName());
   buffer.append(" classloader=").append(clazz.getClassLoader());
   buffer.append(" interfaces={");
   Class[] interfaces = clazz.getInterfaces();
   for (int i = 0; i < interfaces.length; ++i) {
     if (i > 0)
       buffer.append(", ");
     buffer.append("interface=").append(interfaces[i].getName());
     buffer.append(" classloader=").append(interfaces[i].getClassLoader());
   }
   buffer.append("}]");
 }
}
