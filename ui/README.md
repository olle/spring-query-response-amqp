# Query/Response UI

The Query/Response UI helps you gain insights and observe your system landscape
by providing rich information about how queries and responses are flowing
throughout the system, in real-time.

## Getting started

The Query/Response UI makes good use of the broker eco-system, in providing a
rich stream of information. It is also _eating its own dog food_, by using
Query/Response to gather information and allow users to interact directly.

### Running in a container

The Query/Response UI is available as a public published container image, from
the [ghcr.io] (Github Container Registry) as one of the available packages in
this project.

```
docker pull ghcr.io/olle/query-response-spring-amqp/query-response-ui:latest
```

To try it out start by ensuring there's a running AMQP broker in a network that
is accessible by the container.

```
docker network create --driver bridge qr-net
docker run -dit -p 15672:15672 --network qr-net --hostname broker rabbitmq:3-management
```

Check that the RabbitMQ broker is running, by visiting the management UI at
`http://localhost:15672`. Now you can start the Query/Response UI container,
connecting it to the network and broker.

```
docker run -dit -p 8080:8080 \
--network qr-net \
-e SPRING_RABBITMQ_HOST=broker \
ghcr.io/olle/query-response-spring-amqp/query-response-ui:latest
```

And browse to `http://localhost:8080`.

  [ghrc.io]: https://github.com/users/olle/packages/container/package/query-response-spring-amqp%2Fquery-response-ui

### Running locally

The simplest way to start the UI is to run it locally by issuing `make`. This
will start the Spring Boot® application. The Query/Response UI application is
served at http://localhost:8080 by default.

#### AMQP Broker Configuration

The default Spring AMQP configuration will attempt to connect to a local
broker on port 5672 with `guest/guest`. To use another RabbitMQ or other
credentials you can provide the command line arguments:

- `-Dspring.rabbitmq.host=hostname`
- `-Dspring.rabbitmq.username=user`
- `-Dspring.rabbitmq.password=password`

Or set the environment variables:

- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_USERNAME`
- `SPRING_RABBITMQ_PASSWORD`.

## Development

The Query/Response UI is intended to be a rather _thick client_ or _Single Page
Application (SPA)_. It is built with the [Vue] framework. It is easy to start
in development mode, using the `make dev` target. The Vue application is then
available at http://localhost:3000.

[vue]: https://vuejs.org

The SPA source files and modules can all be found under `src/main/web`.

### Spring Boot Backend

The client SPA is both served and supported by a Java, Spring Boot application.
The backend supports web sockets, and publishes updates to the client UI.

There is currently no command, RPC or REST-ful API in the backend.

Happy hacking!

### Architecture & Design

---

Spring Boot is a trademark of Pivotal Software, Inc. in the U.S. and other
countries.
