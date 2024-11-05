package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.gameserver.redis.RedisRepositoryService;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends BinaryWebSocketHandler {

    private static final String CLIENT_ID_ATTRIBUTE = "clientId";

    private final Logger logger = LoggerFactory.getLogger(GameSocketHandler.class);

    @Autowired
    private RedisRepositoryService redisRepositoryService;

    @Autowired
    private SbMessageHandler messageHandler;

    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public Collection<UUID> getSessions() {
        return sessions.keySet();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        // Validate session has clientId
        if (clientId == null) {
            logger.warn("Received packet on session {} without {}", session.getId(), CLIENT_ID_ATTRIBUTE);
            return;
        }

        // Validate that session is allowed to communicate with this gameserver
        if (!redisRepositoryService.hasClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            logger.warn("Received packet from unconfirmed session {}", session.getId());
            return;
        }

        // Deserialize packet using protobuf
        byte[] payload = message.getPayload().array();
        SbMessage sbMessage = SbMessage.parseFrom(payload);

        // Execute
        messageHandler.execute(sbMessage, clientId);
    }

    public void sendCbMessage(UUID clientId, CbMessage message) {
        try {
            WebSocketSession session = sessions.get(clientId);
            if (session == null) {
                throw new IllegalArgumentException("Cannot send message to invalid client " + clientId.toString());
            }
            byte[] messageBytes = message.toByteArray();
            session.sendMessage(new BinaryMessage(messageBytes));
            logger.debug("Sending client {} message of type {}", clientId.toString(), message.getPayloadCase().name());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send client " + clientId.toString() + " packet of type " + message.getPayloadCase().name(), exception);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Validate that the client has been assigned to us
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        if (clientId == null || !redisRepositoryService.hasClientSession(clientId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessions.put(clientId, session);
        logger.info("Initiated connection with client {}", clientId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Validate that the client had a connection with us before deleting the session
        UUID clientId = redisRepositoryService.parseClientId(session.getAttributes().get(CLIENT_ID_ATTRIBUTE));
        if (clientId != null && redisRepositoryService.hasClientSession(clientId)) {
            redisRepositoryService.removeClientSession(clientId);
        }
        sessions.remove(clientId);
        logger.info("Closed connection with client {} for reason {}", clientId, status);
    }

}