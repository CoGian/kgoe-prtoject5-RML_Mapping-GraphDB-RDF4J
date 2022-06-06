import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;

public class GraphDBExample {

    /**
     * How to connect to a graphdb repository, load a file to a repository, add some data through the API and perform a
     * sparql query
     */
    public static void main(String[] args) {

        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/MyIMDb");
        RepositoryConnection connection = repository.getConnection();

        connection.begin();

        String queryString1 = """
                PREFIX ex: <http://www.semanticweb.org/kgiantsios/ontologies/2022/5/imdb#>\s
                SELECT ?title ?total
                WHERE {\s
                    ?movie a ex:Movie;
                    		ex:primaryTitle ?title.
                	?rating ex:ratingFor ?movie.
                	?rating ex:total ?total
                }
                ORDER BY DESC(?total)
                LIMIT 1 """ ;

        TupleQuery query1 = connection.prepareTupleQuery(queryString1);

        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
        try (TupleQueryResult result = query1.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // ... and print out the value of the variable binding for ?s and ?n
                System.out.println("Movie with the most ratings: " + solution.getValue("title"));
                System.out.println("Total movie ratings: " + solution.getValue("total"));
            }
        }

        System.out.println("=================");

        String queryString2 = """
                PREFIX ex: <http://www.semanticweb.org/kgiantsios/ontologies/2022/5/imdb#>\s
                SELECT ?name (AVG(?average) AS ?avg)\s
                WHERE {\s
                    ?actor a ex:Actor;
                           ex:primaryName ?name;
                           ex:knownForTitles ?movie.
                    ?rating ex:ratingFor ?movie;
                            ex:average ?average.
                }
                GROUP BY ?name
                ORDER BY DESC(?avg)
                LIMIT 1 """ ;

        TupleQuery query2 = connection.prepareTupleQuery(queryString2);


        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
        try (TupleQueryResult result = query2.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // ... and print out the value of the variable binding for ?s and ?n
                System.out.println("Actor with the best avg rating: " + solution.getValue("name"));
                System.out.println("Avg ratings: " + solution.getValue("avg"));
            }
        }

        System.out.println("=================");

        String queryString3 = """
                PREFIX ex: <http://www.semanticweb.org/kgiantsios/ontologies/2022/5/imdb#>\s
                SELECT ?name (AVG(?runtimeMinutes) AS ?avgRuntimeMinutes)
                WHERE {\s
                    ?actor a ex:Actor;
                           ex:primaryName ?name;
                           ex:knownForTitles ?movie.
                    ?movie ex:runtimeMinutes ?runtimeMinutes
                }
                GROUP BY ?name""" ;

        TupleQuery query3 = connection.prepareTupleQuery(queryString3);


        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
        try (TupleQueryResult result = query3.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // ... and print out the value of the variable binding for ?s and ?n
                System.out.println("Actor: " + solution.getValue("name")
                        + ", Avg movie runtime: " + solution.getValue("avgRuntimeMinutes"));
            }
        }

        connection.close();
        repository.shutDown();
    }
}
