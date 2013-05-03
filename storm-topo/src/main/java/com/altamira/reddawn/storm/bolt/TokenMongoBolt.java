package com.altamira.reddawn.storm.bolt;

import java.util.Collection;
import java.util.Date;

import storm.contrib.mongo.MongoBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * Needed a bolt to handle the collection of tokens for context purposes. Wrote
 * this one real fast. It technically could be used for any collection (that has
 * objects that have meaningful toString() methods) In my opinion, we should
 * re-factor SimpleMongoBolt from the storm-contrib-mongo project to handle
 * collections of Mongo-valid db types
 */
public class TokenMongoBolt extends MongoBolt {

	protected TokenMongoBolt(String mongoHost, int mongoPort, String mongoDbName) {
		super(mongoHost, mongoPort, mongoDbName);
	}

	public TokenMongoBolt(String mongoHost, int mongoPort, String mongoDbName,
			String collectionName) {
		this(mongoHost, mongoPort, mongoDbName);
		this.collectionName = collectionName;
	}

	private static final long serialVersionUID = 1L;
	private String collectionName;

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public DBObject getDBObjectForInput(Tuple input) {
		BasicDBObjectBuilder dbObjectBuilder = new BasicDBObjectBuilder();

		for (String field : input.getFields()) {
			Object value = input.getValueByField(field);
			if (isPrimitive(value)) {
				dbObjectBuilder.append(field, value);
			} else if (value instanceof Collection) {
				BasicDBList array = new BasicDBList();
				for (Object collVal : (Collection) value) {
					array.add(collVal.toString());
				}
				dbObjectBuilder.add(field, array);
			}
		}

		return dbObjectBuilder.get();
	}

	private boolean isPrimitive(Object value) {
		return value instanceof String || value instanceof Date
				|| value instanceof Integer || value instanceof Float
				|| value instanceof Double || value instanceof Short
				|| value instanceof Long || value instanceof DBObject;
	}

	@Override
	public String getMongoCollectionForInput(Tuple input) {
		return collectionName;
	}

	@Override
	public boolean shouldActOnInput(Tuple input) {
		return true;
	}

}
