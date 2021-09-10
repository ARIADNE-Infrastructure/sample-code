package ariadneplus.sample;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This a very simple example on how to connect to the ARIADNEplus GraphDB.
 *
 * @author Enrico Ottonello
 * @author Alessia Bardi
 */
public class GraphDBReader {

    private static String graphDBUrl = "https://graphdb-test.ariadne.d4science.org";
    private static String graphDBRepository = "ariadneplus-ts01";

    private static RepositoryConnection connection;
    private static RemoteRepositoryManager manager;
    private static Repository repository;

    private static String queryProvenanceGraph = "SELECT ?s ?p ?o WHERE {GRAPH <https://ariadne-infrastructure.eu/datasourceApis> {?s ?p ?o}}";

    //For CONSTRUCT QUERY
    private static ClassPathResource queryTemplateResource = new ClassPathResource("ariadneplus/sample/templates/read_collection_data_template.sparql");
    private static String resourceIRI = "https://ariadne-infrastructure.eu/aocat/Collection/ADS/AAA81A6D-56F3-341C-BAF0-791C31BC7F73";
    private static String datasource = "ads";
    private static String collectionId = "398";

   public static void main(String[] args) throws Exception {
       try {
           System.out.println("Sample class  -  client for the ARIADNEplus GraphDB");
           openConnection();
           System.out.println("Connection opened");
           System.out.println("Executing tuple query");
           //change it to your preferred serialization format or iterate over the statements to do something
           connection.prepareTupleQuery(queryProvenanceGraph).evaluate(new SPARQLResultsJSONWriter(System.out));
           System.out.println("Executing Construct query");
           executeConstructQuery();
       }
       finally{
           closeConnection();
           System.out.println("Connection closed");
       }
   }

    private static void openConnection() {
        manager = new RemoteRepositoryManager(graphDBUrl);
        manager.init();
        manager.setUsernameAndPassword(null, null);
        repository = manager.getRepository(graphDBRepository);
        connection =  repository.getConnection();
    }

    private static void executeConstructQuery() throws Exception {
        GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, prepareConstructQueryFromTemplate());
        GraphQueryResult graphQueryResult = graphQuery.evaluate();
        Model resultsModel = QueryResults.asModel(graphQueryResult);
        graphQueryResult.close();
        System.out.println(resultsModel.size());
        StringWriter recordWriter = new StringWriter();
        //change it to your preferred serialization format (and include the proper RIO package among the dependency)
        RDFWriter rdfRecordWriter = Rio.createWriter(RDFFormat.RDFXML, recordWriter);
        Rio.write(resultsModel, rdfRecordWriter);
        System.out.println(recordWriter.toString());
    }

    private static String prepareConstructQueryFromTemplate() throws IOException {
        String queryTemplate = IOUtils.toString(queryTemplateResource.getInputStream(), StandardCharsets.UTF_8.name());
        final String selectQueryTemplate = queryTemplate.replaceAll("%datasource", datasource).replaceAll("%collectionId", collectionId);
        return selectQueryTemplate.replaceAll("%record", "<"+resourceIRI+">");
    }


    private static void closeConnection(){
        connection.close();
        repository.shutDown();
        manager.shutDown();
    }
}
