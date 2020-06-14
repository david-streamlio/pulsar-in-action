package com.manning.pulsar.iiot.analytics.quantiles.sink;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.Sink;
import org.apache.pulsar.io.core.SinkContext;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import com.manning.pulsar.iiot.db.SensorQuantile;

public class QuantilesSink implements Sink<SensorQuantile> {
	
	private SinkContext ctx;
	
	private String connectionString;
	private MariaDbPoolDataSource pool;
	private String insertStmt;
	private long counter = 0;

	@Override
	public void close() throws Exception {
		if (pool != null) {
			pool.close();
		}
	}

	@Override
	public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
		ctx = sinkContext;
		Driver myDriver = new org.mariadb.jdbc.Driver();
		DriverManager.registerDriver( myDriver );
		
		insertStmt = new StringBuilder()
				.append("INSERT INTO sensor_synopsis (SensorId, SignalId, IntervalStart,"
						+ "IntervalEnd, Min, Max, RetainedItems, Quantile) VALUES (?,?,?,?,?,?,?,?) ")
				.toString();
		
		connectionString = new StringBuilder()
				.append("jdbc:mariadb://")
				.append(config.get("database.host"))
				.append(":")
				.append(config.get("database.port"))
				.append("/")
				.append(config.get("database.name"))
				.append("?user=")
				.append(config.get("database.user"))
				.append("&password=")
				.append(config.get("database.pass"))
				.toString();
		
//		getConnection();
	}

	@Override
	public void write(Record<SensorQuantile> record) throws Exception {
		SensorQuantile quant = record.getValue();
		
		Connection con = getConnection();
	
		try (PreparedStatement stmt = con.prepareStatement(insertStmt)) {
			stmt.setInt(1, quant.getSensorId());
			stmt.setString(2, quant.getSignalId().toString());
			stmt.setTimestamp(3, new Timestamp(quant.getIntervalStart()));
			stmt.setTimestamp(4, new Timestamp(quant.getIntervalEnd()));
			stmt.setDouble(5, quant.getMin());
			stmt.setDouble(6, quant.getMax());
			stmt.setLong(7, quant.getRetainedItems());
			stmt.setBlob(8, new SerialBlob(quant.getQuantile().array()));	

			if (stmt.executeUpdate() < 1 ) {
				record.fail();
			}
			
		} catch (final Exception ex) {
			ex.printStackTrace();
		} finally {
			con.close();
		}
		
		ctx.incrCounter("db.records.inserted", ++counter);
		
	}
	
	private Connection getConnection() throws SQLException {
		return getConnectionPool().getConnection();
	}

	private MariaDbPoolDataSource getConnectionPool() {
		if (pool == null) {
			pool = new MariaDbPoolDataSource(connectionString);
		}
		return pool;
	}
}
