/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.transaction.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.distsql.statement.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowTransactionRuleExecutorTest {
    
    @Test
    void assertExecuteQueryWithXA() throws SQLException {
        TransactionRule rule = new TransactionRule(new TransactionRuleConfiguration(TransactionType.XA.name(),
                "Atomikos", PropertiesBuilder.build(new Property("host", "127.0.0.1"), new Property("databaseName", "jbossts"))), Collections.emptyMap());
        ContextManager contextManager = mockContextManager(rule);
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(new ShowTransactionRuleStatement(), null, contextManager, mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getCell(1), is(TransactionType.XA.name()));
        assertThat(actual.get(0).getCell(2), is("Atomikos"));
        String props = (String) actual.get(0).getCell(3);
        assertTrue(props.contains("\"databaseName\":\"jbossts\""));
        assertTrue(props.contains("\"host\":\"127.0.0.1\""));
    }
    
    @Test
    void assertExecuteQueryWithLocal() throws SQLException {
        TransactionRule rule = new TransactionRule(new TransactionRuleConfiguration(TransactionType.LOCAL.name(), null, new Properties()), Collections.emptyMap());
        ContextManager contextManager = mockContextManager(rule);
        DistSQLQueryExecuteEngine engine = new DistSQLQueryExecuteEngine(new ShowTransactionRuleStatement(), null, contextManager, mock(DistSQLConnectionContext.class));
        engine.executeQuery();
        List<LocalDataQueryResultRow> actual = new ArrayList<>(engine.getRows());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getCell(1), is(TransactionType.LOCAL.name()));
        assertThat(actual.get(0).getCell(2), is(""));
        assertThat(actual.get(0).getCell(3), is(""));
    }
    
    private ContextManager mockContextManager(final TransactionRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
