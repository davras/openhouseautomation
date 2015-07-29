/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openhouseautomation;

/**
 *
 * @author dras
 */
public class GregorianCalendar extends com.ibm.icu.util.GregorianCalendar {
    public GregorianCalendar(){
        super();
    }
    
    public GregorianCalendar( java.util.GregorianCalendar fromcal ){
        this.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone("GMT"));
        this.setTimeInMillis(fromcal.getTimeInMillis());
    }
    
}
