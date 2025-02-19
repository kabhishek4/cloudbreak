package com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.base.RefreshClusterRecipeV4Base;

import io.swagger.annotations.ApiModel;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachRecipeV4Request extends RefreshClusterRecipeV4Base {

    @Override
    public String toString() {
        return "AttachRecipesV4Request{} " + super.toString();
    }
}
