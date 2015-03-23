package com.mysema.query;

import static com.mysema.query.Constants.employee;
import static com.mysema.query.Constants.employee2;
import static com.mysema.query.Constants.survey;
import static com.mysema.query.Target.NUODB;
import static com.mysema.query.Target.ORACLE;
import static com.mysema.query.Target.SQLSERVER;
import static com.mysema.query.Target.TERADATA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.mysema.query.sql.SQLExpressions;
import com.mysema.query.sql.WindowOver;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.Wildcard;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.SimplePath;
import com.mysema.query.types.query.ListSubQuery;
import com.mysema.query.types.query.SimpleSubQuery;
import com.mysema.testutil.ExcludeIn;
import com.mysema.testutil.IncludeIn;

public class SelectWindowFunctionsBase extends AbstractBaseTest {

    @Test
    @ExcludeIn({SQLSERVER, NUODB}) // FIXME
    public void WindowFunctions() {
        NumberPath<Integer> path = survey.id;
        NumberPath<?> path2 = survey.id;

        List<WindowOver<?>> exprs = new ArrayList<WindowOver<?>>();
        add(exprs, SQLExpressions.avg(path));
        add(exprs, SQLExpressions.count(path));
        add(exprs, SQLExpressions.corr(path, path2));
        add(exprs, SQLExpressions.covarPop(path, path2));
        add(exprs, SQLExpressions.covarSamp(path, path2));
        add(exprs, SQLExpressions.cumeDist(), TERADATA);
        add(exprs, SQLExpressions.denseRank(), TERADATA);
        add(exprs, SQLExpressions.firstValue(path), TERADATA);
        add(exprs, SQLExpressions.lag(path), TERADATA);
        add(exprs, SQLExpressions.lastValue(path), TERADATA);
        add(exprs, SQLExpressions.lead(path), TERADATA);
        add(exprs, SQLExpressions.max(path));
        add(exprs, SQLExpressions.min(path));
        add(exprs, SQLExpressions.nthValue(path, 2), TERADATA);
        add(exprs, SQLExpressions.ntile(3), TERADATA);
        add(exprs, SQLExpressions.percentRank());
        add(exprs, SQLExpressions.rank());
        add(exprs, SQLExpressions.rowNumber());
        add(exprs, SQLExpressions.stddev(path), TERADATA);
        add(exprs, SQLExpressions.stddevPop(path), TERADATA);
        add(exprs, SQLExpressions.stddevSamp(path), TERADATA);
        add(exprs, SQLExpressions.sum(path));
        add(exprs, SQLExpressions.variance(path), TERADATA);
        add(exprs, SQLExpressions.varPop(path), TERADATA);
        add(exprs, SQLExpressions.varSamp(path), TERADATA);

        for (WindowOver<?> wo : exprs) {
            query().from(survey).list(wo.over().partitionBy(survey.name).orderBy(survey.id));
        }
    }

    @Test
    @ExcludeIn(NUODB)
    public void WindowFunctions_Manual_Paging() {
        Expression<Long> rowNumber = SQLExpressions.rowNumber().over().orderBy(employee.lastname.asc()).as("rn");
        Expression<Object[]> all = Wildcard.all;

        // simple
        System.out.println("#1");
        for (Tuple row : query().from(employee).list(employee.firstname, employee.lastname, rowNumber)) {
            System.out.println(row);
        }
        System.out.println();

        // with subquery, generic alias
        System.out.println("#2");
        ListSubQuery<Tuple> sub = sq().from(employee).list(employee.firstname, employee.lastname, rowNumber);
        SimplePath<Tuple> subAlias = new SimplePath<Tuple>(Tuple.class, "s");
        for (Object[] row : query().from(sub.as(subAlias)).list(all)) {
            System.out.println(Arrays.asList(row));
        }
        System.out.println();

        // with subquery, only row number
        System.out.println("#3");
        SimpleSubQuery<Long> sub2 = sq().from(employee).unique(rowNumber);
        SimplePath<Long> subAlias2 = new SimplePath<Long>(Long.class, "s");
        for (Object[] row : query().from(sub2.as(subAlias2)).list(all)) {
            System.out.println(Arrays.asList(row));
        }
        System.out.println();

        // with subquery, specific alias
        System.out.println("#4");
        ListSubQuery<Tuple> sub3 = sq().from(employee).list(employee.firstname, employee.lastname, rowNumber);
        for (Tuple row : query().from(sub3.as(employee2)).list(employee2.firstname, employee2.lastname)) {
            System.out.println(Arrays.asList(row));
        }
    }

    @Test
    @IncludeIn(ORACLE)
    public void WindowFunctions_Keep() {
        List<WindowOver<?>> exprs = new ArrayList<WindowOver<?>>();
        NumberPath<Integer> path = survey.id;

        add(exprs, SQLExpressions.avg(path));
        add(exprs, SQLExpressions.count(path));
        add(exprs, SQLExpressions.max(path));
        add(exprs, SQLExpressions.min(path));
        add(exprs, SQLExpressions.stddev(path));
        add(exprs, SQLExpressions.variance(path));

        for (WindowOver<?> wo : exprs) {
            query().from(survey).list(wo.keepFirst().orderBy(survey.id));
        }
    }

    @Test
    @ExcludeIn({SQLSERVER, NUODB})
    public void WindowFunctions_Regr() {
        List<WindowOver<?>> exprs = new ArrayList<WindowOver<?>>();
        NumberPath<Integer> path = survey.id;
        NumberPath<?> path2 = survey.id;

        add(exprs, SQLExpressions.regrSlope(path,  path2), SQLSERVER);
        add(exprs, SQLExpressions.regrIntercept(path,  path2));
        add(exprs, SQLExpressions.regrCount(path,  path2));
        add(exprs, SQLExpressions.regrR2(path,  path2));
        add(exprs, SQLExpressions.regrAvgx(path, path2));
        add(exprs, SQLExpressions.regrSxx(path, path2));
        add(exprs, SQLExpressions.regrSyy(path, path2));
        add(exprs, SQLExpressions.regrSxy(path,  path2));

        for (WindowOver<?> wo : exprs) {
            query().from(survey).list(wo.over().partitionBy(survey.name).orderBy(survey.id));
        }
    }

    @Test
    @IncludeIn(ORACLE)
    public void WindowFunctions_Oracle() {
        List<WindowOver<?>> exprs = new ArrayList<WindowOver<?>>();
        NumberPath<Integer> path = survey.id;
        add(exprs, SQLExpressions.countDistinct(path));
        add(exprs, SQLExpressions.ratioToReport(path));
        add(exprs, SQLExpressions.stddevDistinct(path));

        for (WindowOver<?> wo : exprs) {
            query().from(survey).list(wo.over().partitionBy(survey.name));
        }
    }

    @Test
    @ExcludeIn(NUODB)
    public void WindowFunctions_Over() {
        //SELECT Shipment_id,Ship_date, SUM(Qty) OVER () AS Total_Qty
        //FROM TestDB.Shipment

        query().from(employee).list(
                employee.id,
                SQLExpressions.sum(employee.salary).over());
    }

    @Test
    @ExcludeIn(NUODB)
    public void WindowFunctions_PartitionBy() {
        //SELECT Shipment_id,Ship_date,Ship_Type,
        //SUM(Qty) OVER (PARTITION BY Ship_Type ) AS Total_Qty
        //FROM TestDB.Shipment

        query().from(employee).list(
                employee.id,
                employee.superiorId,
                SQLExpressions.sum(employee.salary).over()
                    .partitionBy(employee.superiorId));
    }

    @Test
    @ExcludeIn({SQLSERVER, NUODB})
    public void WindowFunctions_OrderBy() {
        //SELECT Shipment_id,Ship_date,Ship_Type,
        //SUM(Qty) OVER (PARTITION BY Ship_Type ORDER BY Ship_Dt ) AS Total_Qty
        //FROM TestDB.Shipment

        query().from(employee).list(
                employee.id,
                SQLExpressions.sum(employee.salary).over()
                    .partitionBy(employee.superiorId)
                    .orderBy(employee.datefield));
    }

    @Test
    @ExcludeIn({SQLSERVER, NUODB})
    public void WindowFunctions_UnboundedRows() {
        //SELECT Shipment_id,Ship_date,Ship_Type,
        //SUM(Qty) OVER (PARTITION BY Ship_Type ORDER BY Ship_Dt
        //ROWS BETWEEN UNBOUNDED PRECEDING
        //AND CURRENT ROW) AS Total_Qty
        //FROM TestDB.Shipment

        query().from(employee).list(
                employee.id,
                SQLExpressions.sum(employee.salary).over()
                    .partitionBy(employee.superiorId)
                    .orderBy(employee.datefield)
                    .rows().between().unboundedPreceding().currentRow());
    }

    @Test
    @IncludeIn({TERADATA})
    @ExcludeIn(NUODB)
    public void WindowFunctions_Qualify() {
        //SELECT Shipment_id,Ship_date,Ship_Type,
        //Rank() OVER (PARTITION BY Ship_Type ORDER BY Ship_Dt ) AS rnk
        //FROM TestDB.Shipment
        //QUALIFY  (Rank() OVER (PARTITION BY Ship_Type ORDER BY Ship_Dt ))  =1

        teradataQuery().from(employee)
               .qualify(SQLExpressions.rank().over()
                       .partitionBy(employee.superiorId)
                       .orderBy(employee.datefield).eq(1l))
               .list(employee.id,SQLExpressions.sum(employee.salary).over());

    }

}
