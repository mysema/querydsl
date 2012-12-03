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
package com.mysema.query.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;


/**
 * ExpressionUtils provides utilities for constructing common operation instances. This class is 
 * used internally in Querydsl and is not suitable to be used in cases where DSL methods are needed,
 * since the Expression implementations used in this class are minimal internal implementations.
 * 
 * @author tiwe
 *
 */
public final class ExpressionUtils {
        
    /**
     * @param col
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> all(CollectionExpression<?, ? super T> col) {
        return OperationImpl.create((Class<T>)col.getParameter(0), Ops.QuantOps.ALL, col);
    }

    /**
     * @param col
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Expression<T> any(CollectionExpression<?, ? super T> col) {
        return OperationImpl.create((Class<T>)col.getParameter(0), Ops.QuantOps.ANY, col);
    }
            
    /**
     * Create the intersection of the given arguments
     * 
     * @param exprs
     * @return
     */
    @Nullable
    public static Predicate allOf(Collection<Predicate> exprs) {
        Predicate rv = null;
        for (Predicate b : exprs) {
            if (b != null) {
                rv = rv == null ? b : ExpressionUtils.and(rv,b);    
            }            
        }
        return rv;
    }
    
    /**
     * Create the intersection of the given arguments
     * 
     * @param exprs
     * @return
     */
    @Nullable
    public static Predicate allOf(Predicate... exprs){
        Predicate rv = null;
        for (Predicate b : exprs) {
            if (b != null) {
                rv = rv == null ? b : ExpressionUtils.and(rv,b);    
            }            
        }
        return rv;
    }

    /**
     * Create the intersection of the given arguments
     * 
     * @param left
     * @param right
     * @return
     */
    public static Predicate and(Predicate left, Predicate right){
        return new PredicateOperation(Ops.AND, left, right);
    }
    

    /**
     * Create the union of the given arguments
     * 
     * @param exprs
     * @return
     */
    @Nullable
    public static Predicate anyOf(Collection<Predicate> exprs){
        Predicate rv = null;
        for (Predicate b : exprs) {
            if (b != null) {
                rv = rv == null ? b : ExpressionUtils.or(rv,b);    
            }            
        }
        return rv;
    }
    
    /**
     * Create the union of the given arguments
     * 
     * @param exprs
     * @return
     */
    @Nullable
    public static Predicate anyOf(Predicate... exprs){
        Predicate rv = null;
        for (Predicate b : exprs) {
            if (b != null) {
                rv = rv == null ? b : ExpressionUtils.or(rv,b);    
            }            
        }
        return rv;
    }
        
    /**
     * Create an alias expression (source as alias) with the given source and alias
     * 
     * @param <D>
     * @param source
     * @param alias
     * @return
     */
    public static <D> Expression<D> as(Expression<D> source, Path<D> alias) {
        return new OperationImpl<D>(alias.getType(), Ops.ALIAS, source, alias);
    }
    
    /**
     * Create an alias expression (source as alias) with the given source and alias
     * 
     * @param <D>
     * @param source
     * @param alias
     * @return
     */
    public static <D> Expression<D> as(Expression<D> source, String alias) {
        return as(source, new PathImpl<D>(source.getType(), alias));
    }

    /**
     * @param source
     * @return
     */
    public static Expression<Long> count(Expression<?> source){
        return OperationImpl.create(Long.class, Ops.AggOps.COUNT_AGG, source);
    }
    
    /**
     * Create an left equals constant expression
     * 
     * @param <D>
     * @param left
     * @param constant
     * @return
     */
    public static <D> Predicate eqConst(Expression<D> left, D constant) {
        return eq(left, new ConstantImpl<D>(constant));
    }
    
    /**
     * Create an left equals right expression
     * 
     * @param <D>
     * @param left
     * @param right
     * @return
     */
    public static <D> Predicate eq(Expression<D> left, Expression<? extends D> right) {
        return new PredicateOperation(Ops.EQ, left, right);
    }
    
    /**
     * Create an left in right expression
     * 
     * @param <D>
     * @param left
     * @param right
     * @return
     */
    public static <D> Predicate in(Expression<D> left, CollectionExpression<?,? extends D> right) {
        return new PredicateOperation(Ops.IN, left, right);
    }
    
    /**
     * Create an left in right expression
     * 
     * @param <D>
     * @param left
     * @param right
     * @return
     */
    public static <D> Predicate in(Expression<D> left, Collection<? extends D> right) {
        if (right.size() == 1) {
            return eqConst(left, right.iterator().next());
        } else {
            return new PredicateOperation(Ops.IN, left, new ConstantImpl<Collection<?>>(right));
        }
    }
    
    /**
     * Create a left is null expression
     * 
     * @param left
     * @return
     */
    public static Predicate isNull(Expression<?> left) {
        return new PredicateOperation(Ops.IS_NULL, left);
    }
    
    /**
     * Create a left is not null expression
     * 
     * @param left
     * @return
     */
    public static Predicate isNotNull(Expression<?> left) {
        return new PredicateOperation(Ops.IS_NOT_NULL, left);
    }   
    
    
    
    /**
     * Convert the given like pattern to a regex pattern
     * 
     * @param expr
     * @return
     */    
    public static Expression<String> likeToRegex(Expression<String> expr){
        return likeToRegex(expr, true);
    }
    
    @SuppressWarnings("unchecked")
    public static Expression<String> likeToRegex(Expression<String> expr, boolean matchStartAndEnd){
        // TODO : this should take the escape character into account
        if (expr instanceof Constant<?>) {
            String like = expr.toString();
            if (matchStartAndEnd) {
                if (!like.startsWith("%")) {
                    like = "^" + like;
                }
                if (!like.endsWith("%")) {
                    like = like + "$";
                }   
            }            
            like = like.replace(".", "\\.").replace("*", "\\*").replace("?", "\\?")
                       .replace("%", ".*").replace("_", ".");
            return ConstantImpl.create(like);
        } else if (expr instanceof Operation<?>) {
            Operation<?> o = (Operation<?>)expr;
            if (o.getOperator() == Ops.CONCAT) {
                Expression<String> lhs = likeToRegex((Expression<String>) o.getArg(0), false);
                Expression<String> rhs = likeToRegex((Expression<String>) o.getArg(1), false);
                return new OperationImpl<String>(String.class, Ops.CONCAT, lhs, rhs);
            } else {
                return expr;
            }
        } else {
            return expr;    
        }        
    }
    
    /**
     * @param exprs
     * @return
     */
    public static Expression<?> list(Expression<?>... exprs) {
        return list(Arrays.asList(exprs));
    }
    

    /**
     * @param exprs
     * @return
     */
    public static Expression<?> list(List<? extends Expression<?>> exprs) {
        Expression<?> rv = exprs.get(0);
        for (int i = 1; i < exprs.size(); i++) {
            rv = OperationImpl.create(Object.class, Ops.LIST, rv, exprs.get(i));
        }
        return rv;
    }    
    
    /**
     * @param expressions
     * @return
     */
    public static Expression<?> merge(List<? extends Expression<?>> expressions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("{"+i+"}");
        }
        return TemplateExpressionImpl.create(
                Object.class, 
                builder.toString(), 
                expressions.toArray(new Expression[expressions.size()]));
    }
    
    @SuppressWarnings("unchecked")
    public static Expression<String> regexToLike(Expression<String> expr){
        if (expr instanceof Constant<?>) {
            return ConstantImpl.create(expr.toString().replace(".*", "%").replace(".", "_"));            
        } else if (expr instanceof Operation<?>) {
            Operation<?> o = (Operation<?>)expr;
            if (o.getOperator() == Ops.CONCAT) {
                Expression<String> lhs = regexToLike((Expression<String>) o.getArg(0));
                Expression<String> rhs = regexToLike((Expression<String>) o.getArg(1));
                return new OperationImpl<String>(String.class, Ops.CONCAT, lhs, rhs);
            } else {
                return expr;
            }            
        } else {
            return expr;    
        }     
    }
    
    /**
     * Create a left not equals constant expression
     * 
     * @param <D>
     * @param left
     * @param constant
     * @return
     */
    public static <D> Predicate neConst(Expression<D> left, D constant) {
        return ne(left, new ConstantImpl<D>(constant));
    }
    
    /**
     * Create a left not equals right expression
     * 
     * @param <D>
     * @param left
     * @param right
     * @return
     */
    public static <D> Predicate ne(Expression<D> left, Expression<? super D> right) {
        return new PredicateOperation(Ops.NE, left, right);
    }
    
    /**
     * Create a left or right expression
     * 
     * @param left
     * @param right
     * @return
     */
    public static Predicate or(Predicate left, Predicate right){
        return new PredicateOperation(Ops.OR, left, right);
    }
    
    private ExpressionUtils(){}
    
}
