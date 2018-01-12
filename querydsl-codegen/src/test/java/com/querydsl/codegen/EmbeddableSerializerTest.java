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

import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.Parameter;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;
import com.querydsl.core.annotations.PropertyType;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Time;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EmbeddableSerializerTest {

    private final QueryTypeFactory queryTypeFactory = new QueryTypeFactoryImpl("Q", "", "");

    private final TypeMappings typeMappings = new JavaTypeMappings();

    private final EntitySerializer serializer = new EmbeddableSerializer(typeMappings, Collections.<String>emptySet(), false);

    private final StringWriter writer = new StringWriter();

    @Test
    public void properties() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        entityType.addProperty(new Property(entityType, "b", new ClassType(TypeCategory.BOOLEAN, Boolean.class)));
        entityType.addProperty(new Property(entityType, "c", new ClassType(TypeCategory.COMPARABLE, String.class)));
        //entityType.addProperty(new Property(entityType, "cu", new ClassType(TypeCategory.CUSTOM, PropertyType.class)));
        entityType.addProperty(new Property(entityType, "d", new ClassType(TypeCategory.DATE, Date.class)));
        entityType.addProperty(new Property(entityType, "e", new ClassType(TypeCategory.ENUM, PropertyType.class)));
        entityType.addProperty(new Property(entityType, "dt", new ClassType(TypeCategory.DATETIME, Date.class)));
        entityType.addProperty(new Property(entityType, "i", new ClassType(TypeCategory.NUMERIC, Integer.class)));
        entityType.addProperty(new Property(entityType, "s", new ClassType(TypeCategory.STRING, String.class)));
        entityType.addProperty(new Property(entityType, "t", new ClassType(TypeCategory.TIME, Time.class)));
        typeMappings.register(entityType, queryTypeFactory.create(entityType));

        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        CompileUtils.assertCompiles("QEntity", writer.toString());
    }

    @Test
    public void originalCategory() throws IOException {
        Map<TypeCategory, String> categoryToSuperClass
                = new EnumMap<TypeCategory, String>(TypeCategory.class);
        categoryToSuperClass.put(TypeCategory.COMPARABLE, "ComparablePath<Entity>");
        categoryToSuperClass.put(TypeCategory.ENUM, "EnumPath<Entity>");
        categoryToSuperClass.put(TypeCategory.DATE, "DatePath<Entity>");
        categoryToSuperClass.put(TypeCategory.DATETIME, "DateTimePath<Entity>");
        categoryToSuperClass.put(TypeCategory.TIME, "TimePath<Entity>");
        categoryToSuperClass.put(TypeCategory.NUMERIC, "NumberPath<Entity>");
        categoryToSuperClass.put(TypeCategory.STRING, "StringPath");
        categoryToSuperClass.put(TypeCategory.BOOLEAN, "BooleanPath");

        for (Map.Entry<TypeCategory, String> entry : categoryToSuperClass.entrySet()) {
            StringWriter w = new StringWriter();
            SimpleType type = new SimpleType(entry.getKey(), "Entity", "", "Entity",false,false);
            EntityType entityType = new EntityType(type);
            typeMappings.register(entityType, queryTypeFactory.create(entityType));

            serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(w));
            assertTrue(entry.getValue() + " is missing from " + w, w.toString().contains("public class QEntity extends " + entry.getValue() + " {"));
        }

    }

    @Test
    public void empty() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        typeMappings.register(entityType, queryTypeFactory.create(entityType));

        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        CompileUtils.assertCompiles("QEntity", writer.toString());
    }

    @Test
    public void no_package() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        typeMappings.register(entityType, queryTypeFactory.create(entityType));
        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        assertTrue(writer.toString().contains("public class QEntity extends BeanPath<Entity> {"));
        CompileUtils.assertCompiles("QEntity", writer.toString());
    }

    @Test
    public void correct_superclass() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "java.util.Locale", "java.util", "Locale",false,false);
        EntityType entityType = new EntityType(type);
        typeMappings.register(entityType, queryTypeFactory.create(entityType));
        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        assertTrue(writer.toString().contains("public class QLocale extends BeanPath<Locale> {"));
        CompileUtils.assertCompiles("QLocale", writer.toString());
    }

    @Test
    public void primitive_array() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        entityType.addProperty(new Property(entityType, "bytes", new ClassType(byte[].class)));
        typeMappings.register(entityType, queryTypeFactory.create(entityType));
        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));

        assertTrue(writer.toString().contains("public final SimplePath<byte[]> bytes"));
        CompileUtils.assertCompiles("QEntity", writer.toString());
    }

    @Test
    public void include() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        entityType.addProperty(new Property(entityType, "b", new ClassType(TypeCategory.BOOLEAN, Boolean.class)));
        entityType.addProperty(new Property(entityType, "c", new ClassType(TypeCategory.COMPARABLE, String.class)));
        //entityType.addProperty(new Property(entityType, "cu", new ClassType(TypeCategory.CUSTOM, PropertyType.class)));
        entityType.addProperty(new Property(entityType, "d", new ClassType(TypeCategory.DATE, Date.class)));
        entityType.addProperty(new Property(entityType, "e", new ClassType(TypeCategory.ENUM, PropertyType.class)));
        entityType.addProperty(new Property(entityType, "dt", new ClassType(TypeCategory.DATETIME, Date.class)));
        entityType.addProperty(new Property(entityType, "i", new ClassType(TypeCategory.NUMERIC, Integer.class)));
        entityType.addProperty(new Property(entityType, "s", new ClassType(TypeCategory.STRING, String.class)));
        entityType.addProperty(new Property(entityType, "t", new ClassType(TypeCategory.TIME, Time.class)));

        EntityType subType = new EntityType(new SimpleType(TypeCategory.ENTITY, "Entity2", "", "Entity2",false,false));
        subType.include(new Supertype(type,entityType));

        typeMappings.register(entityType, queryTypeFactory.create(entityType));
        typeMappings.register(subType, queryTypeFactory.create(subType));

        serializer.serialize(subType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        CompileUtils.assertCompiles("QEntity2", writer.toString());
    }

    @Test
    public void superType() throws IOException {
        EntityType superType = new EntityType(new SimpleType(TypeCategory.ENTITY, "Entity2", "", "Entity2",false,false));
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type, Collections.singleton(new Supertype(superType, superType)));
        typeMappings.register(superType, queryTypeFactory.create(superType));
        typeMappings.register(entityType, queryTypeFactory.create(entityType));

        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        assertTrue(writer.toString().contains("public final QEntity2 _super = new QEntity2(this);"));
        //CompileUtils.assertCompiles("QEntity", writer.toString());
    }

    @Test
    public void delegates() throws IOException {
        SimpleType type = new SimpleType(TypeCategory.ENTITY, "Entity", "", "Entity",false,false);
        EntityType entityType = new EntityType(type);
        Delegate delegate = new Delegate(type, type, "test", Collections.<Parameter>emptyList(), Types.STRING);
        entityType.addDelegate(delegate);
        typeMappings.register(entityType, queryTypeFactory.create(entityType));

        serializer.serialize(entityType, SimpleSerializerConfig.DEFAULT, new JavaWriter(writer));
        assertTrue(writer.toString().contains("return Entity.test(this);"));
        CompileUtils.assertCompiles("QEntity", writer.toString());
    }

}
