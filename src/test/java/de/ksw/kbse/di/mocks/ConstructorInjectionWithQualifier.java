package de.ksw.kbse.di.mocks;

import javax.inject.Inject;

public class ConstructorInjectionWithQualifier {

    public QualifiedClass qualifiedClass;

    @Inject
    public ConstructorInjectionWithQualifier(@MyQualifier QualifiedClass qualifiedClass) {
        this.qualifiedClass = qualifiedClass;
    }
}
