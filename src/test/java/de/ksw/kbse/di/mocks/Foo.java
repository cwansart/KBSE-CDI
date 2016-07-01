/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di.mocks;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Christian
 */
public class Foo {

    @Inject
    public Baa baa;

    @Inject
    public Baz baz;

    @Inject
    public Bar bar;

    @Inject
    @Named("namedClass")
    public Object namedClass;

    @Inject
    @MyQualifier
    public Object qualifiedClass;
    
    @Inject
    public ConstructorInjectionWithQualifier constructorInjectionWithQualifier;
    
    @Inject
    public ConstructorInjectionWithNamedQualifier constructorInjectionWithNamedQualifier;
    
    public NamedClass nameOverMethod;
    
    @Inject
    public void setBaaOverMethod(@Named("namedClass") NamedClass name){
        this.nameOverMethod = name;
    }
}
