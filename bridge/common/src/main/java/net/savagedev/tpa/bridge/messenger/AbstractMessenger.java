package net.savagedev.tpa.bridge.messenger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.savagedev.tpa.bridge.BungeeTpBridgePlatform;
import net.savagedev.tpa.bridge.model.BungeeTpPlayer;
import net.savagedev.tpa.common.messaging.ChannelConstants;
import net.savagedev.tpa.common.messaging.Messenger;
import net.savagedev.tpa.common.messaging.messages.Message;
import net.savagedev.tpa.common.messaging.messages.MessageRequestTeleport;
import net.savagedev.tpa.common.messaging.messages.MessageRequestTeleport.Type;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractMessenger<T> implements Messenger<T> {
    private static final Map<String, Function<JsonObject, Message>> DECODER_FUNCTIONS = new HashMap<>();

    static {
        DECODER_FUNCTIONS.put(MessageRequestTeleport.class.getSimpleName(), MessageRequestTeleport::deserialize);
    }

    protected static final String CHANNEL = ChannelConstants.CHANNEL_NAME;

    private final BungeeTpBridgePlatform platform;

    public AbstractMessenger(BungeeTpBridgePlatform platform) {
        this.platform = platform;
    }

    @Override
    public void handleIncomingMessage(String channel, byte[] bytes) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        try (final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             final DataInputStream dataStream = new DataInputStream(byteStream)) {
            final JsonElement element = JsonParser.parseString(dataStream.readUTF());
            if (element.isJsonNull() || !element.isJsonObject()) {
                return;
            }

            final JsonObject object = element.getAsJsonObject();
            if (object.isJsonNull()) {
                return;
            }

            final String messageType = object.get("message_type").getAsString();
            if (messageType == null) {
                return;
            }

            final JsonObject messageObject = object.get("message").getAsJsonObject();
            if (messageObject.isJsonNull()) {
                return;
            }

            final Message message = DECODER_FUNCTIONS.get(messageType).apply(messageObject);
            if (message == null) {
                return;
            }

            if (message instanceof MessageRequestTeleport) {
                this.handleTeleportRequest((MessageRequestTeleport) message);
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }

    private void handleTeleportRequest(MessageRequestTeleport request) {
        if (request == null) {
            return;
        }

        final UUID requesterId = request.getRequester();
        final BungeeTpPlayer receiver = this.platform.getBungeeTpPlayer(request.getReceiver());

        if (request.getType() == Type.INSTANT) {
            final BungeeTpPlayer requester = this.platform.getBungeeTpPlayer(requesterId);
            requester.teleportTo(receiver);
        } else {
            this.platform.getTpCache().put(requesterId, receiver.getUniqueId());
        }
    }

    @Override
    public void sendData(Message message) {
        this.sendData(null, message);
    }
}
