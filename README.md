# Introduction
RSocket is a binary [protocol](https://github.com/rsocket/rsocket/blob/master/Protocol.md) for use on byte stream transports such as TCP, WebSockets, and Aeron.

It enables the following symmetric interaction models via async message passing over a single connection:

- request/response (stream of 1)
> This is probably the most common and familiar interaction model since HTTP also employs this type of communication.
>
> In this interaction model, the client initiates the communication and sends **a request**. Afterward, the server performs
 the operation and returns **a response** to the client – thus the communication completes.

- request/stream (finite stream of many)
> Request streaming is a more involved interaction model, where the client sends **a request** but gets **multiple 
 responses** over the course of time from the server.

- fire-and-forget (no response)
> As the name implies, the client sends **a request** to the server but **doesn't expect a response back**.

- channel (bi-directional streams)
> Send streams of messages in both directions.

All interaction models defined in **RSocket**.
 
# Architecture 
![avatar](./doc/img/request-track.jpg)

# Key Feature
- Four interaction models, handled with `@MessageMapping`
    - request/response (stream of 1)
    - request/stream (finite stream of many)
    - fire-and-forget (no response)
    - channel (bi-directional streams)
- Resume enhance
- RSocketStrategiesCustomizer enhance
- Metadata handle with `@Header` and `@Headers`
    - register different `MimeType` and `Class` by `metadataExtractorRegistry`
- Handle the placeholder in a destination template string
    - `@DestinationVariable`
- Handle the connection level payload included data and metadata with `@ConnectMapping`
    - match all connects
    - match the specific routes
- Switch the requester as a clientResponder by two different ways
    - see more in [ClientConfiguration](./client/src/main/java/com/shf/client/configuration/ClientConfiguration.java)
- Expose two ports in the same server for a webFlux server and a rSocket server
    - see more in [ClientApplication](./client/src/main/java/com/shf/client/ClientApplication.java)
- Integrate with spring-security for `basic authentication` and `authorization`
    - see more in [RSocketSecurityConfiguration](./client/src/main/java/com/shf/client/configuration/RSocketSecurityConfiguration.java)

# Test Endpoints

## Four interaction models

### Request/Response 
> curl http://localhost:8080/user/1

### Request/Stream
> curl http://localhost:8080/user/

### Fire And Forget
> curl http://localhost:8080/user/add

### Request/Channel
> curl http://localhost:8080/user/request/channel

``
Another sample is the `pingpong` module. It implemented by rsocket native API. 
``

## Exception handler
> curl http://localhost:8080/user/error

