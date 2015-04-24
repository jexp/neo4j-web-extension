package extension.web;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Path("/config")
public class PopotoResource {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Path("/config.js")
    @Produces("text/javascript")
    @GET
    public String config(@Context GraphDatabaseService gds, @Context UriInfo uriInfo) throws IOException {

        Map<String,Map> map = metaData(gds);
        String firstLabel = map.keySet().iterator().next();
        return "popoto.rest.CYPHER_URL = '"+uriInfo.getBaseUriBuilder().replacePath("/db/data/transaction/commit").build()+"';\n" +
               "popoto.provider.nodeProviders = "+ OBJECT_MAPPER.writeValueAsString(map) +";\n"+
               "popoto.provider.startNodeLabel = '"+ firstLabel +"'";
    }

    private Map<String,Map> metaData(GraphDatabaseService gds) throws IOException {
        Map<String, Map> map = new LinkedHashMap<>();
        try (Transaction tx = gds.beginTx()) {
            Schema schema = gds.schema();
            ResourceIterable<Label> labels = GlobalGraphOperations.at(gds).getAllLabels();
            for (Label label : labels) {
                Map nodeMetaData = new HashMap();
                Collection<String> properties = new LinkedHashSet<>();
                for (IndexDefinition index : schema.getIndexes(label)) {
                    if (index.isConstraintIndex()) continue;
                    if (!nodeMetaData.containsKey("constraintAttribute")) {
                        nodeMetaData.put("constraintAttribute", index.getPropertyKeys().iterator().next());
                    }
                    for (String s : index.getPropertyKeys()) {
                        properties.add(s);
                    }
                }
                for (ConstraintDefinition constraint : schema.getConstraints(label)) {
                    if (!nodeMetaData.containsKey("constraintAttribute")) {
                        nodeMetaData.put("constraintAttribute", constraint.getPropertyKeys().iterator().next());
                    }
                    for (String s : constraint.getPropertyKeys()) {
                        properties.add(s);
                    }
                }
                ResourceIterator<Node> nodes = gds.findNodes(label);
                if (nodes.hasNext()) {
                    Node node = nodes.next();
                    for (String property : node.getPropertyKeys()) {
                        if (!nodeMetaData.containsKey("constraintAttribute")) {
                            nodeMetaData.put("constraintAttribute", property);
                        }
                        properties.add(property);
                    }
                }
                nodes.close();
                nodeMetaData.put("returnAttributes", properties);
                map.put(label.name(), nodeMetaData);
            }
            tx.success();
        }
        return map;
    }
    /*
    popoto.rest.CYPHER_URL = popoto.rest.CYPHER_URL || "http://localhost:7474/db/data/transaction/commit";

    popoto.provider.nodeProviders = popoto.provider.nodeProviders || {
        "Person": {
            "returnAttributes": ["name", "born"],
            "constraintAttribute": "name"
        },
        "Movie": {
            "returnAttributes": ["title", "released", "tagline"],
            "constraintAttribute": "title"
        }
    };

     */
}
