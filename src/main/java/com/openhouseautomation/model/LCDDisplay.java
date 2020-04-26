/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.model;

import com.google.common.base.MoreObjects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

/**
 *
 * @author dave
 */
@Entity
@Unindex
@Cache
public class LCDDisplay {

    @Id
    String displayname;

    String displaystring;

    public LCDDisplay() {
    }

    public void setDisplayName(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayName() {
        return displayname;
    }

    public void setDisplayString(String displaystring) {
        this.displaystring = displaystring;
    }

    public String getDisplayString() {
        return displaystring;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("displayname", displayname)
                .add("displaystring", displaystring)
                .toString();
    }
}
