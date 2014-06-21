/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.query.sql.mssql;

import java.sql.Connection;

import javax.inject.Provider;

import com.mysema.query.sql.AbstractSQLQueryFactory;
import com.mysema.query.sql.Configuration;
import com.mysema.query.sql.SQLServerTemplates;
import com.mysema.query.sql.SQLTemplates;

/**
 * SQL Server specific implementation of SQLQueryFactory
 *
 * @author tiwe
 *
 */
public class SQLServerQueryFactory extends AbstractSQLQueryFactory<SQLServerQuery, SQLServerSubQuery> {

    public SQLServerQueryFactory(Configuration configuration, Provider<Connection> connection) {
        super(configuration, connection);
    }

    public SQLServerQueryFactory(Provider<Connection> connection) {
        this(new Configuration(new SQLServerTemplates()), connection);
    }

    public SQLServerQueryFactory(SQLTemplates templates, Provider<Connection> connection) {
        this(new Configuration(templates), connection);
    }

    public SQLServerQuery query() {
        return new SQLServerQuery(connection.get(), configuration);
    }
    
    @Override
    public SQLServerSubQuery subQuery() {
        return new SQLServerSubQuery();
    }

}
