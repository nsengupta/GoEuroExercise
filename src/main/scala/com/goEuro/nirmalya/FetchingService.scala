package com.goEuro.nirmalya

import scalaj.http.{Http, HttpResponse}

/**
 * Created by nirmalya on 17/6/15.
 */
trait GoEuroInfoProviderService {

  def fetchInformation(fromURL: String, withParam: String): HttpResponse[String]

}

class CityInfoProviderService  extends GoEuroInfoProviderService {

   def fetchInformation (goEuroURL: String, cityName: String)= Http(goEuroURL + "/" + cityName).asString

}
