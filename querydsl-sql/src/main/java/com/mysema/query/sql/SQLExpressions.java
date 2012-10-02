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

import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.SimpleExpression;
import com.mysema.query.types.expr.SimpleOperation;
import com.mysema.query.types.expr.Wildcard;

/**
 * Common SQL expressions
 * 
 * @author tiwe
 *
 */
public final class SQLExpressions {
    
    public static final Expression<Object[]> all = Wildcard.all;
    
    public static final Expression<Long> countAll = Wildcard.count;
    
    public static final SimpleExpression<Long> nextval(String sequence) {
        return nextval(Long.class, sequence);
    }    
    
    public static final <T extends Number> SimpleExpression<T> nextval(Class<T> type, String sequence) {
        return SimpleOperation.create(type, SQLTemplates.NEXTVAL, ConstantImpl.create(sequence));
    }
    
    private SQLExpressions() {}

}
