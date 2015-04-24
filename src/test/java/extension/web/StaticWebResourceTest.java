package extension.web;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.Mute;
import org.neo4j.test.server.HTTP;

import static org.junit.Assert.*;

public class StaticWebResourceTest {

    @Rule
    public Mute mute = Mute.muteAll();

    @Rule
    public Neo4jRule server = new Neo4jRule().withExtension("/test", StaticWebResource.class);

    @Test
    public void testIndex() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/html").GET("test");

        assertEquals(200, response.status());
        assertEquals(true, response.rawContent().contains("<title>Test Index</title>"));
    }

    @Test
    public void testHtml() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/html").GET("test/test.html");

        assertEquals(200, response.status());
        assertEquals(true, response.rawContent().contains("<title>Test Page</title>"));
    }
    @Test
    public void testNotFound() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/html").GET("test/foo.html");

        assertEquals(404, response.status());
    }

    @Test
    public void testHtml2() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/html").GET("test/html/test.html");

        assertEquals(200, response.status());
        assertEquals(true, response.rawContent().contains("<title>Test Page HTML</title>"));
    }

    @Test
    public void testCss() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/css").GET("test/css/test.css");

        assertEquals(200, response.status());
        System.out.println(response.rawContent());
        assertEquals(true, response.rawContent().contains(".test {"));
    }

    @Test
    public void testJs() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "text/javascript").GET("test/js/test.js");

        assertEquals(200, response.status());
        System.out.println(response.rawContent());
        assertEquals(true, response.rawContent().contains("function test() {"));
    }
    @Test
    public void testImage() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "image/png").GET("test/img/node-blue.png");

        assertEquals(200, response.status());
        assertEquals("image/png", response.header("content-type"));
    }

    @Test
    public void testImageSubDirectory() throws Exception {
        HTTP.Response response =
                HTTP.withBaseUri(server.httpURI().toString()).withHeaders("accept", "image/png").GET("test/css/image/node-blue.png");

        assertEquals(200, response.status());
        assertEquals("image/png", response.header("content-type"));
    }
}
