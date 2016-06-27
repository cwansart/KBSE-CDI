package de.ksw.kbse.di.mocks;

import javax.inject.Inject;

public class Baa {

    public Boo boo;

    @Inject
    public Baa(Boo boo) {
        this.boo = boo;
    }
}
