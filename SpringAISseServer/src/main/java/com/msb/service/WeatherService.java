package com.msb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class WeatherService {

   private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);


   public String getWeatherReport(){
       logger.info("天气情况信息");
       return "获取气象报告";
   }




}
