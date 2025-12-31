package com.ai.server.agent.core.metadata;


import java.util.List;


public interface DataModel {

    String[] getQualifiedName();

    String getName();

    List<Column> getColumns();
}
