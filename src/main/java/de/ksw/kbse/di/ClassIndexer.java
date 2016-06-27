package de.ksw.kbse.di;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

public class ClassIndexer {

    /*
     * Lists for indexing all class files.
     */
    private final List<String> interfaceInjectionPoints = new ArrayList<>();
    private final List<String> namedInjectionPoints = new ArrayList<>();
    private final List<String> qualifierInjectionPoints = new ArrayList<>();

    /*
     * Maps with the default implementations.
     */
    private final Map<String, ClassInfo> interfaceImplementations = new HashMap<>();
    private final Map<String, ClassInfo> namedImplementations = new HashMap<>();
    private final Map<String, ClassInfo> qualifierImplementations = new HashMap<>();
    
    /**
     * 
     */
    private String currentClassPath;

    /**
     * Starts the indexing process.
     *
     * @param clazz the class to be indexed
     */
    public ClassIndexer(Class clazz) {
        index(clazz);
        searchInClassPath();
    }

    /**
     * Indexes all injection points recursively.
     *
     * @param clazz class file to search for injection points
     */
    private void index(Class clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (field.getType().isInterface()) {
                    interfaceInjectionPoints.add(field.getType().getName());
                } else if (field.isAnnotationPresent(Named.class)) {
                    String namedValue = field.getAnnotation(Named.class).value();
                    namedInjectionPoints.add(namedValue);
                } else {
                    java.lang.annotation.Annotation[] annotations = field.getAnnotations();
                    for (java.lang.annotation.Annotation annotation : annotations) {
                        Class annotationType = annotation.annotationType();
                        if (annotationType.isAnnotationPresent(Qualifier.class)) {
                            qualifierInjectionPoints.add(annotationType.getName());
                        }
                    }
                }

                index(field.getType());
            }
        }
    }

    /**
     * Searches inside the classpath for the indexed classes.
     */
    private void searchInClassPath() {
        String[] classPaths = System.getProperty("java.class.path").split(";");
        for (String pathString : classPaths) {
            currentClassPath = pathString;
            File path = new File(pathString);
            if (path.isDirectory()) {
                searchInPath(path);
            }
        }
    }

    /**
     * Searches inside the specified path recursively for compiled class files.
     *
     * @param path current file path
     */
    private void searchInPath(File path) {
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                searchInPath(file);
            } else if (file.getName().toLowerCase().endsWith(".class")) {
                processClassFile(file);
            }
        }
    }

    /**
     * Processes the given file. It checks whether it's one of our indexed
     * objects. If so, it stores the class file in a map.
     *
     * @param file current processing file
     */
    private void processClassFile(File file) {
        try {
            CtClass loadedClass = ClassPool.getDefault().makeClass(new FileInputStream(file));
            ClassFile classFile = loadedClass.getClassFile();
            AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
            
            if (attribute != null) {
                processAnnotations(attribute, file);
            } else {
                processInterface(classFile, file);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClassIndexer.class.getName()).log(Level.SEVERE, "class-Datei konnte nicht gefunden werden!", ex);
        } catch (IOException ex) {
            Logger.getLogger(ClassIndexer.class.getName()).log(Level.SEVERE, "IO-Fehler aufgetreten!", ex);
        } catch (RuntimeException ex) {
            Logger.getLogger(ClassIndexer.class.getName()).log(Level.SEVERE, "RuntimeException während des Ladens!", ex);
        }
    }

    /**
     * Checks if the given interface/ClassFile is indexed and adds it to the
     * interfaceImplementation list.
     *
     * @param interfaceName the interface
     * @param file the current file
     * @throws RuntimeException if the interface is ambiguous
     */
    private void processInterface(ClassFile classFile, File file) throws RuntimeException {
        String[] interfaces = classFile.getInterfaces();
        for (String iface : interfaces) {
            if (interfaceInjectionPoints.contains(iface)) {
                if (interfaceImplementations.containsKey(iface)) {
                    throw new RuntimeException("Interface-Implementierung für " + iface + " ist nicht eindeutig!");
                }

                ClassInfo classInfo = new ClassInfo(iface, currentClassPath, file);
                interfaceImplementations.put(iface, classInfo);
            }
        }
    }

    /**
     * Checks if the class has one of the indexed qualifiers or is one of the
     * indexed named qualifiers.
     *
     * @param attribute
     * @param file
     * @throws RuntimeException
     */
    private void processAnnotations(AnnotationsAttribute attribute, File file) throws RuntimeException {
        Annotation[] annotations = attribute.getAnnotations();

        // First we need to check if the class is annotated with
        // @Named or an qualifier.
        for (Annotation annotation : annotations) {
            String typeName = annotation.getTypeName();
            if (typeName.equals(Named.class.getName())) {
                String namedValue = ((StringMemberValue) annotation.getMemberValue("value")).getValue();
                if (namedImplementations.containsKey(namedValue)) {
                    throw new RuntimeException("Named-Implementierung für " + namedValue + " ist nicht eindeutig!");
                }
                if (namedInjectionPoints.contains(namedValue)) {
                    ClassInfo classInfo = new ClassInfo(namedValue, currentClassPath, file);
                    namedImplementations.put(namedValue, classInfo);
                }
            } else if (qualifierInjectionPoints.contains(typeName)) {
                if (qualifierImplementations.containsKey(typeName)) {
                    throw new RuntimeException("Qualifier-Implementierung für " + typeName + " ist nicht eindeutig!");
                }
                ClassInfo classInfo = new ClassInfo(typeName, currentClassPath, file);
                qualifierImplementations.put(typeName, classInfo);
            }
        }
    }

    /**
     * Returns a File object of the default implementation of the given
     * interface if indexed.
     *
     * @param name given interface name
     * @return default implementation of the given interface
     */
    public ClassInfo getInterfaceFile(String name) {
        return interfaceImplementations.get(name);
    }

    /**
     * Returns a File object of the default implementation of the given named
     * type.
     *
     * @param name given named qualifier
     * @return default implementation of the given named qualifier
     */
    public ClassInfo getNamedFile(String name) {
        return namedImplementations.get(name);
    }

    /**
     * Returns a File object of the default implementation of the given
     * qualifier type.
     *
     * @param name given qualifier
     * @return default implementation of the given qualifier
     */
    public ClassInfo getQualifierFile(String name) {
        return qualifierImplementations.get(name);
    }

    /**
     * This is a test method. We should remove it afterwards.
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Named-Implementierngen: ")
                .append(System.lineSeparator());
        for (Map.Entry<String, ClassInfo> entry : namedImplementations.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getFile().getAbsoluteFile())
                    .append(System.lineSeparator());
        }

        builder.append(System.lineSeparator())
                .append("Qualifier-Implementierngen: ")
                .append(System.lineSeparator());
        for (Map.Entry<String, ClassInfo> entry : qualifierImplementations.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getFile().getAbsoluteFile())
                    .append(System.lineSeparator());
        }
        builder.append(System.lineSeparator())
                .append("Interface-Implementierngen: ")
                .append(System.lineSeparator());
        for (Map.Entry<String, ClassInfo> entry : interfaceImplementations.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getFile().getAbsoluteFile())
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }
}
