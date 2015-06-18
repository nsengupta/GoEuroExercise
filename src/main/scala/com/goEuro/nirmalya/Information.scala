package com.goEuro.nirmalya

/**
 * Created by nirmalya on 17/6/15.
 */
case class Information(_id: Int,
                       key: Option[String]=None,
                       name: Option[String]=None,
                       fullName: Option[String]=None,
                       iata_airport_code: Option[String]=None,
                       iType: Option[String]=None,
                       country: Option[String]=None,
                       geo_position: Option[GeoPosition]=Some(GeoPosition(0.0d,0.0d)),
                       locationId: Option[Int]=None,
                       inEurope: Option[Boolean]=None,
                       countryCode: Option[String]=None,
                       coreCountry: Option[Boolean]=None,
                       distance: Option[String]=None)

