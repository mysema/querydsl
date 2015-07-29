/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
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
package com.querydsl.codegen;

import java.util.Collections;

/**
 * {@code CodegenModule} provides a module for general serialization
 *
 * @author tiwe
 *
 */
public class CodegenModule  extends AbstractModule {

    /**
     * key for the query type name prefix
     */
    public static final String PREFIX = "prefix";

    /**
     * key for the query type name suffix
     */
    public static final String SUFFIX = "suffix";

    /**
     * key for the keywords set
     */
    public static final String KEYWORDS = "keywords";

    /**
     * key for the package suffix
     */
    public static final String PACKAGE_SUFFIX = "packageSuffix";

    /**
     * key for the custom imports set
     */
    public static final String IMPORTS = "imports";

    /**
     * key for the keywords set
     */
    public static final String CASE_TRANSFORMER_CLASS = "caseTransformerClass";

    @Override
    protected void configure() {
        bind(TypeMappings.class, JavaTypeMappings.class);
        bind(QueryTypeFactory.class, QueryTypeFactoryImpl.class);
        bind(EntitySerializer.class);
        bind(EmbeddableSerializer.class);
        bind(ProjectionSerializer.class);
        bind(SupertypeSerializer.class);

        // configuration for QueryTypeFactory
        bind(PREFIX, "Q");
        bind(SUFFIX, "");
        bind(PACKAGE_SUFFIX, "");
        bind(KEYWORDS, Collections.<String>emptySet());
        bind(IMPORTS, Collections.<String>emptySet());
        bind(CASE_TRANSFORMER_CLASS, UncapitalizedCaseTransformer.class.getCanonicalName());
    }

}