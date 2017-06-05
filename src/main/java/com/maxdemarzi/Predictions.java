package com.maxdemarzi;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.*;
import java.util.stream.Collectors;

class Predictions {

    static ArrayList<Node> repeatLastOrder (Node order) {
        ArrayList<Node> products = new ArrayList<>();
        Relationship prevRel = order.getSingleRelationship(RelationshipTypes.PREV, Direction.OUTGOING);
        if (prevRel != null) {
            Node prevOrder = prevRel.getEndNode();
            for (Relationship r1 : prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                Node product = r1.getEndNode();
                products.add(product);
            }
        }
        return products;
    }

    static ArrayList<Node> alternatingOrder (Node order) {
        ArrayList<Node> products = new ArrayList<>();
        Relationship prevRel = order.getSingleRelationship(RelationshipTypes.PREV, Direction.OUTGOING);
        if (prevRel != null) {
            Node prevOrder = prevRel.getEndNode();
            prevRel = prevOrder.getSingleRelationship(RelationshipTypes.PREV, Direction.OUTGOING);
            if (prevRel != null) {
                prevOrder = prevRel.getEndNode();
                for (Relationship r1 : prevOrder.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                    Node product = r1.getEndNode();
                    products.add(product);
                }
            }
        }
        return products;
    }

    static ArrayList<Node> topReorderedBoostedByCartOrder(Node order) {
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
        return (ArrayList<Node>) topProducts;
    }

}
