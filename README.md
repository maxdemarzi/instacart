# Instacart
Instacart Market Basket Analysis


Import
--

Create Constraints:

        CREATE CONSTRAINT ON (n:Aisle) ASSERT n.id IS UNIQUE;
        CREATE CONSTRAINT ON (n:Aisle) ASSERT n.name IS UNIQUE;
        CREATE CONSTRAINT ON (n:Department) ASSERT n.id IS UNIQUE;
        CREATE CONSTRAINT ON (n:Department) ASSERT n.name IS UNIQUE;                
        CREATE CONSTRAINT ON (n:Product) ASSERT n.id IS UNIQUE;
        CREATE CONSTRAINT ON (n:Product) ASSERT n.name IS UNIQUE;  
        CREATE CONSTRAINT ON (n:User) ASSERT n.id IS UNIQUE;  
        CREATE CONSTRAINT ON (n:Order) ASSERT n.id IS UNIQUE;   
                 
        CREATE INDEX ON :Order(user_id, number)
        

Download files from https://www.kaggle.com/c/instacart-market-basket-analysis/data
The small files are already included in this repository unzipped.

Unzip the last two and copy all the CSV files in /import to <neo4j directory>/import        

Clean data:
        
        Replace: \"" with \" in the products.csv file.
        
Load Data:        

        LOAD CSV WITH HEADERS FROM 'file:///aisles.csv' AS csvLine
        CREATE (a:Aisle {id: toInteger(csvLine.aisle_id), name:csvLine.aisle});
        
        LOAD CSV WITH HEADERS FROM 'file:///departments.csv' AS csvLine
        CREATE (d:Department {id: toInteger(csvLine.department_id), name:csvLine.department});
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS csvLine
        CREATE (p:Product {id: toInteger(csvLine.product_id), name:csvLine.product_name});
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS csvLine
        MATCH  (p:Product {id: toInteger(csvLine.product_id)}), (a:Aisle {id: toInteger(csvLine.aisle_id)})
        CREATE (p)-[:IN_AISLE]->(a)
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///products.csv' AS csvLine
        MATCH  (d:Department {id: toInteger(csvLine.department_id)}), (a:Aisle {id: toInteger(csvLine.aisle_id)})
        MERGE (a)-[:IN_DEPARTMENT]->(d)
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///orders.csv' AS csvLine
        MERGE (u:User {id: toInteger(csvLine.user_id)});

        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///orders.csv' AS csvLine
        CREATE (o:Order {id: toInteger(csvLine.order_id), set: csvLine.eval_set, user_id: toInteger(csvLine.user_id), number: toInteger(csvLine.order_number),
                dow: toInteger(csvLine.order_dow), hour: toInteger(csvLine.order_hour_of_day) });
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///orders.csv' AS csvLine
        MATCH  (u:User {id: toInteger(csvLine.user_id)}), (o:Order {id: toInteger(csvLine.order_id)})
        CREATE (u)<-[:ORDERED_BY]-(o)
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///orders.csv' AS csvLine
        MATCH  (o:Order {id: toInteger(csvLine.order_id)}), (po:Order {user_id: toInteger(csvLine.user_id), number: toInteger(csvLine.order_number) - 1})
        CREATE (o)-[:PREV]->(po)
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///order_products__prior.csv' AS csvLine
        MATCH  (o:Order {id: toInteger(csvLine.order_id)}), (p:Product {id: toInteger(csvLine.product_id)})
        CREATE (o)-[:HAS {order: toInteger(csvLine.add_to_cart_order), reordered: toBoolean(csvLine.reordered)} ]->(p)
        
        USING PERIODIC COMMIT
        LOAD CSV WITH HEADERS FROM 'file:///order_products__train.csv' AS csvLine
        MATCH  (o:Order {id: toInteger(csvLine.order_id)}), (p:Product {id: toInteger(csvLine.product_id)})
        CREATE (o)-[:HAS {order: toInteger(csvLine.add_to_cart_order), reordered: toBoolean(csvLine.reordered)} ]->(p)
