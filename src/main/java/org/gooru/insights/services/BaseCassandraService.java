package org.gooru.insights.services;

import java.util.Collection;

import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.Rows;

public interface BaseCassandraService {

	OperationResult<ColumnList<String>> readColumns(String keyspace, String columnFamilyName, String rowKey,Collection<String> columns);

	OperationResult<Rows<String, String>> readAll(String keyspace, String columnFamilyName, Collection<String> keys, Collection<String> columns);

	OperationResult<Rows<String, String>> readAll(String keyspace, String columnFamily,Collection<String> columns);
}
