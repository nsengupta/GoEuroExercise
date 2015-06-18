package com.goEuro.nirmalya

import java.io.File

import com.github.tototoshi.csv.CSVReader
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.mockito.Mockito._

import scalaj.http.HttpResponse

/**
 * Created by nirmalya on 17/6/15.
 */
class ServiceTest  extends FunSuite with BeforeAndAfter with MockitoSugar {

  test("Non-existent REST endpoint") {

    val mockedGoEuroEndpoint = mock[GoEuroInfoProviderService]

    when (mockedGoEuroEndpoint.fetchInformation("DummyURL","DummyCity")).thenThrow(new RuntimeException("Invalid URL"))

    val listProducer = new CitySuggestionListProducer()

    val fetchedInfo = listProducer.seekResponse("DummyURL","DummyCity",mockedGoEuroEndpoint)

    assert(fetchedInfo.code == -1)
    assert(fetchedInfo.body == "Invalid URL")
    assert(fetchedInfo.headers("GoEuroStatus") == "call failed")

  }

  test("correct parsing for erroneous HttpResponse (server error)") {

    val httpReponse500 = HttpResponse("Some body",500,Map("Status" -> "Server Error"))

    val listProducer = new CitySuggestionListProducer()

    val parsedInfo = listProducer.parseResponse(httpReponse500)

    assert(parsedInfo.size == 1)
    assert(parsedInfo.head._id == -2)
    assert(parsedInfo.head.key.get == "Server Error")
    assert(parsedInfo.head.name.isEmpty == true)
    assert(parsedInfo.head.geo_position.get == GeoPosition(0.0d,0.0d))

  }

  test("correct parsing for erroneous HttpResponse (client error)") {

    val httpReponse500 = HttpResponse("Some body",400,Map("Status" -> "Client Error"))

    val listProducer = new CitySuggestionListProducer()

    val parsedInfo = listProducer.parseResponse(httpReponse500)

    assert(parsedInfo.size == 1)
    assert(parsedInfo.head._id == -2)
    assert(parsedInfo.head.key.get == "Client Error")
    assert(parsedInfo.head.name.isEmpty == true)
    assert(parsedInfo.head.geo_position.get == GeoPosition(0.0d,0.0d))

  }

  test("correct extraction of Information from HttpResponse (well-formed JSON)") {

    val jsonBody = "[{\"_id\":376217,\"key\":null,\"name\":\"Berlin\",\"fullName\":\"Berlin, Germany\",\"iata_airport_code\":null,\"itype\":\"location\",\"country\":\"Germany\",\"geo_position\":{\"latitude\":52.52437,\"longitude\":13.41053},\"locationId\":8384,\"inEurope\":true,\"countryCode\":\"DE\",\"coreCountry\":true,\"distance\":null}]"

    val sampleHttpResponse = HttpResponse(jsonBody,200,Map("Status" -> "OK"))

    val listProducer = new CitySuggestionListProducer()

    val infoBunch = listProducer.prepareInformation(sampleHttpResponse)

    assert(infoBunch.size == 1)
    assert(infoBunch.head._id == 376217)
    assert(infoBunch.head.key.isEmpty == true)
    assert(infoBunch.head.name.get == "Berlin")


  }

  test("correct extraction of Information from HttpResponse (malformed JSON)") {

    val jsonBody = "[{\"_id\":376217,key\":null,\"name\":\"Berlin\",\"fullName\":\"Berlin, Germany\",\"iata_airport_code\":null,\"itype\":\"location\",\"country\":\"Germany\",\"geo_position\":{\"latitude\":52.52437,\"longitude\":13.41053},\"locationId\":8384,\"inEurope\":true,\"countryCode\":\"DE\",\"coreCountry\":true,\"distance\":null}]"

    val sampleHttpResponse = HttpResponse(jsonBody,200,Map("Status" -> "OK"))

    val listProducer = new CitySuggestionListProducer()

    val infoBunch = listProducer.prepareInformation(sampleHttpResponse)

    assert(infoBunch.size == 1)
    assert(infoBunch.head._id == -3)
    assert(infoBunch.head.key.isEmpty == false)
    assert(infoBunch.head.name == None)
  }

  test("Storing values in CSV retrieved from HttpResponse (well-formed JSON)") {

    val jsonBody = "[{\"_id\":376217,\"key\":null,\"name\":\"Berlin\",\"fullName\":\"Berlin, Germany\",\"iata_airport_code\":null,\"type\":\"location\",\"country\":\"Germany\",\"geo_position\":{\"latitude\":52.52437,\"longitude\":13.41053},\"locationId\":8384,\"inEurope\":true,\"countryCode\":\"DE\",\"coreCountry\":true,\"distance\":null}]"

    val sampleHttpResponse = HttpResponse(jsonBody,200,Map("Status" -> "OK"))

    val listProducer = new CitySuggestionListProducer("./City-Suggestions.csv")

    val infoBunch = listProducer.prepareInformation(sampleHttpResponse)

    listProducer.saveAsCSV(infoBunch)

    val reader = CSVReader.open(new File(listProducer.csvFileName))

    val rows = reader.allWithHeaders

    assert(rows.size == 1)

    val firstRowWithColumnHeaders = rows.head

    assert(firstRowWithColumnHeaders("_id") == "376217")
    assert(firstRowWithColumnHeaders("name") == "Berlin")
    assert(firstRowWithColumnHeaders("type") == "location")
    assert(firstRowWithColumnHeaders("latitude") == "52.52437")
    assert(firstRowWithColumnHeaders("longitude") == "13.41053")

    reader.close

  }


}
