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
package com.mysema.query.sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryException;
import com.mysema.query.QueryFlag;
import com.mysema.query.QueryMetadata;
import com.mysema.query.QueryModifiers;
import com.mysema.query.SearchResults;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.ParamNotSetException;
import com.mysema.query.types.Path;
import com.mysema.util.ResultSetAdapter;

/**
 * AbstractSQLQuery is the base type for SQL query implementations
 *
 * @author tiwe
 *
 * @param <Q> concrete subtype
 */
public abstract class AbstractSQLQuery<Q extends AbstractSQLQuery<Q>> extends ProjectableSQLQuery<Q> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSQLQuery.class);

    private static final QueryFlag rowCountFlag = new QueryFlag(QueryFlag.Position.AFTER_PROJECTION, ", count(*) over() ");

    @Nullable
    private final Connection conn;

    protected SQLListeners listeners;

    protected boolean useLiterals;

    private boolean getLastCell;

    private Object lastCell;

    public AbstractSQLQuery(@Nullable Connection conn, Configuration configuration) {
        this(conn, configuration, new DefaultQueryMetadata().noValidate());
    }
 
    public AbstractSQLQuery(@Nullable Connection conn, Configuration configuration, QueryMetadata metadata) {
        super(new QueryMixin<Q>(metadata, false), configuration);
        this.conn = conn;
        this.listeners = new SQLListeners(configuration.getListeners());
        this.useLiterals = configuration.getUseLiterals();
    }

    /**
     * @param listener
     */
    public void addListener(SQLListener listener) {
        listeners.add(listener);
    }

    @Override
    public long count() {
        try {
            return unsafeCount();
        } catch (SQLException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw configuration.translate(e);
        }
    }

    /**
     * If you use forUpdate() with a backend that uses page or row locks, rows examined by the
     * query are write-locked until the end of the current transaction.
     *
     * Not supported for SQLite and CUBRID
     *
     * @return
     */
    public Q forUpdate() {
        return addFlag(SQLOps.FOR_UPDATE_FLAG);
    }

    protected SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    @Nullable
    private <T> T get(ResultSet rs, Expression<?> expr, int i, Class<T> type) throws SQLException {
        return configuration.get(rs, expr instanceof Path ? (Path<?>)expr : null, i, type);
    }

    private void set(PreparedStatement stmt, Path<?> path, int i, Object value) throws SQLException{
        configuration.set(stmt, path, i, value);
    }

    /**
     * Get the results as an JDBC result set
     *
     * @param args
     * @return
     */
    public ResultSet getResults(Expression<?>... exprs) {
        queryMixin.addProjection(exprs);
        SQLSerializer serializer = serialize(false);
        String queryString = serializer.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("query : {}", queryString);
        }
        listeners.notifyQuery(queryMixin.getMetadata());

        List<Object> constants = serializer.getConstants();
        try {
            final PreparedStatement stmt = conn.prepareStatement(queryString);
            setParameters(stmt, constants, serializer.getConstantPaths(), getMetadata().getParams());
            final ResultSet rs = stmt.executeQuery();

            return new ResultSetAdapter(rs) {
                @Override
                public void close() throws SQLException {
                    try {
                        super.close();
                    } finally {
                        stmt.close();
                    }
                }
            };
        } catch (SQLException e) {
            throw configuration.translate(queryString, constants, e);
        } finally {
            reset();
        }
    }

    protected Configuration getConfiguration() {
        return configuration;
    }
    
    @Override
    public <RT> CloseableIterator<RT> iterate(Expression<RT> expr) {
        expr = queryMixin.addProjection(expr);
        return iterateSingle(queryMixin.getMetadata(), expr);
    }

    @SuppressWarnings("unchecked")
    private <RT> CloseableIterator<RT> iterateSingle(QueryMetadata metadata, @Nullable final Expression<RT> expr) {
        SQLSerializer serializer = serialize(false);
        final String queryString = serializer.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("query : {}", queryString);
        }
        listeners.notifyQuery(queryMixin.getMetadata());
        List<Object> constants = serializer.getConstants();
        try {
            final PreparedStatement stmt = conn.prepareStatement(queryString);
            setParameters(stmt, constants, serializer.getConstantPaths(), metadata.getParams());
            final ResultSet rs = stmt.executeQuery();

            if (expr == null) {
                return new SQLResultIterator<RT>(configuration, stmt, rs) {
                    @Override
                    public RT produceNext(ResultSet rs) throws Exception {
                        return (RT) rs.getObject(1);
                    }
                };
            } else if (expr instanceof FactoryExpression) {
                return new SQLResultIterator<RT>(configuration, stmt, rs) {
                    @Override
                    public RT produceNext(ResultSet rs) throws Exception {
                        return newInstance((FactoryExpression<RT>) expr, rs, 0);
                    }
                };
            } else if (expr.getType().isArray()) {
                return new SQLResultIterator<RT>(configuration, stmt, rs) {
                    @Override
                    public RT produceNext(ResultSet rs) throws Exception {
                        Object[] rv = new Object[rs.getMetaData().getColumnCount()];
                        for (int i = 0; i < rv.length; i++) {
                            rv[i] = rs.getObject(i+1);
                        }
                        return (RT) rv;
                    }
                };
            } else {
                return new SQLResultIterator<RT>(configuration, stmt, rs) {
                    @Override
                    public RT produceNext(ResultSet rs) throws Exception {
                        return get(rs, expr, 1, expr.getType());
                    }
                };
            }

        } catch (SQLException e) {
            throw configuration.translate(queryString, constants, e);
        } finally {
            reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <RT> List<RT> list(Expression<RT> expr) {
        expr = queryMixin.addProjection(expr);
        SQLSerializer serializer = serialize(false);
        final String queryString = serializer.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("query : {}", queryString);
        }
        listeners.notifyQuery(queryMixin.getMetadata());
        List<Object> constants = serializer.getConstants();
        try {
            final PreparedStatement stmt = conn.prepareStatement(queryString);
            try {
                setParameters(stmt, constants, serializer.getConstantPaths(), queryMixin.getMetadata().getParams());
                final ResultSet rs = stmt.executeQuery();
                try {
                    lastCell = null;
                    final List<RT> rv = new ArrayList<RT>();
                    if (expr instanceof FactoryExpression) {
                        FactoryExpression<RT> fe = (FactoryExpression<RT>)expr;
                        while (rs.next()) {
                            if (getLastCell) {
                                lastCell = rs.getObject(fe.getArgs().size() + 1);
                                getLastCell = false;
                            }
                            rv.add(newInstance(fe, rs, 0));
                        }
                    }  else if (expr.getType().isArray()) {
                        while (rs.next()) {
                            Object[] row = new Object[rs.getMetaData().getColumnCount()];
                            if (getLastCell) {
                                lastCell = rs.getObject(row.length);
                                getLastCell = false;
                            }
                            for (int i = 0; i < row.length; i++) {
                                row[i] = rs.getObject(i+1);
                            }
                            rv.add((RT)row);
                        }
                    } else {
                        while (rs.next()) {
                            if (getLastCell) {
                                lastCell = rs.getObject(2);
                                getLastCell = false;
                            }
                            rv.add(get(rs, expr, 1, expr.getType()));
                        }
                    }
                    return rv;
                } catch (IllegalAccessException e) {
                    throw new QueryException(e);
                } catch (InvocationTargetException e) {
                    throw new QueryException(e);
                } catch (InstantiationException e) {
                    throw new QueryException(e);
                } catch (SQLException e) {
                    throw configuration.translate(queryString, constants, e);
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException e) {
            throw configuration.translate(queryString, constants, e);
        } finally {
            reset();
        }
    }

    @Override
    public <RT> SearchResults<RT> listResults(Expression<RT> expr) {
        try {
            if (configuration.getTemplates().isCountViaAnalytics()) {
                List<RT> results;
                try {
                    queryMixin.addFlag(rowCountFlag);
                    getLastCell = true;
                    results = list(expr);
                } finally {
                    queryMixin.removeFlag(rowCountFlag);
                }
                long total;
                if (!results.isEmpty()) {
                    if (lastCell instanceof Number) {
                        total = ((Number)lastCell).longValue();
                    } else {
                        throw new IllegalStateException("Unsupported lastCell instance " + lastCell);
                    }
                } else {
                    total = count();
                }
                QueryModifiers modifiers = queryMixin.getMetadata().getModifiers();
                return new SearchResults<RT>(results, modifiers, total);

            } else {
                queryMixin.addProjection(expr);
                long total = count();
                if (total > 0) {
                    queryMixin.getMetadata().clearProjection();
                    QueryModifiers modifiers = queryMixin.getMetadata().getModifiers();
                    return new SearchResults<RT>(list(expr), modifiers, total);
                } else {
                    return SearchResults.emptyResults();
                }
            }

        } finally {
            getLastCell = false;
            reset();
        }
    }

    private <RT> RT newInstance(FactoryExpression<RT> c, ResultSet rs, int offset)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException{
        Object[] args = new Object[c.getArgs().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = get(rs, c.getArgs().get(i), offset + i + 1, c.getArgs().get(i).getType());
        }
        return c.newInstance(args);
    }

    private void reset() {
        queryMixin.getMetadata().reset();
    }

    protected void setParameters(PreparedStatement stmt, List<?> objects, List<Path<?>> constantPaths,
            Map<ParamExpression<?>, ?> params) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() +
                    " paths, but got " + constantPaths.size());
        }
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            try {
                if (o instanceof ParamExpression) {
                    if (!params.containsKey(o)) {
                        throw new ParamNotSetException((ParamExpression<?>) o);
                    }
                    o = params.get(o);
                }
                set(stmt, constantPaths.get(i), i+1, o);
            } catch (SQLException e) {
                throw configuration.translate(e);
            }
        }
    }

    @Override
    public <RT> RT uniqueResult(Expression<RT> expr) {
        if (getMetadata().getModifiers().getLimit() == null
           && !expr.toString().contains("count(")) {
            limit(2);
        }
        CloseableIterator<RT> iterator = iterate(expr);
        return uniqueResult(iterator);
    }

    private long unsafeCount() throws SQLException {
        SQLSerializer serializer = serialize(true);
        final String queryString = serializer.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("query : {}", queryString);
        }
        List<Object> constants = serializer.getConstants();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(queryString);
            setParameters(stmt, constants, serializer.getConstantPaths(), getMetadata().getParams());
            rs = stmt.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw configuration.translate(queryString, constants, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
    }

    public void setUseLiterals(boolean useLiterals) {
        this.useLiterals = useLiterals;
    }
    
    @Override
    protected void clone(Q query) {
        super.clone(query);
        this.useLiterals = query.useLiterals;
        this.listeners = new SQLListeners(query.listeners);
    }
    
    @Override
    public Q clone() {
        return this.clone(this.conn);
    }
    
    public abstract Q clone(Connection connection);

}
