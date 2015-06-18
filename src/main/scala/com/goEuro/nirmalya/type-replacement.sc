import com.goEuro.nirmalya.App.{Information, geo_position}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._

//case class geo_position(latitude: Double, longitude: Double)
//case class Information(_id: Int,key: Option[String],name: String,fullName: String,iata_airport_code: String,iType: String,country: String, geo_position: geo_position, locationId: Option[Int], inEurope: Boolean, countryCode: String,coreCountry: Boolean,distance: Option[String])
implicit val formats = DefaultFormats

val v = "[{\"_id\":376217,\"key\":null,\"name\":\"Berlin\",\"fullName\":\"Berlin, Germany\",\"iata_airport_code\":null,\"type\":\"location\",\"country\":\"Germany\",\"geo_position\":{\"latitude\":52.52437,\"longitude\":13.41053},\"locationId\":8384,\"inEurope\":true,\"countryCode\":\"DE\",\"coreCountry\":true,\"distance\":null}]"
val parsedBody = parse(v.replaceAll(",\"type\":",",\"iType\":"))

val i = parsedBody.extract[List[Information]]
