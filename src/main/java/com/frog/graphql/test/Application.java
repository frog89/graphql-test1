package com.frog.graphql.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

//	@Autowired
//	private EmployeeService employeeService;
//
//	@Autowired
//	private JobService jobService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
//	@Bean
//	public ServletRegistrationBean graphQLServlet() {
//		GraphQLSchema schema = Application.buildSchema(employeeService, jobService);
//		return new ServletRegistrationBean(SimpleGraphQLHttpServlet.newBuilder(schema).build(), "/graphql");
//	}
//	
//	private static GraphQLSchema buildSchema(EmployeeService employeeService, JobService jobService) {
//		return SchemaParser
//			.newParser()
//			.file("graphql/schema.graphqls")
//			//.dictionary()
//			.resolvers(new Query(employeeService, jobService), new EmployeeResolver(jobService))
//			.build()
//			.makeExecutableSchema();
//	}

}
