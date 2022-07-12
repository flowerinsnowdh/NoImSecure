package online.flowerinsnow.noimsecure.eci;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;

/**
 * When returning "SUCCESS", means "cancelled"
 */
public interface PlayerPublicKeyVerfyingCallback {
    Event<PlayerPublicKeyVerfyingCallback> EVENT = EventFactory.createArrayBacked(PlayerPublicKeyVerfyingCallback.class,
            (listeners) -> ((server, profile, publicKeyData, connection) -> {
                for (PlayerPublicKeyVerfyingCallback listener : listeners) {
                    ActionResult result = listener.interact(server, profile, publicKeyData, connection);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }));

    ActionResult interact(MinecraftServer server, GameProfile profile, PlayerPublicKey.PublicKeyData publicKeyData, ClientConnection connection);
}
