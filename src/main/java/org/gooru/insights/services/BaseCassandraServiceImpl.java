package org.gooru.insights.services;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.gooru.insights.constants.CassandraConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.query.AllRowsQuery;
import com.netflix.astyanax.query.ColumnFamilyQuery;
import com.netflix.astyanax.query.IndexQuery;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.query.RowSliceQuery;
import com.netflix.astyanax.retry.ConstantBackoff;
import com.netflix.astyanax.serializers.StringSerializer;

@Component
public class BaseCassandraServiceImpl implements BaseCassandraService,CassandraConstants{

	@Autowired
	BaseConnectionService connector;
	
	@Autowired
	BaseAPIService baseAPIService;

	 protected static final ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = ConsistencyLevel.CL_QUORUM;
	 
	
	public OperationResult<ColumnList<String>> readColumns(String keyspace, String columnFamilyName, String rowKey,Collection<String> columns) {

			Keyspace queryKeyspace = null;
			if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
				queryKeyspace = connector.connectInsights();
			} else {
				queryKeyspace = connector.connectSearch();
			}
			try {
				RowQuery<String, String> rowQuery = queryKeyspace.prepareQuery(this.accessColumnFamily(columnFamilyName)).setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).getKey(rowKey);
			
				if(baseAPIService.checkNull(columns)){
					rowQuery.withColumnSlice(columns);
				}
				return rowQuery.execute();
			} catch (ConnectionException e) {

				e.printStackTrace();
				System.out.println("Query execution exeption");
			}
			return null;

		}
	
	public OperationResult<Rows<String, String>> readAll(String keyspace, String columnFamilyName, Collection<String> keys, Collection<String> columns) {

		OperationResult<Rows<String, String>> queryResult = null;

		Keyspace queryKeyspace = null;
		if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
			queryKeyspace = connector.connectInsights();
		} else {
			queryKeyspace = connector.connectSearch();
		}
		try {
			RowSliceQuery<String, String> Query = queryKeyspace.prepareQuery(this.accessColumnFamily(columnFamilyName)).setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).getKeySlice(keys);
			if (!columns.isEmpty()) {
				Query.withColumnSlice(columns);
			}
			queryResult = Query.execute();

		} catch (ConnectionException e) {

			e.printStackTrace();
			System.out.println("Query execution exeption");
		}
		return queryResult;

	}
	
	public ColumnList<String> read(String keyspace,String cfName,String key){
		ColumnList<String> result = null;
		Keyspace queryKeyspace = null;
		if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
			queryKeyspace = connector.connectInsights();
		} else {
			queryKeyspace = connector.connectSearch();
		}
		try {
              result = queryKeyspace.prepareQuery(this.accessColumnFamily(cfName))
                    .setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).withRetryPolicy(new ConstantBackoff(2000, 5))
                    .getKey(key)
                    .execute()
                    .getResult()
                    ;

        } catch (ConnectionException e) {
        		e.printStackTrace();
        }
    	
    	return result;
	}

	public Column<String> readColumnValue(String keyspace,String cfName,String key,String columnName){
		ColumnList<String> result = null;
		Keyspace queryKeyspace = null;
		if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
			queryKeyspace = connector.connectInsights();
		} else {
			queryKeyspace = connector.connectSearch();
		}
		try {
              result = queryKeyspace.prepareQuery(this.accessColumnFamily(cfName))
                    .setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).withRetryPolicy(new ConstantBackoff(2000, 5))
                    .getKey(key)
                    .execute()
                    .getResult()
                    ;

        } catch (ConnectionException e) {
        		e.printStackTrace();
        }
    	if(!StringUtils.isBlank(columnName)){
    		return result.getColumnByName(columnName);
    	}
    	return null;
	}
    
	public void saveStringValue(String keyspace,String cfName, String key,String columnName,String value) {
	
		Keyspace queryKeyspace = null;
		
		if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
			queryKeyspace = connector.connectInsights();
		} else {
			queryKeyspace = connector.connectSearch();
		}
        MutationBatch m = queryKeyspace.prepareMutationBatch().setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).withRetryPolicy(new ConstantBackoff(2000, 5));

        m.withRow(this.accessColumnFamily(cfName), key).putColumnIfNotNull(columnName, value, null);

        try {
            m.execute();
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
    
	public OperationResult<Rows<String, String>> readAll(String keyspace, String columnFamily,Collection<String> columns) {
		OperationResult<Rows<String, String>> queryResult = null;
		AllRowsQuery<String, String> allRowQuery = null;
		Keyspace queryKeyspace = null;
		if (keyspaces.INSIGHTS.keyspace().equalsIgnoreCase(keyspace)) {
			queryKeyspace = connector.connectInsights();
		} else {
			queryKeyspace = connector.connectSearch();
		}
		try {

			allRowQuery  = queryKeyspace.prepareQuery(this.accessColumnFamily(columnFamily)).setConsistencyLevel(DEFAULT_CONSISTENCY_LEVEL).getAllRows();

			if (!columns.isEmpty()) {
				System.out.println("entered column fetcher ");
				allRowQuery.withColumnSlice(columns);
			}
			
			queryResult = allRowQuery.execute();

		} catch (ConnectionException e) {
			e.printStackTrace();
			System.out.println("Query execution exeption");
		}
		return queryResult;
	}
	
	public ColumnFamily<String, String> accessColumnFamily(String columnFamilyName) {

		ColumnFamily<String, String> columnFamily;

		columnFamily = new ColumnFamily<String, String>(columnFamilyName, StringSerializer.get(), StringSerializer.get());

		return columnFamily;
	}
	public static void main(String[] args){
		System.out.print("hgdhfg kjhKhxkjhvdk");
	}
}
