package extension.web;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.Mute;
import org.neo4j.test.server.HTTP;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;

public class PopotoResourceTest {


    public static final String PERSON_NAME_INDEX = "create index on :Person(name)";
    @Rule
    public Mute mute = Mute.muteAll();

    @Rule
    public Neo4jRule server = new Neo4jRule()
//            .withFixture(PERSON_NAME_INDEX)
//            .withFixture("create constraint on (m:Movie) assert m.title is unique")
            .withFixture("create (:Movie {title:'Matrix'})<-[:ACTS_IN]-(:Person {born:1964,name:'Keanu Reeves'})")
            .withExtension("/test", StaticWebResource.class.getPackage().getName());

    @Test
    public void testConfig() throws Exception {
        HTTP.Response fixtureResponse = HTTP.POST(server.httpsURI().resolve("/db/data/transaction/commit").toString(), singletonMap("statements", asList(singletonMap("statement", PERSON_NAME_INDEX))));
        assertEquals(200, fixtureResponse.status());

        String baseUri = server.httpURI().resolve("test/config/").toString();
        HTTP.Response response =
                HTTP.withBaseUri(baseUri).withHeaders("accept", "text/javascript").GET("config.js");

        assertEquals(200, response.status());
        String content = response.rawContent();
        System.err.println(content);
        assertEquals(true, content.contains("popoto.rest.CYPHER_URL = 'http://localhost:"));
        assertEquals(true, content.contains("/db/data/transaction/commit';"));
        assertEquals(true, content.contains("\"Movie\":"));
        assertEquals(true, content.contains("\"constraintAttribute\":\"name\""));
        assertEquals(true, content.contains("\"resultOrderByAttribute\":\"name\""));
        assertEquals(true, content.contains("\"title\""));
        assertEquals(true, content.contains("\"name\",\"born\""));
    }


}
