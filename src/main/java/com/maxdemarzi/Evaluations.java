package com.maxdemarzi;

import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Evaluations {

    static ArrayList<Node> intersection( List<Node> list1, List<Node> list2) {
        ArrayList<Node> intersection = new ArrayList<>(list1);
        intersection.retainAll(list2);
        return intersection;
    }

    static HashMap<String, Object> all( List<Node> lastOrdered, List<Node> predicted) {
        HashMap<String, Object> evaluations = new HashMap<>();
        ArrayList<Node> intersection = new ArrayList<>(lastOrdered);
        intersection.retainAll(predicted);

        Double precision = (double)intersection.size()/predicted.size();
        Double recall = (double) intersection.size()/lastOrdered.size();
        Double f1 = 2 * precision * recall / (precision + recall);

        evaluations.put("intersection", intersection);
        evaluations.put("precision", precision);
        evaluations.put("recall", recall);
        evaluations.put("f1", f1);

        return evaluations;
    }
}
