package com.frog.graphql.test.emp.querybuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.frog.graphql.test.emp.repository.EmpDbField;
import com.frog.graphql.test.emp.repository.EmpFieldEnum;
import com.frog.graphql.test.emp.repository.EmpRepository;
import com.frog.graphql.test.emp.repository.EmpTableEnum;
import com.frog.graphql.test.querybuilder.DbField;
import com.frog.graphql.test.querybuilder.SqlQuery;
import com.frog.graphql.test.querybuilder.constraint.SqlOperatorEnum;
import com.frog.graphql.test.querybuilder.constraint.StringConstraint;
import com.frog.graphql.test.querybuilder.constraint.StringOperatorEnum;

@SpringBootTest
//@ContextConfiguration(classes = {EmpRepository.class, EmpQueryBuilder.class})
class EmpQueryBuilderTest {
	@Autowired
	private EmpRepository empRepository;

	@Autowired
	private EmpQueryBuilder empQueryBuilder;
	
	@Test
	void testCreateQuery2() {
		EmpQueryBuilderArgs args = new EmpQueryBuilderArgs(EmpTableEnum.EMPLOYEES);
		args.setSqlOperator(SqlOperatorEnum.OR);
		List<DbField> fieldList = new ArrayList<DbField>();
		EmpDbField firstNameField = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_FIRST_NAME);
		EmpDbField lastNameField = empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_LAST_NAME);
		fieldList.add(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_EMPLOYEE_ID));
		fieldList.add(lastNameField);
		fieldList.add(empRepository.getFields().get(EmpFieldEnum.EMPLOYEES_JOB_ID));
		args.addAdditionalSelectedFieldList(fieldList);
		
		args.addConstraint(new StringConstraint(1, firstNameField, StringOperatorEnum.BEGINS_WITH, "abc"));
		args.addConstraint(new StringConstraint(2, firstNameField, StringOperatorEnum.ENDS_WITH, "cde"));
		args.addConstraint(new StringConstraint(3, lastNameField, StringOperatorEnum.EQUALS, "xy"));

		SqlQuery query = empQueryBuilder.createQueryforTable(args);
		System.out.println(query.getSql());
		
		assertTrue(query.getSql().contains(String.format("%s like :p1 || '%%'", firstNameField.getFullName())));
		assertTrue(query.getSql().contains(String.format("%s like '%%' || :p2", firstNameField.getFullName())));
		assertTrue(query.getSql().contains(String.format("%s = :p3", lastNameField.getFullName())));
	}
}
