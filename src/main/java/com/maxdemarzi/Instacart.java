package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

@Path("/instacart")
public class Instacart {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/repeat_last_order")
    public Response recommendLastOrder( @Context GraphDatabaseService db) throws IOException {
        ArrayList<String> results = new ArrayList<>();
        try (Transaction tx = db.beginTx()) {

            ResourceIterator<Node> orders = db.findNodes(Labels.Order);
            while (orders.hasNext()) {

                Node order = orders.next();

                StringBuilder result = new StringBuilder();
                result.append(order.getProperty("id"));
                result.append(",");

                Relationship prevRel = order.getSingleRelationship(RelationshipTypes.PREV, Direction.OUTGOING);
                Node prevOrder = prevRel.getEndNode();
                for (Relationship r1: prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                    Node product = r1.getEndNode();
                    result.append(product.getProperty("id"));
                    result.append(" ");
                }
                results.add(result.toString());
            }

            tx.success();
        }
        

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    // TODO: 6/2/17 Do something smarter than simply last order 
//    @GET
//    @Path("/{order_id}")
//    public Response recommendProducts(@PathParam("order_id") final String orderId, @Context GraphDatabaseService db) throws IOException {
//        Map<String, Object> results;
//        try (Transaction tx = db.beginTx()) {
//            Node order = db.findNode(Labels.Order, "id", orderId);
//            Relationship ordered = order.getSingleRelationship(RelationshipTypes.ORDERED_BY, Direction.OUTGOING);
//            Node user = ordered.getEndNode();
//
//            ArrayList<Pair<Node, ArrayList<Node>>> orderedProducts = new ArrayList<>();
//            // Find previous orders and products
//            for (Relationship r1 :  user.getRelationships(Direction.INCOMING, RelationshipTypes.ORDERED_BY)) {
//                Node previousOrder = r1.getStartNode();
//                ArrayList<Node> products = new ArrayList<>();
//                for (Relationship r2: previousOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
//                    products.add(r2.getEndNode());
//                }
//                orderedProducts.add(Pair.of(previousOrder, products));
//            }
//            orderedProducts.sort(Comparator.comparing(m -> (Long) m.first().getProperty("number")));
//
//            for(int i = 0; i < orderedProducts.size() - 1; i++) {
//                if (orderedProducts.get(i).other().containsAll(orderedProducts.get(i+1).other())) {
//
//                }
//            }
//
//            for(Pair<Node, ArrayList<Node>> entry : orderedProducts) {
//
//            }
//
//
//            results = user.getAllProperties();
//            tx.success();
//        }
//        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
//    }
}
