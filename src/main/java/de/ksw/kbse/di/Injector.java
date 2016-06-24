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
public class Injector {

    static void inject(Object object, Field field) {
        if (field.getType().isInterface()) {

        } else {
            try {
                Object instance = field.getType().newInstance();
                field.setAccessible(true);

                field.setAccessible(false);
            } catch (InstantiationException ex) {
                Logger.getLogger(Injector.class.getName()).log(Level.SEVERE, field.getType().getName() + " enthält keinen Default-Konstruktor", ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Injector.class.getName()).log(Level.SEVERE, "Konstruktor von " + field.getType().getName() + " enthält keinen öffentlichen Default-Konstruktor", ex);
            }

        }

    }

}
