package online.flowerinsnow.noimsecure.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import online.flowerinsnow.noimsecure.util.ReflectMirror;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class MixinServerLoginNetworkHandler {
    @Shadow
    GameProfile profile;
    @Shadow
    @Final
    MinecraftServer server;
    @Shadow
    private PlayerPublicKey.PublicKeyData publicKeyData;
    @Shadow
    @Final
    static Logger LOGGER = LogUtils.getLogger();
    @Shadow
    @Final
    public ClientConnection connection;
    @Shadow
    Object state;
    @Shadow
    private ServerPlayerEntity delayedPlayer;

    @Shadow
    protected abstract GameProfile toOfflineProfile(GameProfile profile);
    @Shadow
    private static PlayerPublicKey getVerifiedPublicKey(@Nullable PlayerPublicKey.PublicKeyData publicKeyData, UUID playerUuid, SignatureVerifier servicesSignatureVerifier, boolean shouldThrowOnMissingKey) throws TextifiedException {
        return null;
    }
    @Shadow
    public abstract void disconnect(Text reason);
    @Shadow
    protected abstract void addToServer(ServerPlayerEntity player);


    /**
     * @author flowerinsnow
     * @reason Cannot do this with inject.
     */
    @Overwrite
    public void acceptPlayer() {
        PlayerPublicKey playerPublicKey = null;
        if (!this.profile.isComplete()) {
            this.profile = this.toOfflineProfile(this.profile);
        } else {
            try {
                SignatureVerifier signatureVerifier = this.server.getServicesSignatureVerifier();
                playerPublicKey = getVerifiedPublicKey(this.publicKeyData, this.profile.getId(), signatureVerifier, this.server.shouldEnforceSecureProfile());
            } catch (TextifiedException var7) {
                LOGGER.error(var7.getMessage(), var7.getCause());
                if (!this.connection.isLocal()) {
                    this.disconnect(var7.getMessageText());
                    return;
                }
            }
        }

        Text text = this.server.getPlayerManager().checkCanJoin(this.connection.getAddress(), this.profile);
        if (text != null) {
            this.disconnect(text);
        } else {
            this.state = ReflectMirror.ServerLoginNetworkHandler.State.ACCEPTED;
            if (this.server.getNetworkCompressionThreshold() >= 0 && !this.connection.isLocal()) {
                this.connection.send(new LoginCompressionS2CPacket(this.server.getNetworkCompressionThreshold()), (channelFuture) ->
                    this.connection.setCompressionThreshold(this.server.getNetworkCompressionThreshold(), true)
                );
            }

            this.connection.send(new LoginSuccessS2CPacket(this.profile));
            ServerPlayerEntity serverPlayerEntity = this.server.getPlayerManager().getPlayer(this.profile.getId());

            try {
                ServerPlayerEntity serverPlayerEntity2 = this.server.getPlayerManager().createPlayer(this.profile, playerPublicKey);
                if (serverPlayerEntity != null) {
                    this.state = ReflectMirror.ServerLoginNetworkHandler.State.DELAY_ACCEPT;
                    this.delayedPlayer = serverPlayerEntity2;
                } else {
                    this.addToServer(serverPlayerEntity2);
                }
            } catch (Exception var6) {
                LOGGER.error("Couldn't place player in world", var6);
                Text text2 = Text.translatable("multiplayer.disconnect.invalid_player_data");
                this.connection.send(new DisconnectS2CPacket(text2));
                this.connection.disconnect(text2);
            }
        }
    }
}
