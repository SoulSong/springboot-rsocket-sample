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
 

# Key Feature

 
# Test
See more details in the `server` and `client` modules.

## Request/Response 
> curl http://localhost:8080/user/1

## Request/Stream
> curl http://localhost:8080/user/

## Fire And Forget
> curl http://localhost:8080/user/add

## Request/Channel
> curl http://localhost:8080/user/request/channel

``
Another sample is the `pingpong` module. It implemented by rsocket native API. 
``

## Exception handler
> curl http://localhost:8080/user/error

