package de.ksw.kbse.di;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

/**
 * Main class for the CDI container. You can initialize the container by calling
 * the init method.
 *
 * @author Florian Kruckmann
 * @author Larissa Schenk
 * @author Christian Wansart
 */
public class CDIC {

    private ClassIndexer classIndexer;

    /**
     * Field injection with simple @Inject annotation.
     *
     * @param object the object to be injected
     * @param field the field to be injected
     */
    private <T> void simpleInjection(Object object, Field field) {
        Class clazz;
        if (field.getType().isInterface()) {
            ClassInfo classInfo = classIndexer.getInterfaceFile(field.getType().getName());
            clazz = loadClass(classInfo);
        } else {
            clazz = field.getType();
        }

        injectField(clazz, field, object);
    }

    /**
     * Injects fields annotated with qualifiers.
     *
     * @param object the object to be injected
     * @param field the field to be injected
     * @param annotation the annotated qualifier
     */
    private <T> void qualifierInjection(T object, Field field, Annotation annotation) {
        ClassInfo qualifierFile = classIndexer.getQualifierFile(annotation.annotationType().getTypeName());
        Class clazz = loadClass(qualifierFile);

        injectField(clazz, field, object);
    }

    /**
     * Injects named qualified fields.
     *
     * @param object the object to be injected
     * @param field the field to be injected
     */
    private <T> void namedInjection(T object, Field field) {
        Named annotation = field.getAnnotation(Named.class);
        ClassInfo namedFile = classIndexer.getNamedFile(annotation.value());
        Class clazz = loadClass(namedFile);

        injectField(clazz, field, object);
    }

    /**
     * Sets the given object to the field and calls inject (recursion) on the
     * new instance afterwards.
     *
     * @param clazz the class type that will be injected
     * @param field the field that will be injected
     * @param object the object that holds the field
     */
    private <T> void injectField(Class clazz, Field field, T object) {
        T fieldInstance = newInstance(clazz, field.getType());
        setField(field, object, fieldInstance);
        inject(fieldInstance);
    }
    
    private <T> void methodInjection(T object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Inject.class)) {
                try {
                    Parameter[] parameters = method.getParameters();
                    Object[] params = new Object[method.getParameterCount()];
                    for (int i = 0; i < params.length; i++) {
                        Class parameterType;

                        if (parameters[i].getType().isInterface()) {
                            ClassInfo interfaceFile = classIndexer.getInterfaceFile(parameters[i].getType().getName());
                            parameterType = loadClass(interfaceFile);
                        } else if (parameters[i].isAnnotationPresent(Named.class)) {
                            Named annotation = parameters[i].getAnnotation(Named.class);
                            ClassInfo namedFile = classIndexer.getNamedFile(annotation.value());
                            if(namedFile == null){
                                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Konnte Klasse (" + annotation.value() +") nicht finden!");
                                return;
                            }
                            parameterType = loadClass(namedFile);
                        } else {
                            Annotation[] annotations = parameters[i].getAnnotations();
                            parameterType = parameters[i].getType();
                            for (Annotation annotation : annotations) {
                                if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                                    ClassInfo qualifierFile = classIndexer.getQualifierFile(annotation.annotationType().getName());
                                    if(qualifierFile == null){
                                        Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Konnte Klasse (" + annotation.annotationType().getName() +") nicht finden!");
                                        return;
                                    }
                                    parameterType = loadClass(qualifierFile);
                                    break;
                                }
                            }
                        }
                        params[i] = parameterType.newInstance();
                        inject(params[i]);
                    }
                    method.invoke(object, params);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Creates a new instance of the given class. It also calls the constructor
     * injection if it's not possible to create a new instance via default
     * constructor.
     *
     * @param clazz the class for which the new instance will be created
     * @param fieldType the field that will be injected
     * @return the created object
     */
    private <T> T newInstance(Class clazz, Class fieldType) {
        T fieldInstance = null;
        try {
            fieldInstance = (T) clazz.newInstance();
        } catch (InstantiationException ex) {
            //Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " besitzt keinen Default-Konstruktor!", ex);
            fieldInstance = constructorInjection(clazz);
        } catch (IllegalAccessException ex) {
            try {//Prüfen ob getInstance verfügbar ist und wenn möglich aufrufen.
                Method getInstanceMethod = fieldType.getMethod("getInstance");
                fieldInstance = (T) getInstanceMethod.invoke(null);
            } catch (NoSuchMethodException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " besitzt keinen public Default-Konstruktor!", ex1);
            } catch (SecurityException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex1);
            } catch (IllegalAccessException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " getInstance() ist nicht public!", ex1);
            } catch (IllegalArgumentException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " getInstance() benötigt zusätzliche Argumente", ex1);
            } catch (InvocationTargetException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " getInstance() warf eine Exeption", ex1);
            }
        }
        return fieldInstance;
    }

    /**
     * Entry point for the CDI container.
     *
     * @param clazz the class that will used for injection
     * @return the created class
     */
    public <T> T init(Class clazz) {
        classIndexer = new ClassIndexer(clazz);

        T object;
        try {
            object = (T) clazz.newInstance();
        } catch (InstantiationException ex) {
            //Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Klasse " + clazz.getName() + " besitzt keinen Default-Konstruktor!", ex);
            object = constructorInjection(clazz);
            if (object == null) {
                return null;
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Konstruktor ist private...", ex);
            return null;
        }
        return inject(object);
    }

    /**
     * Performs constructor injection.
     *
     * @param clazz the class will be checked for constructor injection
     * @return the created object by running the injected constructor
     */
    public <T> T constructorInjection(Class clazz) {
        T object = null;
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                try {
                    Parameter[] parameters = constructor.getParameters();
                    Object[] params = new Object[constructor.getParameterCount()];
                    for (int i = 0; i < params.length; i++) {
                        Class parameterType;

                        if (parameters[i].getType().isInterface()) {
                            ClassInfo interfaceFile = classIndexer.getInterfaceFile(parameters[i].getType().getName());
                            parameterType = loadClass(interfaceFile);
                        } else if (parameters[i].isAnnotationPresent(Named.class)) {
                            Named annotation = parameters[i].getAnnotation(Named.class);
                            ClassInfo namedFile = classIndexer.getNamedFile(annotation.value());
                            parameterType = loadClass(namedFile);
                        } else {
                            Annotation[] annotations = parameters[i].getAnnotations();
                            parameterType = parameters[i].getType();
                            for (Annotation annotation : annotations) {
                                if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                                    ClassInfo qualifierFile = classIndexer.getQualifierFile(annotation.annotationType().getName());
                                    parameterType = loadClass(qualifierFile);
                                    break;
                                }
                            }
                        }
                        params[i] = parameterType.newInstance();
                    }
                    object = (T) constructor.newInstance(params);
                } catch (InstantiationException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Das zu erzeugende Objekt ist Abstrakt!", ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Es existiert kein öffentlicher Konstruktor!", ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Die übergebenen Parameter passen nicht überein!", ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Der Konstruktor warf eine Exception!", ex);
                }
            }
        }
        return object;
    }

    /**
     * Recursive method for injection.
     *
     * @param object the object that will be created
     * @return the created object
     */
    private <T> T inject(T object) {
        // Injecting fields
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {

                if (field.isAnnotationPresent(Named.class)) {
                    namedInjection(object, field);
                } else {
                    Annotation[] annotations = field.getAnnotations();
                    boolean isQualifier = false;
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                            isQualifier = true;
                            qualifierInjection(object, field, annotation);
                        }
                    }

                    if (!isQualifier) {
                        simpleInjection(object, field);
                    }
                }
            }
        }
        
        methodInjection(object);
        
        return object;
    }

    /**
     * Sets a field of the given object.
     *
     * @param field the field that will be set
     * @param object the object that holds the field
     * @param fieldInstance the object that will be set in the field
     */
    private <T> void setField(Field field, Object object, T fieldInstance) {
        try {
            field.setAccessible(true);
            field.set(object, fieldInstance);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Feld " + field.getName() + " ist kein Objekt!", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Feld " + field.getName() + " ist nicht zugreifbar!", ex);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Loads a class on runtime.
     *
     * @param classInfo class info for the class that will be loaded
     * @return the loaded class
     */
    private Class loadClass(ClassInfo classInfo) {
        Class type = null;
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{
                new File(classInfo.getPath()).toURI().toURL()
            });
            type = classLoader.loadClass(classInfo.getName());
        } catch (MalformedURLException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Class url stimmt nicht. Ggf. hat der ClassIndexer einen falschen Pfad!", ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Klasse konnte nicht gefunden werden!", ex);
        }
        return type;
    }
}
