package org.meeboo.model;

import lombok.Data;

@Data
public class SunriseSunsetModel {

    private String sunrise;
    private String sunset;
    private String solar_noon;
    private String day_length;
    private String civil_twilight_begin;
    private String civil_twilight_end;
    private String nautical_twilight_begin;
    private String nautical_twilight_end;
    private String astronomical_twilight_begin;
    private String astronomical_twilight_end;

}
