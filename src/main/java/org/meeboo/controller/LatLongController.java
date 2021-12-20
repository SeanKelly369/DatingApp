package org.meeboo.controller;

import lombok.extern.slf4j.Slf4j;
import org.meeboo.component.HttpClientComponent;
import org.meeboo.model.SunriseSunsetModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api")
public class LatLongController {

    @Autowired
    HttpClientComponent httpClientComponent;

    @GetMapping(value = "/getLatLon", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getLatLong2(@RequestParam("lat") String lat, @RequestParam("lon") String lon, @RequestParam("date") Long date) throws Exception {
        Date date1 = new Date(date);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate = dateFormat.format(date1);
        SunriseSunsetModel sunriseSunsetModel = new SunriseSunsetModel();
        String uri = "http://api.sunrise-sunset.org/json?lat=" + lat + "&lon=" + lon + "&date=" + strDate;
        return httpClientComponent.httpClientGet(uri);
    }
}
