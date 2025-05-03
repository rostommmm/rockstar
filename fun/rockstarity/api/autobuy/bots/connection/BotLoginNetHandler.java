package fun.rockstarity.api.autobuy.bots.connection;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.secure.logger.SecureLogs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.client.CEncryptionResponsePacket;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SDisconnectLoginPacket;
import net.minecraft.network.login.server.SEnableCompressionPacket;
import net.minecraft.network.login.server.SEncryptionRequestPacket;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.CryptException;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HTTPUtil;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;


public class BotLoginNetHandler implements IClientLoginNetHandler
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft mc;
    @Nullable
    private final Screen previousGuiScreen;
    private final Consumer<ITextComponent> statusMessageConsumer;
    private final BotNetworkManager networkManager;
    private GameProfile gameProfile;

    public BotLoginNetHandler(BotNetworkManager networkManager2, Minecraft mcIn, @Nullable Screen previousScreen, Consumer<ITextComponent> statusMessageConsumerIn)
    {
        this.networkManager = networkManager2;
        this.mc = mcIn;
        this.previousGuiScreen = previousScreen;
        this.statusMessageConsumer = statusMessageConsumerIn;
    }

    public void handleEncryptionRequest(SEncryptionRequestPacket packetIn)
    {
        Cipher cipher;
        Cipher cipher1;
        String s;
        CEncryptionResponsePacket cencryptionresponsepacket;

        try
        {
            SecretKey secretkey = CryptManager.createNewSharedKey();
            PublicKey publickey = packetIn.getPublicKey();
            s = (new BigInteger(CryptManager.getServerIdHash(packetIn.getServerId(), publickey, secretkey))).toString(16);
            cipher = CryptManager.createNetCipherInstance(2, secretkey);
            cipher1 = CryptManager.createNetCipherInstance(1, secretkey);
            cencryptionresponsepacket = new CEncryptionResponsePacket(secretkey, publickey, packetIn.getVerifyToken());
        }
        catch (CryptException cryptexception)
        {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        this.statusMessageConsumer.accept(new TranslationTextComponent("connect.authorizing"));
        HTTPUtil.DOWNLOADER_EXECUTOR.submit(() ->
        {
            ITextComponent itextcomponent = this.joinServer(s);

            if (itextcomponent != null)
            {
                if (this.mc.getCurrentServerData() == null || !this.mc.getCurrentServerData().isOnLAN())
                {
                    this.networkManager.closeChannel(itextcomponent);
                    return;
                }

                LOGGER.warn(itextcomponent.getString());
            }

            this.statusMessageConsumer.accept(new TranslationTextComponent("connect.encrypting"));
            this.networkManager.sendPacket(cencryptionresponsepacket, (p_244776_3_) -> {
                this.networkManager.func_244777_a(cipher, cipher1);
            });
        });
    }

    @Nullable
    private ITextComponent joinServer(String serverHash)
    {
        try
        {
            this.getSessionService().joinServer(this.mc.getSession().getProfile(), this.mc.getSession().getToken(), serverHash);
            return null;
        }
        catch (AuthenticationUnavailableException authenticationunavailableexception)
        {
            return new TranslationTextComponent("disconnect.loginFailedInfo", new TranslationTextComponent("disconnect.loginFailedInfo.serversUnavailable"));
        }
        catch (InvalidCredentialsException invalidcredentialsexception)
        {
            return new TranslationTextComponent("disconnect.loginFailedInfo", new TranslationTextComponent("disconnect.loginFailedInfo.invalidSession"));
        }
        catch (InsufficientPrivilegesException insufficientprivilegesexception)
        {
            return new TranslationTextComponent("disconnect.loginFailedInfo", new TranslationTextComponent("disconnect.loginFailedInfo.insufficientPrivileges"));
        }
        catch (AuthenticationException authenticationexception)
        {
            return new TranslationTextComponent("disconnect.loginFailedInfo", authenticationexception.getMessage());
        }
    }

    private MinecraftSessionService getSessionService()
    {
        return this.mc.getSessionService();
    }

    public void handleLoginSuccess(SLoginSuccessPacket packetIn)
    {
        //this.statusMessageConsumer.accept(new TranslationTextComponent("connect.joining"));
        this.gameProfile = packetIn.getProfile();
        this.networkManager.setConnectionState(ProtocolType.PLAY);
        BotPlayNetHandler playHandler = new BotPlayNetHandler(this.mc, this.previousGuiScreen, this.networkManager, this.gameProfile);
        this.networkManager.playHandler = playHandler;
        this.networkManager.setNetHandler(playHandler);
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(ITextComponent reason)
    {
    	Chat.msg("РћС‚РєРѕРЅРµРєС‚РёР»Рѕ");
	
        if (this.previousGuiScreen != null && this.previousGuiScreen instanceof RealmsScreen)
        {
            this.mc.displayGuiScreen(new DisconnectedRealmsScreen(this.previousGuiScreen, DialogTexts.CONNECTION_FAILED, reason));
        }
        else
        {
            this.mc.displayGuiScreen(new DisconnectedScreen(this.previousGuiScreen, DialogTexts.CONNECTION_FAILED, reason));
        }
    }

    public void handleDisconnect(SDisconnectLoginPacket packetIn)
    {
    	Chat.msg("РћС‚РєРѕРЅРµРєС‚РёР»Рѕ");
	
        this.networkManager.closeChannel(packetIn.getReason());
    }

    public void handleEnableCompression(SEnableCompressionPacket packetIn)
    {
        if (!this.networkManager.isLocalChannel())
        {
            this.networkManager.setCompressionThreshold(packetIn.getCompressionThreshold());
        }
    }

    public void handleCustomPayloadLogin(SCustomPayloadLoginPacket packetIn)
    {
        this.statusMessageConsumer.accept(new TranslationTextComponent("connect.negotiating"));
        this.networkManager.sendPacket(new CCustomPayloadLoginPacket(packetIn.getTransaction(), (PacketBuffer)null));
    }

	@Override
	public NetworkManager getNetworkManager() {
		// TODO Auto-generated method stub
		return null;
	}
}
