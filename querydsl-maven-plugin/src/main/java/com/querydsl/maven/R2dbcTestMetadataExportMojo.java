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
package com.querydsl.maven;

import com.querydsl.r2dbc.codegen.MetaDataExporter;

/**
 * {@code TestMetadataExportMojo} is a goal for {@link MetaDataExporter} usage and is bound to the generated-sources phase
 *
 * @phase generate-sources
 * @goal r2dbc-test-export
 *
 */
public class R2dbcTestMetadataExportMojo extends AbstractR2dbcMetaDataExportMojo {

    @Override
    protected boolean isForTest() {
        return true;
    }

}
