/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

/**
 *
 * @author Larr
 */
public class CDIC {

    private ClassIndexer classIndexer;

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

    private <T> void qualifierInjection(T object, Field field, Annotation annotation) {
        ClassInfo qualifierFile = classIndexer.getQualifierFile(annotation.annotationType().getTypeName());
        Class clazz = loadClass(qualifierFile);

        injectField(clazz, field, object);
    }

    private <T> void namedInjection(T object, Field field) {
        Named annotation = field.getAnnotation(Named.class);
        ClassInfo namedFile = classIndexer.getNamedFile(annotation.value());
        Class clazz = loadClass(namedFile);

        injectField(clazz, field, object);
    }

    private <T> void injectField(Class clazz, Field field, T object) throws SecurityException {
        T fieldInstance = newInstance(clazz, field.getType());
        setField(field, object, fieldInstance);
        inject(fieldInstance);
    }

    private <T> T newInstance(Class clazz, Class fieldType) {
        T fieldInstance = null;
        try {
            fieldInstance = (T) clazz.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, fieldType.getName() + " besitzt keinen Default-Konstruktor!", ex);

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

    public <T> T init(Class clazz) {
        classIndexer = new ClassIndexer(clazz);

        T object;
        try {
            object = (T) clazz.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Klasse " + clazz.getName() + " besitzt keinen Default-Konstruktor!", ex);
            return null;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Konstruktor ist private...", ex);
            // TODO: getInstance() Methode versuchen aufzurufen
            return null;
        }
        return inject(object);
    }

    private <T> T inject(T object) throws SecurityException {
        // Prüfen ob Injection Points vorhanden sind und entsprechende Injections durchführen.
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

        return object;
    }

    private <T> void setField(Field field, Object object, T fieldInstance) throws SecurityException {
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
