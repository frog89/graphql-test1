package com.frog.graphql.test.provider;

import java.util.List;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.visibility.GraphqlFieldVisibility;

public class CustomFieldVisibility implements GraphqlFieldVisibility {

	@Override
    public List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer) {
        if ("Employee".equals(fieldsContainer.getName())) {
        	System.out.println("Employee-FieldDefs");
        }
        if ("Job".equals(fieldsContainer.getName())) {
        	System.out.println("Job-FieldDefs");
        }
        return fieldsContainer.getFieldDefinitions();
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName) {
        if ("Employee".equals(fieldsContainer.getName())) {
        	System.out.println("Employee." + fieldName);
        }
        if ("Job".equals(fieldsContainer.getName())) {
        	System.out.println("Job." + fieldName);
        }
        return fieldsContainer.getFieldDefinition(fieldName);
    }
}
