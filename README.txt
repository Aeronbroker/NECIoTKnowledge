Steps to use the Semantix Engine:

1- Download the latest version of Apache Fuseki 
2- Run the executable jar file (fuseki-server.jar) in the Fuseki directory.
3- Open browser and open the Fuseki page. (http://localhost:3030/)
4- Create a new dataset named "\Subscription"
5- Upload the file "SmartSantanderNGSI-RDF.owl" 
(included in \SemantixEngine\NGSI_Sparql_Examples folder)
6- (Optional) Open SystemParameters.txt file and set the configurations as you wish 
7- Clean and install Semantix Engine with Maven
8- Run it as a SpringBoot app (mvn spring-boot:run)
9- Check the ExampleQueries.txt file to try sample HTTP queries
