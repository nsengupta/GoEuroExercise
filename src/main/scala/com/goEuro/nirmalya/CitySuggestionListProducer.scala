package com.goEuro.nirmalya

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Success, Try}
import scalaj.http.HttpResponse

/**
 * Created by nirmalya on 18/6/15.
 */
class CitySuggestionListProducer (val csvFileName: String = "./City-Suggestions.csv" ) {

  implicit val formats = DefaultFormats

  val fetchFailedID   = -1
  val errorOccurredID = -2
  val parseFailedID   = -3
  val emptyBodyID     = -4

  val inapplicable_geo_position = GeoPosition(0.0d, 0.0d)

  // val defaultTargetURL = "http://api.goeuro.com/api/v2/position/suggest/en"

  def seekResponse(targetURL: String, city: String, service: GoEuroInfoProviderService) = {

    Try {
      service.fetchInformation(targetURL,city)
    }
    match {
      case Success(receivedResponse) => receivedResponse
      case Failure(error)    => HttpResponse[String](error.getMessage,-1,Map("GoEuroStatus" -> "call failed"))
    }
  }

  def parseResponse(response: HttpResponse[String]) = {

    response match {

      case HttpResponse(body,-1,headerParams) => List(Information(fetchFailedID, name=Some(headerParams("status") + ":" + body)))
      case _ => if (response.isError || response.isServerError)
        List(Information(errorOccurredID,name=Some(response.headers("Status"))))
      else
        prepareInformation(response)
    }
  }

  def prepareInformation(response: HttpResponse[String]) = {

    if (response.body.isEmpty)
      List(Information(emptyBodyID,name=Some("Not found")))
    else {

      // [NS]: This step is necessary because 'type' is a language keyword; it cannot be used as a field-name in
      // user-supplied classes. So, the field is named as 'iType'. In the incoming JSON data, the 'term' type is
      // replaced with 'iType'. That way, json4s can work properly while filling in an instance of 'Information'
      // In any case, using 'type' as a JSON key is a not a good idea, IMHO.
      val modifiedResponse = response.copy(body = response.body.replaceAll(",\"type\":",",\"iType\":"))

      val info = Try {
        parse(modifiedResponse.body)
      } match {
        case Success(parsedBody) => parsedBody.extract[List[Information]]
        case Failure(reason)     => List(Information(parseFailedID,name=Some(reason.getMessage)))
      }

      info
    }
  }

  def saveAsCSV(informationBunch: List[Information]) = {

    Try {
      new File(csvFileName)
    } match {
      case Success(f) =>
        val writerHandle = CSVWriter.open(f)
        writerHandle.writeRow(List("_id","name","type","latitude","longitude"))
        if (informationBunch.isEmpty) {
          writerHandle
            .writeRow(
              List(emptyBodyID,
                ("No records found"),
                "",
                "",
                "")
            )
        }
        else {
          informationBunch.foreach(e =>
            writerHandle
              .writeRow(
                List(e._id,
                  e.name.getOrElse(""),
                  e.iType.get,
                  e.geo_position.get.latitude,
                  e.geo_position.get.longitude)
              )
          )
        }

        writerHandle.close
      case Failure(ex) => println("Failed to save to file <" + csvFileName + ">, " + ex.getMessage)
    }
  }

}
