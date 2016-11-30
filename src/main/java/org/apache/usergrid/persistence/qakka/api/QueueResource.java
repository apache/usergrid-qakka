/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.usergrid.persistence.qakka.api;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import org.apache.usergrid.persistence.qakka.URIStrategy;
import org.apache.usergrid.persistence.qakka.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Api(value="/queues", description = "Queue management, send, get and ack.")
@Path("queues")
public class QueueResource {
    private static final Logger logger = LoggerFactory.getLogger( QueueResource.class );

    private final QueueManager        queueManager;
    private final QueueMessageManager queueMessageManager;
    private final URIStrategy uriStrategy;
    private final Regions             regions;


    @Inject
    public QueueResource(
            QueueManager              queueManager,
            QueueMessageManager       queueMessageManager,
            URIStrategy uriStrategy,
            Regions                   regions ) {

        this.queueManager              = queueManager;
        this.queueMessageManager       = queueMessageManager;
        this.uriStrategy               = uriStrategy;
        this.regions                   = regions;

        logger.info("Constructed");
    }


    @ApiOperation(value = "Create new queue.", response=ApiResponse.class)
    @ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 400, 
                message = "No Queue object posted, or name field is missing"),
    })
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createQueue(Queue queue ) throws Exception {

        Preconditions.checkArgument(queue != null, "Queue configuration is required");
        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queue.getName()), "Queue name is required");

        queueManager.createQueue(queue);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setQueues( Collections.singletonList(queue) );
        return Response.created( uriStrategy.queueURI( queue.getName() )).entity(apiResponse).build();
    }


    @ApiOperation(value = "Update Queue configuration.", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "No Queue object posted, or name field is missing"),
    })
    @PUT
    @Path( "{queueName}/config" )
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateQueueConfig( @PathParam("queueName") String queueName, Queue queue) {

        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queueName), "Queue name is required");
        Preconditions.checkArgument(queue != null, "Queue configuration is required");

        queue.setName(queueName);
        queueManager.updateQueueConfig(queue);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setQueues( Collections.singletonList(queue) );
        return Response.ok().entity(apiResponse).build();
    }


    @ApiOperation(value = "Delete Queue.", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Queue name or confirm flag missing."),
    })
    @DELETE
    @Path( "{queueName}" )
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteQueue( @PathParam("queueName") String queueName,
                                 @QueryParam("confirm") @DefaultValue("false") Boolean confirmedParam) {

        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queueName), "Queue name is required");
        Preconditions.checkArgument(confirmedParam != null, "Confirm parameter is required");

        ApiResponse apiResponse = new ApiResponse();

        if ( confirmedParam ) {
            queueManager.deleteQueue( queueName );
            return Response.ok().entity( apiResponse ).build();
        }

        apiResponse.setMessage( "confirm parameter must be true" );
        return Response.status( Response.Status.BAD_REQUEST ).entity( apiResponse ).build();
    }


    @ApiOperation(value = "Get Queue config.", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Queue name or confirm flag missing."),
    })
    @GET
    @Path( "{queueName}/config" )
    @Produces({MediaType.APPLICATION_JSON})
    public Response getQueueConfig(
            @ApiParam(value = "Name of Queue", required = true) @PathParam("queueName") String queueName) {

        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queueName), "Queue name is required");

        ApiResponse apiResponse = new ApiResponse();
        Queue queue = queueManager.getQueueConfig( queueName );
        if ( queue != null ) {
            apiResponse.setQueues( Collections.singletonList(queue) );
            return Response.ok().entity(apiResponse).build();
        }
        return Response.status( Response.Status.NOT_FOUND ).build();
    }


    @ApiOperation(value = "Get list of all Queues.", response=ApiResponse.class)
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> getListOfQueues() {

        // TODO: create design to handle large number of queues, e.g. paging and/or hierarchy of queues
        // TODO: create design to support multi-tenant usage, authentication, etc.
        return queueManager.getListOfQueues();
    }


//    @GET
//    @Path( "{queueName}/stats" )
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response getQueueStats(
//            @ApiParam(value = "Name of Queue", required = true) @PathParam("queueName") String queueName) 
//        throws Exception {
//        
//        // TODO: implement GET /queues/{queueName}/stats
//        throw new UnsupportedOperationException();
//    }


    Long convertDelayParameter(String delayParam) {
        Long delayMs = 0L;
        if (!QakkaUtils.isNullOrEmpty(delayParam)) {
            switch (delayParam.toUpperCase()) {
                case "NONE":
                case "":
                    delayMs = 0L;
                    break;
                default:
                    try {
                        delayMs = Long.parseLong(delayParam);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid delay parameter");
                    }
                    break;
            }
        }
        return delayMs;
    }

    Long convertExpirationParameter(String expirationParam) throws IllegalArgumentException {
        Long expirationSecs = null;
        if (!QakkaUtils.isNullOrEmpty(expirationParam)) {
            switch (expirationParam.toUpperCase()) {
                case "NEVER":
                case "":
                    expirationSecs = null;
                    break;
                default:
                    try {
                        expirationSecs = Long.parseLong(expirationParam);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid expiration parameter");
                    }
                    break;
            }
        }
        return expirationSecs;
    }


    /**
     * Send a queue message with a JSON payload.
     *
     * @param queueName         Name of queue to target (queue must exist)
     * @param regionsParam      Comma-separated list of regions to send to
     * @param delayParam        Delay (ms) before sending message (not yet supported)
     * @param expirationParam   Time (ms) after which message will expire (not yet supported)
     * @param messageBody       JSON payload in string form
     */
    @ApiOperation(value = "Send Queue Message with a JSON payload.", response=ApiResponse.class) 
    @POST
    @Path( "{queueName}/messages" )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessageJson(
            
            @ApiParam(value = "Name of Queue", required = true)
                @PathParam("queueName") String queueName,

            @ApiParam(value = "Regions to which message is to be sent", required = false)
                @QueryParam("regions" ) @DefaultValue("") String regionsParam,

            @QueryParam("delay")      @DefaultValue("") String delayParam,
            @QueryParam("expiration") @DefaultValue("") String expirationParam,
            
            @ApiParam(value = "Data to be send with Queue Message", required = true)                                               String messageBody) 
            
            throws Exception {

        return sendMessage( queueName, regionsParam, delayParam, expirationParam,
                MediaType.APPLICATION_JSON, ByteBuffer.wrap( messageBody.getBytes() ) );
    }


    /**
     * Send a queue message with a binary data payload.
     *
     * @param queueName         Name of queue to target (queue must exist)
     * @param regionsParam      Comma-separated list of regions to send to
     * @param delayParam        Delay (ms) before sending message (not yet supported)
     * @param expirationParam   Time (ms) after which message will expire (not yet supported)
     * @param actualContentType Content type of messageBody data (if not application/octet-stream)
     * @param messageBody       Binary data that is the payload of the queue message
     */
    @ApiOperation(value = "Send Queue Message with a binary data (blob) payload.", response=ApiResponse.class) 
    @POST
    @Path( "{queueName}/messages" )
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessageBinary(
            
            @ApiParam(value = "Name of Queue", required = true) 
                @PathParam("queueName") String queueName,
            
            @ApiParam(value = "Regions to which message is to be sent", required = false)    
                @QueryParam("regions" ) @DefaultValue("") String regionsParam,
            
            @QueryParam("delay") @DefaultValue("") String delayParam,
            @QueryParam("expiration") @DefaultValue("") String expirationParam,
            
            @ApiParam(value = "Content type of the data to be sent with Queue Message", required = true) 
                @QueryParam("contentType") String actualContentType,
            
            @ApiParam(value = "Data to be send with Queue Message", required = true) 
                byte[] messageBody) 
            
            throws Exception { 
        
        String contentType = actualContentType != null ? actualContentType : MediaType.APPLICATION_OCTET_STREAM;

        return sendMessage( queueName, regionsParam, delayParam, expirationParam,
                contentType, ByteBuffer.wrap( messageBody ) );
    }


    private Response sendMessage( String queueName,
                                   String regionsParam,
                                   String delayParam,
                                   String expirationParam,
                                   String contentType,
                                   ByteBuffer byteBuffer) {

            if ( queueManager.getQueueConfig( queueName ) == null ) {
                throw new NotFoundException( "Queue " + queueName + " not found" ) ;
            }

            Preconditions.checkArgument( !QakkaUtils.isNullOrEmpty( queueName ), "Queue name is required" );

            // if regions, delay or expiration are empty string, would get the defaults from the queue
            if (regionsParam.equals( "" )) {
                regionsParam = Regions.LOCAL;
            }

            Long delayMs = convertDelayParameter( delayParam );

            Long expirationSecs = convertExpirationParameter( expirationParam );

            List<String> regionList = regions.getRegions( regionsParam );

            queueMessageManager.sendMessages( queueName, regionList, delayMs, expirationSecs,
                    contentType, byteBuffer );

            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCount( 1 );
            return Response.ok().entity( apiResponse ).build();

    }


    @ApiOperation(value = "Get next Queue Messages from a Queue", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid count parameter"),
    })
    @GET
    @Path( "{queueName}/messages" )
    @Produces({MediaType.APPLICATION_JSON})
    public Response getNextMessages(
            
            @ApiParam(value = "Name of Queue", required = false)
                @PathParam("queueName") String queueName,
            
            @ApiParam(value = "Number of messages to get", required = false)
                @QueryParam("count") @DefaultValue("1") String countParam) 
            
            throws Exception {

        Preconditions.checkArgument( !QakkaUtils.isNullOrEmpty( queueName ), "Queue name is required" );

        int count = 1;
        try {
            count = Integer.parseInt( countParam );
        } catch (Exception e) {
            throw new IllegalArgumentException( "Invalid count parameter" );
        }
        if (count <= 0) {
            // invalid count
            throw new IllegalArgumentException( "Count must be >= 1" );
        }

        List<QueueMessage> messages = queueMessageManager.getNextMessages( queueName, count );

        ApiResponse apiResponse = new ApiResponse();

        if (messages != null && !messages.isEmpty()) {
            apiResponse.setQueueMessages( messages );

        } else { // always return queueMessages field
            apiResponse.setQueueMessages( Collections.EMPTY_LIST );
        }
        apiResponse.setCount( apiResponse.getQueueMessages().size() );
        return Response.ok().entity( apiResponse ).build();
    }


    @ApiOperation(value = "Acknowledge that Queue Message has been processed.", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, 
                    message = "Queue Message ID invalid, or message not in-flight"),
    })
    @DELETE
    @Path( "{queueName}/messages/{queueMessageId}" )
    @Produces({MediaType.APPLICATION_JSON})
    public Response ackMessage(
            
            @ApiParam(value = "Name of Queue", required = true)
                @PathParam("queueName") String queueName,
            
            @ApiParam(value = "ID of Queue Message to be acknowledged", required = true)
                @PathParam("queueMessageId") String queueMessageId) 
            
            throws Exception {

        Preconditions.checkArgument( !QakkaUtils.isNullOrEmpty( queueName ), "Queue name is required" );

        UUID messageUuid;
        try {
            messageUuid = UUID.fromString( queueMessageId );
        } catch (Exception e) {
            throw new IllegalArgumentException( "Invalid queue message UUID" );
        }
        queueMessageManager.ackMessage( queueName, messageUuid );

        ApiResponse apiResponse = new ApiResponse();
        return Response.ok().entity( apiResponse ).build();
    }


    @ApiOperation(value = "Get data associated with a Queue Message.", response=ApiResponse.class)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 400, message = "Message ID invalid"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Queue Message or data not found")
    })
    @GET
    @Path( "{queueName}/data/{queueMessageId}" )
    public Response getMessageData(
            
            @ApiParam(value = "Name of Queue", required = true)
                @PathParam("queueName") String queueName,

            @ApiParam(value = "ID of Queue Message for which data is to be returned", required = true)
                @PathParam("queueMessageId") String queueMessageIdParam ) {

        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queueName), "Queue name is required");

        UUID queueMessageId;
        try {
            queueMessageId = UUID.fromString(queueMessageIdParam);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid queue message UUID");
        }

        QueueMessage message = queueMessageManager.getMessage( queueName, queueMessageId );
        if ( message == null ) {
            throw new NotFoundException(
                    "Message not found for queueName: " + queueName + " queue message id: " + queueMessageId );
        }

        ByteBuffer messageData = queueMessageManager.getMessageData( message.getMessageId() );
        if ( messageData == null ) {
            throw new NotFoundException( "Message data not found queueName: " + queueName
                    + " queue message id: " + queueMessageId + " message id: " + message.getMessageId() );
        }

        ByteBufferBackedInputStream input = new ByteBufferBackedInputStream( messageData );

        StreamingOutput stream = output -> {
            try {
                ByteStreams.copy(input, output);
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };

        return Response.ok( stream ).header( "Content-Type", message.getContentType() ).build();
    }


//    @PUT
//    @Path( "{queueName}/messages/{queueMessageId}" )
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response requeueMessage( @PathParam("queueName") String queueName,
//                                    @PathParam("queueMessageId") String queueMessageIdParam,
//                                    @QueryParam("delay") @DefaultValue("") String delayParam) throws Exception {
//
//        Preconditions.checkArgument(!QakkaUtils.isNullOrEmpty(queueName), "Queue name is required");
//
//        UUID queueMessageId;
//        try {
//            queueMessageId = UUID.fromString(queueMessageIdParam);
//        }
//        catch (Exception e) {
//            throw new IllegalArgumentException("Invalid message UUID");
//        }
//        Long delayMs = convertDelayParameter(delayParam);
//
//        queueMessageManager.requeueMessage(queueName, queueMessageId, delayMs);
//
//        ApiResponse apiResponse = new ApiResponse();
//        return Response.ok().entity(apiResponse).build();
//    }
//
//
//    @DELETE
//    @Path( "{queueName}/messages" )
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response clearMessages( @PathParam("queueName") String queueName,
//                                   @QueryParam("confirm") @DefaultValue("false") Boolean confirmed) throws Exception {
//
//        // TODO: implement DELETE /queues/{queueName}/messages"
//        throw new UnsupportedOperationException();
//    }

}
