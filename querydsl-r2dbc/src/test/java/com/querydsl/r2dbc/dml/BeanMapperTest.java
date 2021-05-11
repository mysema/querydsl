package com.querydsl.r2dbc.dml;

import com.querydsl.core.types.Path;
import com.querydsl.r2dbc.domain.QEmployee;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BeanMapperTest extends AbstractMapperTest {

    private static final QEmployee emp = QEmployee.employee;

    @Test
    public void extract() {
        Map<Path<?>, Object> values = BeanMapper.DEFAULT.createMap(emp, employee);
        assertEquals(employee.getDatefield(), values.get(emp.datefield));
        assertEquals(employee.getFirstname(), values.get(emp.firstname));
        assertEquals(employee.getLastname(), values.get(emp.lastname));
        assertEquals(employee.getSalary(), values.get(emp.salary));
        assertEquals(employee.getSuperiorId(), values.get(emp.superiorId));
        assertEquals(employee.getTimefield(), values.get(emp.timefield));
    }

    @Test
    public void extract2() {
        Map<Path<?>, Object> values = BeanMapper.DEFAULT.createMap(emp, new EmployeeX());
        assertTrue(values.isEmpty());
    }

}
