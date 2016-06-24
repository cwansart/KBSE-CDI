/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di;

/**
 *
 * @author Larr
 */
public class Main {
    public static void main(String[] args) {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(";");
        System.out.println("ClassPath: " + classpath);
        for(String path: paths) {
            System.out.println(path);
        }
    }
}
