package com.mysema.query.sql;

import java.sql.Types;

import com.google.common.base.Preconditions;
import com.mysema.query.types.Path;

/**
 * Provides metadata like the column name, JDBC type and constraints
 */
public class ColumnMetadata {

    /**
     * Returns this path's column metadata if present. Otherwise returns default
     * metadata where the column name is equal to the path's name.
     */
    public static ColumnMetadata getColumnMetadata(Path<?> path) {
        Path<?> parent = path.getMetadata().getParent();
        if (parent != null && parent instanceof RelationalPath) {
            ColumnMetadata columnMetadata = ((RelationalPath<?>) parent).getColumnMetadata(path);
            if (columnMetadata != null) {
                return columnMetadata;
            }
        }
        return ColumnMetadata.named(path.getMetadata().getName());
    }

    /**
     * Creates default column meta data with the given column name, but without
     * any type or constraint information. Use the fluent builder methods to
     * further configure it.
     * 
     * @throws NullPointerException
     *             if the name is null
     */
    public static ColumnMetadata named(String name) {
        return new ColumnMetadata(name, null, true, UNDEFINED, UNDEFINED, UNDEFINED, true, true);
    }

    private static int UNDEFINED = -1;

    private final String name;
    private final Integer jdbcType;
    private final boolean nullable;
    private final int length;
    private final int precision;
    private final int scale;
    private final boolean updateable;
    private final boolean insertable;

    private ColumnMetadata(String name, Integer jdbcType, boolean nullable, int length,
            int precision, int scale, boolean updateable, boolean insertable) {
        this.name = Preconditions.checkNotNull(name, "Name cannot be null");
        this.jdbcType = jdbcType;
        this.nullable = nullable;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.updateable = updateable;
        this.insertable = insertable;
    }

    public String getName() {
        return name;
    }

    public boolean hasJdbcType() {
        return jdbcType != null;
    }

    /**
     * The JDBC type of this column.
     * 
     * @see Types
     * @see ColumnMetadata#hasJdbcType()
     * @throws IllegalStateException
     *             if this metadata has no type information
     */
    public int getJdbcType() {
        Preconditions.checkState(hasJdbcType(), name + " has no jdbc type");
        return jdbcType;
    }

    /**
     * Returns a new column with the given type information
     * 
     * @see Types
     */
    public ColumnMetadata ofType(int jdbcType) {
        return new ColumnMetadata(name, jdbcType, nullable, length, precision, scale, updateable,
                insertable);
    }

    public boolean isNullable() {
        return nullable;
    }

    /**
     * Returns a new column with a not null constraint
     */
    public ColumnMetadata notNull() {
        return new ColumnMetadata(name, jdbcType, false, length, precision, scale, updateable,
                insertable);
    }

    /**
     * The length constraint of this column.
     * 
     * @see ColumnMetadata#hasLength()
     * @throws IllegalStateException
     *             if this column has no length constraint
     */
    public int getLength() {
        Preconditions.checkState(hasLength(), name + " has no length");
        return length;
    }

    public boolean hasLength() {
        return length != UNDEFINED;
    }

    /**
     * Returns a new column with the given length constraint. The length must be
     * > 0.
     * 
     * @throws IllegalStateException
     *             if precision and scale have already been defined
     * @throws IllegalArgumentException
     *             if the length is invalid
     */
    public ColumnMetadata withlength(int length) {
        Preconditions.checkState(scale == UNDEFINED && precision == UNDEFINED,
                "Cannot define both length and scale/precision");
        Preconditions.checkArgument(length > 0, "Length must be > 0");
        return new ColumnMetadata(name, jdbcType, nullable, length, precision, scale, updateable,
                insertable);
    }

    /**
     * Returns the precision of this numeric column.
     * 
     * @see ColumnMetadata#hasPrecisionAndScale()
     * @throws IllegalStateException
     *             if this column has no precision
     */
    public int getPrecision() {
        Preconditions.checkState(hasPrecisionAndScale(), name + " has no precision");
        return precision;
    }

    /**
     * Returns the scale of this numeric column
     * 
     * @see ColumnMetadata#hasPrecisionAndScale()
     * @throws IllegalStateException
     *             if this column has no scale
     */
    public int getScale() {
        Preconditions.checkState(hasPrecisionAndScale(), name + " has no scale");
        return scale;
    }

    public boolean hasPrecisionAndScale() {
        return scale != UNDEFINED && precision != UNDEFINED;
    }

    /**
     * Returns a new column with the given precision and scale constraint. Both
     * must be > 0.
     * 
     * @throws IllegalStateException
     *             if a length constraint has already been added
     * @throws IllegalArgumentException
     *             if precision or scale are invalid
     */
    public ColumnMetadata withPrecisionAndScale(int precision, int scale) {
        Preconditions.checkState(length == UNDEFINED,
                "Cannot define both length and scale/precision");
        Preconditions.checkArgument(precision > 0, "Precision must be > 0");
        Preconditions.checkArgument(scale > 0, "Scale must be > 0");
        return new ColumnMetadata(name, jdbcType, nullable, length, precision, scale, updateable,
                insertable);
    }

    public boolean isUpdateable() {
        return updateable;
    }

    /**
     * Returns a new column with a no-update constraint.
     */
    public ColumnMetadata nonUpdateable() {
        return new ColumnMetadata(name, jdbcType, nullable, length, precision, scale, false,
                insertable);
    }

    public boolean isInsertable() {
        return insertable;
    }

    /**
     * Returns a new column with a no-insert constraint.
     */
    public ColumnMetadata nonInsertable() {
        return new ColumnMetadata(name, jdbcType, nullable, length, precision, scale, updateable,
                false);
    }
}
