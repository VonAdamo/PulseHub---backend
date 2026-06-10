package com.pulsehub.userservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserGrpcServer implements SmartLifecycle {

    private final int port;
    private final UserGrpcEndpoint userGrpcEndpoint;
    private Server server;
    private boolean running;

    public UserGrpcServer(
            @Value("${pulsehub.grpc.server.port}") int port,
            UserGrpcEndpoint userGrpcEndpoint
    ) {
        this.port = port;
        this.userGrpcEndpoint = userGrpcEndpoint;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            server = ServerBuilder.forPort(port)
                    .addService(userGrpcEndpoint)
                    .build()
                    .start();
            running = true;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start user-service gRPC server on port " + port, exception);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
