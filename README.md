This application retrieves a bunch of information about a city
from GoEuro's REST Service at "http://api.goeuro.com/api/v2/position/suggest/en"

To run, execute:
java -jar GoEuroTest.jar <CITY NAME>
(assuming that JAVA_HOME and CLASSPATH are set appropriately)

This is a Scala application. Hence, required Scala libraries are 
included in the JAR so as to obviate the need to install Scala
runtime separately, to run it.

The data returned from the GoEuro REST API, are stored in a file
named './City-Suggestions.csv'.

To shield users from various exceptions that can occur at various
stages of execution, the application is consciously designed
such that users only have to refer to the contents of the 
CSV file created, to find out if any error occurred and if so,
what.

The following are the values stored under the field '_id' in the
CSV file, for error conditions mentioned above:
*   -1 -> Fetch failed
*   -2 -> Some error occurred while HTTP messages were being exchanged
*   -3 -> The JSON data returned by GoEuro API could not be parsed
*   -4 -> The JSON data returned was empty
