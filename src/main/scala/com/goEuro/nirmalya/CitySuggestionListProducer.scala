package com.goEuro.nirmalya

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Success, Try}
import scalaj.http.HttpResponse

/**
 * The main work-horse of the application. It does the job of fetching information about a given CITY
 * by calling the REST Service of GoEuro, and parsing the response, if possible. A successfully parsed
 * response is transformed to a List of {@com.goEuro.nirmalya.Information}.
 * Created by nirmalya on 18/6/15.
 */
class CitySuggestionListProducer (val csvFileName: String = "./City-Suggestions.csv" ) {

  implicit val formats = DefaultFormats

  val fetchFailedID   = -1
  val errorOccurredID = -2
  val parseFailedID   = -3
  val emptyBodyID     = -4

  val inapplicable_geo_position = GeoPosition(0.0d, 0.0d)

  // Fetches a Http Response from the GoEuro URL provided, by supplying the name of the CITY
  // as the parameter. Exceptions - if any raised - are caught and an appropriate Http Response
  // is returned which indicates that all has not been well with the REST API call.

  def seekResponse(targetURL: String, city: String, service: GoEuroInfoProviderService) = {

    Try {
      service.fetchInformation(targetURL,city)
    }
    match {
      case Success(receivedResponse) => receivedResponse
      case Failure(error)    => HttpResponse[String](error.getMessage,-1,Map("GoEuroStatus" -> "call failed"))
    }
  }

  // Parses a Http Response. If the Response indicates that things have not gone well while fetching it
  // from the GoEuro's REST Endpoint, then an appropriate {com.goEuro.nirmalya.Information} is created to
  // encapsulate the incompleteness of REST API call; otherwise its payload-contents (JSON) are extracted
  // to instances of {@com.goEuro.nirmalya.Information}.
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

  // Self-explanatory
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
      case Failure(ex) =>
        println("Failed to save to file <" + csvFileName + ">, " + ex.getMessage)
        
    }
  }

}
