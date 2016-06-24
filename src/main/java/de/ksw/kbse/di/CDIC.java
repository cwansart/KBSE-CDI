/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di;

import java.lang.reflect.Field;
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

    public Object init(Class clazz) {
        return inject(clazz);
    }

    private static Object inject(Class clazz) throws SecurityException {
        Object object = new Object();
        try {
            object = clazz.newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Klasse " + clazz.getName() + " besitzt keinen Default-Konstruktor!", ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(CDIC.class.getName()).log(Level.SEVERE, "Konstruktor ist private...", ex);
            // TODO: getInstance() Methode versuchen aufzurufen
            return object;
        }

        // Prüfen ob Injection Points vorhanden sind und entsprechende Injections durchführen.
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                if(field.isAnnotationPresent(Named.class)) {
                    NamedInjector.inject(object, field);
                } else if(field.isAnnotationPresent(Qualifier.class)) {
                    QualifierInjector.inject(object, field);
                } else {
                    Injector.inject(object, field);
                }
            }
        }

        return object;
    }
}
