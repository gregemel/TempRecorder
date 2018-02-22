package model

final case class Temperature(location: String, dateTime: String, temp: Long) {
  println(s"p1 creating a temp ($location, $temp)")
}