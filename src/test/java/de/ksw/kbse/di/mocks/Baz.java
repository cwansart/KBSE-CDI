/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di.mocks;

import javax.inject.Inject;

public class Baz {

    @Inject
    public Boo boo;
    public String name = "Baz";
}
