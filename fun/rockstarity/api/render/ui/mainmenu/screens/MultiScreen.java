package fun.rockstarity.api.render.ui.mainmenu.screens;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.proxy.GuiProxy;
import fun.rockstarity.api.helpers.game.proxy.ProxyServer;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.ServerCustomList;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.via.ui.VersionSelectorElement;
import lombok.Getter;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerListScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.LanServerInfo;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
public class MultiScreen extends Screen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerPinger oldServerPinger = new ServerPinger();
    private final Screen parentScreen;
    public ServerCustomList serverListSelector;
    private ServerList savedServerList;
    private Button btnEditServer;
    private Button btnSelectServer;
    private Button btnDeleteServer;
    private List<ITextComponent> hoveringText;
    private ServerData selectedServer;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.LanServerFindThread lanServerDetector;
    private boolean initialized;
    private final VersionSelectorElement viaScreen = rock.getVia().viaScreen;
    public static boolean KOSTIL;
    
    public MultiScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("multiplayer.title"));
        this.parentScreen = parentScreen;
    }

    protected void init()
    {
    	KOSTIL = true;
        super.init();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        int height = (int) (sr.getScaledHeight()/1.33F);

        if (this.initialized)
        {
            this.serverListSelector.updateSize(this.width, this.height, (int) (this.height/2 - height/2 - sr.getScaledHeight()/12.73F + 10), height+10);
        }
        else
        {
            this.initialized = true;
            this.savedServerList = new ServerList(this.minecraft);
            this.savedServerList.loadServerList();
            this.lanServerList = new LanServerDetector.LanServerList();

            try
            {
                this.lanServerDetector = new LanServerDetector.LanServerFindThread(this.lanServerList);
                this.lanServerDetector.start();
            }
            catch (Exception exception)
            {
                LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
            }

            this.serverListSelector = new ServerCustomList(this, this.minecraft, this.width, this.height, (int) (this.height/2 - height/2 - sr.getScaledHeight()/12.73F + 10), height+10, 38);
            this.serverListSelector.updateOnlineServers(this.savedServerList);
        }
        serverListSelector.func_244605_b(false);
        serverListSelector.func_244606_c(false);
        serverListSelector.setRenderHeader(false, 0);
        
        if (!this.rock.isPanic()) this.addButton(this.rock.getVia().viaScreen);
        
        int offset = 40;

        this.children.add(this.serverListSelector);
        this.btnSelectServer = this.addButton(new Button(this.width / 2 - 154, this.height - 52 - offset, 100, 20, new TranslationTextComponent("selectServer.select"), (p_214293_1_) ->
        {
            this.connectToSelected();
        }));
        this.addButton(new Button(this.width / 2 - 50, this.height - 52 - offset, 100, 20, new TranslationTextComponent("selectServer.direct"), (p_214286_1_) ->
        {
            this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false);
            this.minecraft.displayGuiScreen(new ServerListScreen(this, this::func_214290_d, this.selectedServer));
        }));
        this.addButton(new Button(this.width / 2 + 4 + 50, this.height - 52 - offset, 100, 20, new TranslationTextComponent("selectServer.add"), (p_214288_1_) ->
        {
            this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false);
            this.minecraft.displayGuiScreen(new AddServerScreen(this, this::func_214284_c, this.selectedServer));
        }));
        this.btnEditServer = this.addButton(new Button(this.width / 2 - 154, this.height - 28 - offset, 70, 20, new TranslationTextComponent("selectServer.edit"), (p_214283_1_) ->
        {
            ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

            if (ServerCustomList$entry instanceof ServerCustomList.NormalEntry entry)
            {
            	if (entry.getServerData().serverIP.equalsIgnoreCase("roc.metahvh.space") || entry.getServerData().serverIP.equalsIgnoreCase("rk.bravohvh.fun")) return;
                ServerData serverdata = ((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData();
                this.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
                this.selectedServer.copyFrom(serverdata);
                this.minecraft.displayGuiScreen(new AddServerScreen(this, this::func_214292_b, this.selectedServer));
            }
        }));
        this.btnDeleteServer = this.addButton(new Button(this.width / 2 - 74, this.height - 28 - offset, 70, 20, new TranslationTextComponent("selectServer.delete"), (p_214294_1_) ->
        {
            ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

            if (ServerCustomList$entry instanceof ServerCustomList.NormalEntry entry)
            {
            	if (entry.getServerData().serverIP.equalsIgnoreCase("roc.metahvh.space") || entry.getServerData().serverIP.equalsIgnoreCase("rk.bravohvh.fun")) return;
                String s = ((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData().serverName;

                if (s != null)
                {
                    ITextComponent itextcomponent = new TranslationTextComponent("selectServer.deleteQuestion");
                    ITextComponent itextcomponent1 = new TranslationTextComponent("selectServer.deleteWarning", s);
                    ITextComponent itextcomponent2 = new TranslationTextComponent("selectServer.deleteButton");
                    ITextComponent itextcomponent3 = DialogTexts.GUI_CANCEL;
                    this.minecraft.displayGuiScreen(new ConfirmScreen(this::func_214285_a, itextcomponent, itextcomponent1, itextcomponent2, itextcomponent3));
                }
            }
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height - 28 - offset, 70, 20, new TranslationTextComponent("selectServer.refresh"), (p_214291_1_) ->
        {
            this.refreshServerList();
        }));
        this.addButton(new Button(this.width / 2 + 4 + 76, this.height - 28 - offset, 75, 20, DialogTexts.GUI_CANCEL, (p_214289_1_) ->
        {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
        this.func_214295_b();
    }

    public void tick()
    {
        super.tick();
        if (!this.rock.isPanic()) this.rock.getVia().viaScreen.tick();
        if (this.lanServerList.getWasUpdated())
        {
            List<LanServerInfo> list = this.lanServerList.getLanServers();
            this.lanServerList.setWasNotUpdated();
            this.serverListSelector.updateNetworkServers(list);
        }

        this.oldServerPinger.pingPendingNetworks();
    }

    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);

        if (this.lanServerDetector != null)
        {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }

        this.oldServerPinger.clearPendingNetworks();
    }

    private void refreshServerList()
    {
        int height = (int) (sr.getScaledHeight()/1.33F);

    	this.initialized = true;
        this.savedServerList = new ServerList(this.minecraft);
        this.savedServerList.loadServerList();
        this.lanServerList = new LanServerDetector.LanServerList();

        try
        {
            this.lanServerDetector = new LanServerDetector.LanServerFindThread(this.lanServerList);
            this.lanServerDetector.start();
        }
        catch (Exception exception)
        {
            LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
        }

        this.serverListSelector = new ServerCustomList(this, this.minecraft, this.width, this.height, (int) (this.height/2 - height/2 - sr.getScaledHeight()/12.73F + 10), height+10, 38);
        this.serverListSelector.updateOnlineServers(this.savedServerList);
        serverListSelector.func_244605_b(false);
        serverListSelector.func_244606_c(false);
        serverListSelector.setRenderHeader(false, 0);
        mc.displayGuiScreen(Page.MULTI.getScreen());
    }

    private void func_214285_a(boolean p_214285_1_)
    {
        ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

        if (p_214285_1_ && ServerCustomList$entry instanceof ServerCustomList.NormalEntry)
        {
            if (((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData().serverIP.equalsIgnoreCase("mc.bingohvh.ru")) {
                this.minecraft.displayGuiScreen(this);
                return;
            }
            this.savedServerList.func_217506_a(((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData());
            this.savedServerList.saveServerList();
            this.serverListSelector.setSelected((ServerCustomList.Entry)null);
            this.serverListSelector.updateOnlineServers(this.savedServerList);
        }

        this.minecraft.displayGuiScreen(this);
    }

    private void func_214292_b(boolean p_214292_1_)
    {
        ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

        if (p_214292_1_ && ServerCustomList$entry instanceof ServerCustomList.NormalEntry)
        {
            ServerData serverdata = ((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData();
            serverdata.serverName = this.selectedServer.serverName;
            serverdata.serverIP = this.selectedServer.serverIP;
            serverdata.copyFrom(this.selectedServer);
            this.savedServerList.saveServerList();
            this.serverListSelector.updateOnlineServers(this.savedServerList);
        }

        this.minecraft.displayGuiScreen(this);
    }

    private void func_214284_c(boolean p_214284_1_)
    {
        if (p_214284_1_)
        {
            this.savedServerList.addServerData(this.selectedServer);
            this.savedServerList.saveServerList();
            this.serverListSelector.setSelected((ServerCustomList.Entry)null);
            this.serverListSelector.updateOnlineServers(this.savedServerList);
        }

        this.minecraft.displayGuiScreen(this);
    }

    private void func_214290_d(boolean p_214290_1_)
    {
        if (p_214290_1_)
        {
            this.connectToServer(this.selectedServer);
        }
        else
        {
            this.minecraft.displayGuiScreen(this);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
		rock.getMenuManager().keyPressed(keyCode, scanCode, modifiers);

        if (super.keyPressed(keyCode, scanCode, modifiers))
        {
            return true;
        }
        else if (keyCode == 294)
        {
            this.refreshServerList();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

            if (ServerCustomList$entry instanceof ServerCustomList.NormalEntry)
            {
                String s = ((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData().serverName;

                if (s != null)
                {
                    ITextComponent itextcomponent = new TranslationTextComponent("selectServer.deleteQuestion");
                    ITextComponent itextcomponent1 = new TranslationTextComponent("selectServer.deleteWarning", s);
                    ITextComponent itextcomponent2 = new TranslationTextComponent("selectServer.deleteButton");
                    ITextComponent itextcomponent3 = DialogTexts.GUI_CANCEL;
                    this.minecraft.displayGuiScreen(new ConfirmScreen(this::func_214285_a, itextcomponent, itextcomponent1, itextcomponent2, itextcomponent3));
                }
            }
        	return super.keyPressed(keyCode, scanCode, modifiers);
        }
        else if (this.serverListSelector.getSelected() != null)
        {
            if (keyCode != 257 && keyCode != 335)
            {
                return this.serverListSelector.keyPressed(keyCode, scanCode, modifiers);
            }
            else
            {
                this.connectToSelected();
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.viaScreen.mouseClicked(mouseX, mouseY, button);
		rock.getMenuManager().clicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
    	FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();
		FixColor text = rock.getThemes().getTextFirstColor();
		FixColor textSecond = rock.getThemes().getTextSecondColor();
		Animation alphaAnim = rock.getMenuManager().getAlphaAnim();
		
		alphaAnim.setForward(rock.getMenuManager().getTarget() == null);
		
		float logoSize = sr.getScaledWidth() / 1.88f;
		
		rock.getMenuManager().drawBackground(matrixStack, mouseX, mouseY, partialTicks);
		
		// Хотбар
		rock.getMenuManager().drawHotbar(matrixStack, mouseX, mouseY, 1);
		
		Rect rect = rock.getMenuManager().drawWindow(matrixStack, mouseX, mouseY, 1);
		
		float titleWidth = bold.get(20).getWidth("Многопользовательская игра");
		bold.get(20).draw(matrixStack, "Многопользовательская игра", sr.getScaledWidth()/2F - titleWidth / 2F, rect.getY() - 18, text.alpha(alphaAnim.get()));
		
		this.hoveringText = null;
        
        if (mc.currentScreen == Page.MULTI.getScreen()) {
        	GL11.glEnable(GL11.GL_SCISSOR_TEST);
            Rect toScissor = rect.size(1);
    		Render.scissor(toScissor.getX(), toScissor.getY(), toScissor.getWidth(), toScissor.getHeight());
            this.serverListSelector.render(matrixStack, mouseX, mouseY, alphaAnim.get());
    		GL11.glDisable(GL11.GL_SCISSOR_TEST);
    		
    		viaScreen.y = (int) (5 - 60 + 60 * alphaAnim.get());

            super.render(matrixStack, mouseX, mouseY, alphaAnim.get());
        }

        if (this.hoveringText != null)
        {
            this.func_243308_b(matrixStack, this.hoveringText, mouseX, mouseY);
        }
        
        if (!this.rock.isPanic()) this.rock.getVia().viaScreen.render(matrixStack, mouseX, mouseY, alphaAnim.get());
    }

    public void connectToSelected()
    {
        ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

        if (ServerCustomList$entry instanceof ServerCustomList.NormalEntry)
        {
            this.connectToServer(((ServerCustomList.NormalEntry)ServerCustomList$entry).getServerData());
        }
        else if (ServerCustomList$entry instanceof ServerCustomList.LanDetectedEntry)
        {
            LanServerInfo lanserverinfo = ((ServerCustomList.LanDetectedEntry)ServerCustomList$entry).getServerData();
            this.connectToServer(new ServerData(lanserverinfo.getServerMotd(), lanserverinfo.getServerIpPort(), true));
        }
    }

    private void connectToServer(ServerData server)
    {
        this.minecraft.displayGuiScreen(new ConnectingScreen(rock.isPanic() ? new MainMenuScreen() : Page.MAIN.getScreen(), this.minecraft, server));
    }

    public void func_214287_a(ServerCustomList.Entry p_214287_1_)
    {
        this.serverListSelector.setSelected(p_214287_1_);
        this.func_214295_b();
    }

    public void func_214295_b()
    {
        this.btnSelectServer.active = false;
        this.btnEditServer.active = false;
        this.btnDeleteServer.active = false;
        ServerCustomList.Entry ServerCustomList$entry = this.serverListSelector.getSelected();

        if (ServerCustomList$entry != null && !(ServerCustomList$entry instanceof ServerCustomList.LanScanEntry))
        {
            this.btnSelectServer.active = true;

            if (ServerCustomList$entry instanceof ServerCustomList.NormalEntry)
            {
                this.btnEditServer.active = true;
                this.btnDeleteServer.active = true;
            }
        }
    }

    public ServerPinger getOldServerPinger()
    {
        return this.oldServerPinger;
    }

    public void func_238854_b_(List<ITextComponent> p_238854_1_)
    {
        this.hoveringText = p_238854_1_;
    }

    public ServerList getServerList()
    {
        return this.savedServerList;
    }
}
