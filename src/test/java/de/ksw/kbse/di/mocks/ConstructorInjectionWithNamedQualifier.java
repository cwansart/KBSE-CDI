package de.ksw.kbse.di.mocks;

import javax.inject.Inject;
import javax.inject.Named;

public class ConstructorInjectionWithNamedQualifier {

    public NamedClass namedClass;

    @Inject
    public ConstructorInjectionWithNamedQualifier(@Named("namedClass") NamedClass namedClass) {
        this.namedClass = namedClass;
    }
}
