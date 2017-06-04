package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/instacart")
public class Instacart {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/repeat_last_order")
    @Produces("text/plain")
    public Response repeatLastOrder( @Context GraphDatabaseService db) throws IOException {

        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("order_id,products\n");

            try (Transaction tx = db.beginTx()) {

                ResourceIterator<Node> orders = db.findNodes(Labels.Order);
                while (orders.hasNext()) {

                    Node order = orders.next();
                    if (order.getProperty("set").equals("test")) {
                        StringJoiner joiner = new StringJoiner(" ");

                        Relationship prevRel = order.getSingleRelationship(RelationshipTypes.PREV, Direction.OUTGOING);
                        if (prevRel != null) {
                            Node prevOrder = prevRel.getEndNode();
                            for (Relationship r1 : prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                                Node product = r1.getEndNode();
                                joiner.add(Long.toString((long) product.getProperty("id")));
                            }
                        }
                        writer.write( order.getProperty("id") + "," + joiner + "\n");
                    }
                }

                tx.success();
            }
            writer.flush();
        };

        return Response.ok(stream).build();
    }

    @GET
    @Path("/empty")
    @Produces("text/plain")
    public Response empty( @Context GraphDatabaseService db) throws IOException {

        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("order_id,products\n");

            try (Transaction tx = db.beginTx()) {

                ResourceIterator<Node> orders = db.findNodes(Labels.Order);
                while (orders.hasNext()) {

                    Node order = orders.next();
                    if (order.getProperty("set").equals("test")) {
                        StringJoiner joiner = new StringJoiner(" ");
                        // work

                        writer.write( order.getProperty("id") + "," + joiner + "\n");
                    }
                }
                tx.success();
            }
            writer.flush();
        };

        return Response.ok(stream).build();
    }

    @GET
    @Path("/top_reordered")
    @Produces("text/plain")
    public Response topReordered( @Context GraphDatabaseService db) throws IOException {

        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("order_id,products\n");

            try (Transaction tx = db.beginTx()) {

                ResourceIterator<Node> orders = db.findNodes(Labels.Order);
                while (orders.hasNext()) {

                    Node order = orders.next();
                    if (order.getProperty("set").equals("test")) {
                        StringJoiner joiner = new StringJoiner(" ");
                        ArrayList<Integer> sizes = new ArrayList<>();
                        HashMap<Node, Integer> products = new HashMap<>();
                        Relationship ordered = order.getSingleRelationship(RelationshipTypes.ORDERED_BY, Direction.OUTGOING);
                        Node user = ordered.getEndNode();
                        for (Relationship r1 : user.getRelationships(Direction.INCOMING, RelationshipTypes.ORDERED_BY)) {
                            Node prevOrder = r1.getStartNode();
                            sizes.add(prevOrder.getDegree(RelationshipTypes.HAS, Direction.OUTGOING));
                            for (Relationship r2 : prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                                Node product = r2.getEndNode();
                                Integer count = 1;
                                if (products.containsKey(product)) {
                                     count = 1 + products.get(product);
                                }
                                products.put(product, count);
                            }
                        }
                        Long average = Math.round(sizes.stream().mapToInt(i -> i).average().orElse(0));
                        List<Node> topProducts = products.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .limit(average)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                        for (Node product : topProducts) {
                            joiner.add( Long.toString((long)product.getProperty("id")));
                        }

                        writer.write( order.getProperty("id") + "," + joiner + "\n");
                    }
                }
                tx.success();
            }
            writer.flush();
        };

        return Response.ok(stream).build();
    }

    @GET
    @Path("/top_reordered_boosted_by_cart_order")
    @Produces("text/plain")
    public Response topReorderedBoostedByCartOrder( @Context GraphDatabaseService db) throws IOException {
        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write("order_id,products\n");

            try (Transaction tx = db.beginTx()) {

                ResourceIterator<Node> orders = db.findNodes(Labels.Order);
                while (orders.hasNext()) {

                    Node order = orders.next();
                    if (order.getProperty("set").equals("test")) {
                        StringJoiner joiner = new StringJoiner(" ");
                        ArrayList<Integer> sizes = new ArrayList<>();
                        HashMap<Node, Double> products = new HashMap<>();
                        Relationship ordered = order.getSingleRelationship(RelationshipTypes.ORDERED_BY, Direction.OUTGOING);
                        Node user = ordered.getEndNode();
                        for (Relationship r1 : user.getRelationships(Direction.INCOMING, RelationshipTypes.ORDERED_BY)) {
                            Node prevOrder = r1.getStartNode();
                            sizes.add(prevOrder.getDegree(RelationshipTypes.HAS, Direction.OUTGOING));
                            for (Relationship r2 : prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                                Node product = r2.getEndNode();
                                Double count = 1.0;
                                Double boost = 0.0;
                                // Products added to the cart earlier are more likely to be re-ordered, give them a boost
                                switch (String.valueOf(r2.getProperty("order",99))){
                                    case "1" : boost = 0.3; break;
                                    case "2" : boost = 0.2; break;
                                    case "3" : boost = 0.1; break;
                                }
                                if (products.containsKey(product)) {
                                    count = 1.0 + products.get(product);
                                }
                                products.put(product, count + boost);
                            }
                        }
                        Long average = Math.round(sizes.stream().mapToInt(i -> i).average().orElse(0));
                        List<Node> topProducts = products.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .limit(average)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                        for (Node product : topProducts) {
                            joiner.add( Long.toString((long)product.getProperty("id")));
                        }

                        writer.write( order.getProperty("id") + "," + joiner + "\n");
                    }
                }
                tx.success();
            }
            writer.flush();
        };

        return Response.ok(stream).build();
    }

}
