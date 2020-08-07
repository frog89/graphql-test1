package com.frog.graphql.test.provider;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.frog.graphql.test.fetcher.AllFetcher;
import com.frog.graphql.test.fetcher.EmpDataFetcher;
import com.frog.graphql.test.fetcher.EmployeeFetcher;
import com.frog.graphql.test.fetcher.JobFetcher;
import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

@Component
public class GraphQLProvider {

    @Autowired
    EmployeeFetcher employeeFetcher;
    @Autowired
    JobFetcher jobFetcher;
    @Autowired
    AllFetcher allFetcher;

    private GraphQL graphQL;

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("graphql/schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser(). parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema schema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
        return schema;
    }

    private RuntimeWiring buildWiring() {
//    	GraphqlFieldVisibility fieldVisibility = BlockedFields.newBlock()
//    		.addPattern(".*\\.employees")
//    		.addPattern("Job.employees.job")
//    		.build();
//    	GraphqlFieldVisibility fieldVisibility = new CustomFieldVisibility();

    	DataFetcher<List<Employee>> empListFetcher = dataFetchingEnvironment -> {
    		Consumer<Consumer<Employee>> consumerAction = c -> employeeFetcher.fetchAll(c, dataFetchingEnvironment);
    		return EmpDataFetcher.getAsList(consumerAction);
		};

    	DataFetcher<List<Job>> jobListFetcher = dataFetchingEnvironment -> {
    		Consumer<Consumer<Job>> consumerAction = c -> jobFetcher.fetchAll(c, dataFetchingEnvironment);
			return EmpDataFetcher.getAsList(consumerAction);
		};

    	DataFetcher<List<Object>> allListFetcher = dataFetchingEnvironment -> {
    		Consumer<Consumer<Object>> consumerAction = c -> allFetcher.fetchAll(c, dataFetchingEnvironment);
			return EmpDataFetcher.getAsList(consumerAction);
		};

    	return RuntimeWiring.newRuntimeWiring()
    		.type(newTypeWiring("Query")
    			.dataFetcher("allEmps", empListFetcher)
    			.dataFetcher("allJobs", jobListFetcher)
    			.dataFetcher("allAll", allListFetcher)
    		).type(newTypeWiring("Employee")
				.dataFetcher("job", employeeFetcher.fetchJobForEmployee())
			).type(newTypeWiring("Job")
				.dataFetcher("employees", jobFetcher.fetchEmployeesForJob())
			).type("All", typeWriting -> typeWriting.typeResolver(getAllTypeResolver())
//			).fieldVisibility(fieldVisibility)
			).build();
    }
    
    private TypeResolver getAllTypeResolver() {
    	return new TypeResolver() {
    	    @Override
    	    public GraphQLObjectType getType(TypeResolutionEnvironment env) {
    	        Object javaObject = env.getObject();
    	        if (javaObject instanceof Job) {
    	            return env.getSchema().getObjectType("Job");
    	        }  
    	        if (javaObject instanceof Employee) {
    	        	return env.getSchema().getObjectType("Employee");
    	        }
    	        return null;
    	    }
    	};
    }
    
    
}
