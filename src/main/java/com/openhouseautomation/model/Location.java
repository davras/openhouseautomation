/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 *
 * @author dras
 */
@Entity
public class Location {
    @Id String name;
    String zipcode;
    
    public void setName(String name) {
        this.name=name;
    }
    public String getName() {
        return name;
    }
    public void setZipcode(String zipcode) {
        this.zipcode=zipcode;
    }
    public String getZipcode() {
        return zipcode;
    }
}
