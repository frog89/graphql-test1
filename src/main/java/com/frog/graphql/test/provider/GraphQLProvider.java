package com.frog.graphql.test.provider;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.frog.graphql.test.fetcher.AllFetcher;
import com.frog.graphql.test.fetcher.EmployeeFetcher;
import com.frog.graphql.test.fetcher.JobFetcher;
import com.frog.graphql.test.pojo.Employee;
import com.frog.graphql.test.pojo.Job;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
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
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
    	return RuntimeWiring.newRuntimeWiring()
    		.type(newTypeWiring("Query")
    			.dataFetcher("allEmps", employeeFetcher.fetchAll())
    			.dataFetcher("allJobs", jobFetcher.fetchAll())
    			.dataFetcher("allAll", allFetcher.fetchAll())
    		).type(newTypeWiring("Employee")
				.dataFetcher("job", employeeFetcher.fetchJobForEmployee())
			).type(newTypeWiring("Job")
				.dataFetcher("employees", jobFetcher.fetchEmployeesForJob())
			).type("All", typeWriting -> typeWriting.typeResolver(getAllTypeResolver())
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
