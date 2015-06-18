package com.goEuro.nirmalya

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import org.json4s

import scala.util.{Success, Failure, Try}
import scalaj.http.{HttpResponse, Http}

import json4s._
import org.json4s.jackson.JsonMethods._
/**
 * Main driver of the application. The real work is done by {com.goEuro.nirmalya.CitySuggestionListProducer}
 * The driver creates an instance of it, passes the CITY name along and asks it to do the needful.
 * Once the retrieved records come back here, they are written to a CSV file.
 * @author nirmalya
 */
object App {


  def main(args : Array[String]) {

    implicit val formats = DefaultFormats

    val defaultTargetURL = "http://api.goeuro.com/api/v2/position/suggest/en"

    println( "Hello World!" )

    if (args.length < 1) {
      println("Insufficient arguments!")
      println("Usage: java -jar GoEuroTest.jar <CITY_NAME>")
      System.exit(-1)
    }

    val cityNameSupplied = args(0)

    println("Retrieving suggestions for < " + cityNameSupplied + " >")

    val listProducer = new CitySuggestionListProducer("./City-Suggestions.csv")

    val response = listProducer.seekResponse(defaultTargetURL,cityNameSupplied,new CityInfoProviderService())

    val infoRetrieved = listProducer.parseResponse(response)

    listProducer.saveAsCSV(infoRetrieved)

    println("[" + infoRetrieved.size + "] records written to ./City-Suggestions.csv")

  }

}
