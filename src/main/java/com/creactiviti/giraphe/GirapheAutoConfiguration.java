package com.creactiviti.giraphe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.creactiviti.giraphe.graph.sql.SqlGraph;
import com.creactiviti.giraphe.graphql.MutationBuilder;
import com.creactiviti.giraphe.graphql.QueryBuilder;
import com.creactiviti.giraphe.graphql.TypeBuilder;

import graphql.GraphQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLObjectType.Builder;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;

@Configuration
@ComponentScan(basePackages="com.creactiviti.giraphe")
public class GirapheAutoConfiguration {
  
  @Lazy
  @Autowired(required=false)
  private List<QueryBuilder> queryBuilders = new ArrayList<>();
  
  @Lazy
  @Autowired(required=false)
  private List<MutationBuilder> mutationBuilders = new ArrayList<>();
  
  @Lazy
  @Autowired(required=false)
  private List<TypeBuilder> typeBuilders = new ArrayList<>();
  
  @Bean
  public GraphQL graphql () {
    // build the Query (Read) portion of the GraphQL schema
    Builder queryBuilder = GraphQLObjectType.newObject().name("Query");
    
    queryBuilders.forEach(qb->qb.build(queryBuilder));
    
    // build the Mutation (Write) portion of the GraphQL schema    
    
    Builder mutationBuilder = GraphQLObjectType.newObject().name("MutationQuery");
    
    mutationBuilders.forEach(mb->mb.build(mutationBuilder));
    
    // build all types
    
    List<GraphQLType> types = typeBuilders.stream().map(tb->tb.build()).collect(Collectors.toList());
    
    // build the GraphQL schema
    
    GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
    
    if(queryBuilders.size() > 0) {
      schemaBuilder.query(queryBuilder);
    }
    
    if(mutationBuilders.size() > 0) {
      schemaBuilder.mutation(mutationBuilder);
    }
    
    return GraphQL.newGraphQL(schemaBuilder.additionalTypes(new HashSet<>(types))
                                           .build())
                  .build();
  }
  

  @Bean
  public SqlGraph sqlGraph (DataSource aDataSource) {
    return new SqlGraph(aDataSource);
  }
  
}
