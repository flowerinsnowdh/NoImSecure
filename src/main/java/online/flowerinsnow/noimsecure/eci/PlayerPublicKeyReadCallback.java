package online.flowerinsnow.noimsecure.eci;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.util.ActionResult;

/**
 * When returning "SUCCESS", means "cancelled"
 */
public interface PlayerPublicKeyReadCallback {
    Event<PlayerPublicKeyReadCallback> EVENT = EventFactory.createArrayBacked(PlayerPublicKeyReadCallback.class,
            (listeners) -> ((packet) -> {
                for (PlayerPublicKeyReadCallback listener : listeners) {
                    ActionResult result = listener.interact(packet);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }));

    ActionResult interact(LoginHelloC2SPacket packet);
}
