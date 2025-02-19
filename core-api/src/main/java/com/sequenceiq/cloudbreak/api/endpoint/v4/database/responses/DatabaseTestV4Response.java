package com.sequenceiq.cloudbreak.api.endpoint.v4.database.responses;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.Database;
import com.sequenceiq.common.model.JsonEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@NotNull
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseTestV4Response implements JsonEntity {

    @ApiModelProperty(value = Database.DATABASE_CONNECTION_TEST_RESULT, required = true)
    private String result;

    public DatabaseTestV4Response() {

    }

    public DatabaseTestV4Response(String connectionResult) {
        result = connectionResult;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
