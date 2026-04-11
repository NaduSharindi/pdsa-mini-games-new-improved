package com.nibm.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.nibm.config.AppConfig;

public class MongoDBConnection {

    private static final String URI = AppConfig.get("mongo.uri");
    private static final String DB_NAME = AppConfig.get("mongo.db");

    private static MongoClient client;
    private static MongoDatabase database;

    // Private constructor — no instantiation
    private MongoDBConnection() {}

    public static MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(URI);
            database = client.getDatabase(DB_NAME);
        }
        return database;
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}