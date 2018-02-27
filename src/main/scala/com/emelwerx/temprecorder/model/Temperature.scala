package com.emelwerx.temprecorder.model

final case class Temperature(location: String, dateTime: String, temp: Long) {
  println(s"* creating a temp ($location, $dateTime, $temp)")
}