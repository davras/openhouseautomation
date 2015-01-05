package com.openhouseautomation.model;

import org.joda.time.DateTime;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

/**
 * A class representing a reading from a sensor device.
 *
 * @author dras@google.com (David Ras)
 */
@Entity
@Index
@Cache
public class ReadingHistory {

    @Parent Key<Sensor> sensor;
    @Id String id;
    String high;
    String low;
    String average;
    String total;
    DateTime timestamp;

    /**
     * Empty constructor for objectify.
     */
    public ReadingHistory() {
    }

    /**
     * Returns the parent {@code sensor} of the {@link ReadingHistory}.
     */
    public Key<Sensor> getSensor() {
        return sensor;
    }

    /**
     * Sets the {@code sensor} Parent Key of the {@link ReadingHistory}.
     *
     * @param sensor the sensor to set
     */
    public void setSensor(Key<Sensor> sensor) {
        this.sensor = sensor;
    }

    /**
     * Returns the {@code id} of the {@link ReadingHistory}.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the {@code id} of the {@link ReadingHistory}.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the {@code high} of the {@link ReadingHistory}.
     */
    public String getHigh() {
        return high;
    }

    /**
     * Sets the {@code high} of the {@link ReadingHistory}.
     *
     * @param high the id to set
     */
    public void setHigh(String value) {
        this.high = value;
    }

    /**
     * Returns the {@code low} of the {@link ReadingHistory}.
     */
    public String getLow() {
        return low;
    }

    /**
     * Sets the {@code low} of the {@link ReadingHistory}.
     *
     * @param low the id to set
     */
    public void setLow(String value) {
        this.low = value;
    }

    public String getAverage() {
        return this.average;
    }

    public void setAverage(String value) {
        this.average = value;
    }

    public void setTotal(String total) {
      this.total = total;
    }
    public String getTotal() {
      return total;
    }
    /**
     * Returns the {@code timestamp} of the {@link ReadingHistory}.
     */
    public DateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the {@code timestamp} of the {@link ReadingHistory}.
     *
     * @param id the id to set
     */
    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sensor,
                id,
                high,
                low,
                timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReadingHistory)) {
            return false;
        }

        ReadingHistory otherReading = (ReadingHistory) obj;
        return Objects.equal(this.sensor, otherReading.getSensor())
                && Objects.equal(this.id, otherReading.getId())
                && Objects.equal(this.high, otherReading.getHigh())
                && Objects.equal(this.low, otherReading.getLow())
                && Objects.equal(this.timestamp, otherReading.getTimestamp());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass().getName())
                .add("sensor", sensor)
                .add("id", id)
                .add("high", high)
                .add("low", low)
                .add("average", average)
                .add("timestamp", timestamp)
                .toString();
    }
}
