/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.model;

import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import java.util.Date;

/**
 *
 * @author dras
 */
@Entity
@Index
@Cache
public class Forecast {

    @Id
    String zipcode;
    String forecasthigh;
    String forecastlow;
    String forecastpop;
    Date lastupdate;
    
    public Forecast() {
    }

    public void setForecastHigh(String forecasthigh) {
        this.forecasthigh = forecasthigh;
    }

    public String getForecastHigh() {
        return forecasthigh;
    }

    public void setForecastLow(String forecastlow) {
        this.forecastlow = forecastlow;
    }

    public String getForecastLow() {
        return forecastlow;
    }

    public void setForecastPop(String forecastpop) {
        this.forecastpop = forecastpop;
    }

    public String getForecastPop() {
        return forecastpop;
    }

    public void setZipCode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getZipCode() {
        return zipcode;
    }

    public void setLastUpdate(Date lastupdate) {
        this.lastupdate = lastupdate;
    }
    
    public Date getLastUpdate() {
        return lastupdate;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass().getName())
                .add("zipcode", zipcode)
                .add("forecasthigh", forecasthigh)
                .add("forecastlow", forecastlow)
                .add("forecastpop", forecastpop)
                .add("lastupdate", lastupdate)
                .toString();
    }
}
