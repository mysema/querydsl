package com.querydsl.r2dbc.dml;

import com.querydsl.core.QueryFlag;
import com.querydsl.r2dbc.KeyAccessorsTest.QEmployee;
import com.querydsl.r2dbc.SQLTemplates;
import com.querydsl.sql.SQLBindings;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class R2DBCInsertClauseTest {

    @Test(expected = IllegalStateException.class)
    public void noConnection() {
        QEmployee emp1 = new QEmployee("emp1");
        R2DBCInsertClause insert = new R2DBCInsertClause(null, SQLTemplates.DEFAULT, emp1);
        insert.set(emp1.id, 1);
        insert.execute().block();
    }

    @Test
    public void getSQL() {
        QEmployee emp1 = new QEmployee("emp1");
        R2DBCInsertClause insert = new R2DBCInsertClause(null, SQLTemplates.DEFAULT, emp1);
        insert.set(emp1.id, 1);

        SQLBindings sql = insert.getSQL().get(0);
        assertEquals("insert into EMPLOYEE (ID)\nvalues (?)", sql.getSQL());
        assertEquals(Collections.singletonList(1), sql.getNullFriendlyBindings());
    }

    @Test
    public void bulk() {
        QEmployee emp1 = new QEmployee("emp1");
        R2DBCInsertClause insert = new R2DBCInsertClause(null, SQLTemplates.DEFAULT, emp1);
        insert.set(emp1.id, 1);
        insert.addBatch();
        insert.set(emp1.id, 2);
        insert.addBatch();
        insert.addFlag(QueryFlag.Position.END, " on duplicate key ignore");
        insert.setBatchToBulk(true);
        assertEquals("insert into EMPLOYEE (ID)\n" +
                "values (?), (?) on duplicate key ignore", insert.getSQL().get(0).getSQL());

    }

    @Test
    public void getSQLWithPreservedColumnOrder() {
        com.querydsl.r2dbc.domain.QEmployee emp1 = new com.querydsl.r2dbc.domain.QEmployee("emp1");
        R2DBCInsertClause insert = new R2DBCInsertClause(null, SQLTemplates.DEFAULT, emp1);
        insert.populate(emp1);

        SQLBindings sql = insert.getSQL().get(0);
        assertEquals("The order of columns in generated sql should be predictable",
                "insert into EMPLOYEE (ID, FIRSTNAME, LASTNAME, SALARY, DATEFIELD, TIMEFIELD, SUPERIOR_ID)\n" +
                        "values (EMPLOYEE.ID, EMPLOYEE.FIRSTNAME, EMPLOYEE.LASTNAME, EMPLOYEE.SALARY, EMPLOYEE.DATEFIELD, EMPLOYEE.TIMEFIELD, EMPLOYEE.SUPERIOR_ID)", sql.getSQL());
    }

    @Test
    public void clear() {
        QEmployee emp1 = new QEmployee("emp1");
        R2DBCInsertClause insert = new R2DBCInsertClause(null, SQLTemplates.DEFAULT, emp1);
        insert.set(emp1.id, 1);
        insert.addBatch();
        assertEquals(1, insert.getBatchCount());
        insert.clear();
        assertEquals(0, insert.getBatchCount());
    }

}
