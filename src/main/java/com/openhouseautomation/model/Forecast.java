/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openhouseautomation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;
import com.google.common.base.Objects;
import com.googlecode.objectify.annotation.*;

/**
 *
 * @author dras
 */
@Entity
@Unindex
@Cache
public class Forecast {

    @Id
    @Index
    String zipcode;
    String forecasthigh;
    String forecastlow;
    String forecastpop;
    @JsonIgnore
    DateTime lastupdate;
    
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

    public void setLastUpdate(DateTime lastupdate) {
        this.lastupdate = lastupdate;
    }
    
    public DateTime getLastUpdate() {
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
