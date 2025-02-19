/**
 * NOTE: This class is auto generated by the swagger code generator program (2.4.16).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.sequenceiq.mock.swagger.v45.api;

import com.sequenceiq.mock.swagger.model.ApiParcelList;
import com.sequenceiq.mock.swagger.model.ApiParcelUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2021-12-10T21:24:30.629+01:00")

@Api(value = "ParcelsResource", description = "the ParcelsResource API")
@RequestMapping(value = "/{mockUuid}/api/v45")
public interface ParcelsResourceApi {

    Logger log = LoggerFactory.getLogger(ParcelsResourceApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Retrieve details parcel usage information for the cluster.", nickname = "getParcelUsage", notes = "Retrieve details parcel usage information for the cluster. This describes which processes, roles and hosts are using which parcels.", response = ApiParcelUsage.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "ParcelsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "The parcel usage information.", response = ApiParcelUsage.class) })
    @RequestMapping(value = "/clusters/{clusterName}/parcels/usage",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiParcelUsage> getParcelUsage(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "",required=true) @PathVariable("clusterName") String clusterName) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"racks\" : [ {    \"hosts\" : [ {      \"hostRef\" : { },      \"roles\" : [ { }, { } ]    }, {      \"hostRef\" : { },      \"roles\" : [ { }, { } ]    } ],    \"rackId\" : \"...\"  }, {    \"hosts\" : [ {      \"hostRef\" : { },      \"roles\" : [ { }, { } ]    }, {      \"hostRef\" : { },      \"roles\" : [ { }, { } ]    } ],    \"rackId\" : \"...\"  } ],  \"parcels\" : [ {    \"parcelRef\" : {      \"clusterName\" : \"...\",      \"parcelName\" : \"...\",      \"parcelVersion\" : \"...\",      \"parcelDisplayName\" : \"...\"    },    \"processCount\" : 12345,    \"activated\" : true  }, {    \"parcelRef\" : {      \"clusterName\" : \"...\",      \"parcelName\" : \"...\",      \"parcelVersion\" : \"...\",      \"parcelDisplayName\" : \"...\"    },    \"processCount\" : 12345,    \"activated\" : true  } ]}", ApiParcelUsage.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default ParcelsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    @ApiOperation(value = "Lists all parcels that the cluster has access to.", nickname = "readParcels", notes = "Lists all parcels that the cluster has access to.", response = ApiParcelList.class, authorizations = {
        @Authorization(value = "basic")
    }, tags={ "ParcelsResource", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of parcels.", response = ApiParcelList.class) })
    @RequestMapping(value = "/clusters/{clusterName}/parcels",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    default ResponseEntity<ApiParcelList> readParcels(@ApiParam(value = "The unique id of CB cluster (works in CB test framework only)",required=true) @PathVariable("mockUuid") String mockUuid,@ApiParam(value = "",required=true) @PathVariable("clusterName") String clusterName,@ApiParam(value = "", allowableValues = "EXPORT, EXPORT_REDACTED, FULL, FULL_WITH_HEALTH_CHECK_EXPLANATION, SUMMARY", defaultValue = "summary") @Valid @RequestParam(value = "view", required = false, defaultValue="summary") String view) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
            if (getAcceptHeader().get().contains("application/json")) {
                try {
                    return new ResponseEntity<>(getObjectMapper().get().readValue("{  \"items\" : [ {    \"product\" : \"...\",    \"version\" : \"...\",    \"stage\" : \"...\",    \"state\" : {      \"progress\" : 12345,      \"totalProgress\" : 12345,      \"count\" : 12345,      \"totalCount\" : 12345,      \"errors\" : [ \"...\", \"...\" ],      \"warnings\" : [ \"...\", \"...\" ]    },    \"clusterRef\" : {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    },    \"displayName\" : \"...\",    \"description\" : \"...\"  }, {    \"product\" : \"...\",    \"version\" : \"...\",    \"stage\" : \"...\",    \"state\" : {      \"progress\" : 12345,      \"totalProgress\" : 12345,      \"count\" : 12345,      \"totalCount\" : 12345,      \"errors\" : [ \"...\", \"...\" ],      \"warnings\" : [ \"...\", \"...\" ]    },    \"clusterRef\" : {      \"clusterName\" : \"...\",      \"displayName\" : \"...\"    },    \"displayName\" : \"...\",    \"description\" : \"...\"  } ]}", ApiParcelList.class), HttpStatus.NOT_IMPLEMENTED);
                } catch (IOException e) {
                    log.error("Couldn't serialize response for content type application/json", e);
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default ParcelsResourceApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
