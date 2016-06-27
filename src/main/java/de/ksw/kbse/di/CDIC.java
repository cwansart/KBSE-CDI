/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di;

import java.io.File;
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

    private void simpleInjection(Object object, Field field) {
        Object fieldInstance;
        Class type;

        if (field.getType().isInterface()) {
            ClassInfo classInfo = classIndexer.getInterfaceFile(field.getType().getName());

            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{
                    new File(classInfo.getPath()).toURI().toURL()
                });
                type = classLoader.loadClass(classInfo.getName());
            } catch (MalformedURLException ex) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Class url stimmt nicht. Ggf. hat der ClassIndexer einen falschen Pfad!", ex);
                return;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Klasse konnte nicht gefunden werden!", ex);
                return;
            }
        } else {
            type = field.getType();
        }
        
        try {
            fieldInstance = type.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, field.getType().getName() + " besitzt keinen Default-Konstruktor!", ex);
            return;
        } catch (IllegalAccessException ex) {
            try {//Prüfen ob getInstance verfügbar ist und wenn möglich aufrufen.
                Method getInstanceMethod = field.getType().getMethod("getInstance");
                fieldInstance = getInstanceMethod.invoke(object);
            } catch (NoSuchMethodException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, field.getType().getName() + " besitzt keinen public Default-Konstruktor!", ex1);
                return;
            } catch (SecurityException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, null, ex1);
                return;
            } catch (IllegalAccessException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, field.getType().getName() + " getInstance() ist nicht public!", ex1);
                return;
            } catch (IllegalArgumentException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, field.getType().getName() + " getInstance() benötigt zusätzliche Argumente", ex1);
                return;
            } catch (InvocationTargetException ex1) {
                Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, field.getType().getName() + " getInstance() warf eine Exeption", ex1);
                return;
            }
        }

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

        inject(fieldInstance);

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

                } else if (field.isAnnotationPresent(Qualifier.class)) {

                } else {
                    simpleInjection(object, field);
                }
            }
        }

        return object;
    }
}
