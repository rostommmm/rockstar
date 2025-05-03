package fun.rockstarity.api.helpers.game.proxy;

import com.mojang.blaze3d.matrix.MatrixStack;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class GuiProxy extends Screen {
	private boolean isSocks4 = false;

    private TextFieldWidget ipPort;
    private TextFieldWidget username;
    private TextFieldWidget password;
    private CheckboxButton enabledCheck;

    private Screen parentScreen;

    private String msg = "";

    private int[] positionY;
    private int positionX;

    private Proxy oldProxy;

    @Getter
    private TestPing testPing = new TestPing();

    public GuiProxy(Screen parentScreen) {
        super(new StringTextComponent("Proxy"));
        this.parentScreen = parentScreen;
    }
    
    private boolean setProxy() {
        if (!isValidIpPort(ipPort.getText())) {
            msg = TextFormatting.RED + "Invalid IP:PORT";
            this.ipPort.changeFocus(true);
            return false;
        }

        ProxyServer.proxy = new Proxy(isSocks4, ipPort.getText().trim(), username.getText().trim(), password.getText().trim());
        return true;
    }
    
    private static boolean isValidIpPort(String ipP) {
        return ipP.matches("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+");
    }

    private void centerButtons(int amount, int buttonLength, int gap) {
        positionX = (this.width / 2) - (buttonLength / 2);
        positionY = new int[amount];
        int center = (this.height + amount * gap) / 2;
        int buttonStarts = center - (amount * gap);
        for (int i = 0; i != amount; i++) {
            positionY[i] = buttonStarts + (gap * i);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        msg = "";
        testPing.state = "";
        return true;
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        if(enabledCheck.isChecked() && !isValidIpPort(ipPort.getText())){
            enabledCheck.onPress();
        }

        font.drawStringWithShadow(matrixStack, "Proxy Type:", this.width / 2f - 149, positionY[1] + 5, 10526880);
        drawCenteredString(matrixStack, this.font, "Proxy Authentication (optional)", this.width / 2, positionY[3] + 8, TextFormatting.WHITE.getColor());
        font.drawStringWithShadow(matrixStack, "IP:PORT: ", this.width / 2f - 125, positionY[2] + 5, 10526880);

        this.ipPort.render(matrixStack, mouseX, mouseY, partialTicks);
        if (isSocks4) {
            font.drawStringWithShadow(matrixStack, "User ID: ", this.width / 2f - 140, positionY[4] + 5, 10526880);
            this.username.render(matrixStack, mouseX, mouseY, partialTicks);
        } else {
            font.drawStringWithShadow(matrixStack, "Username: ", this.width / 2f - 140, positionY[4] + 5, 10526880);
            font.drawStringWithShadow(matrixStack, "Password: ",this.width / 2f - 140, positionY[5] + 5, 10526880);
            this.username.render(matrixStack, mouseX, mouseY, partialTicks);
            this.password.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        drawCenteredString(matrixStack, this.font, !msg.isEmpty() ? msg : testPing.state, this.width / 2, positionY[6] + 5, 10526880);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void tick() {
        testPing.pingPendingNetworks();

        this.ipPort.tick();
        this.username.tick();
        this.password.tick();
    }
    
    @Override
    public void init() {
        mc.keyboardListener.enableRepeatEvents(true);
        int buttonLength = 160;
        centerButtons(10, buttonLength, 26);

        isSocks4 = ProxyServer.proxy.type == Proxy.ProxyType.SOCKS4;
        oldProxy = ProxyServer.proxy;

        Button proxyType = new Button(positionX, positionY[1], buttonLength, 20, new StringTextComponent(isSocks4 ? "Socks 4" : "Socks 5"), (button) -> {
            isSocks4 = !isSocks4;
            button.setMessage(new StringTextComponent(isSocks4 ? "Socks 4" : "Socks 5"));
        });
        this.addButton(proxyType);

        this.ipPort = new TextFieldWidget(this.font, positionX, positionY[2], buttonLength, 20, new StringTextComponent(""));
        this.ipPort.setText(ProxyServer.proxy.ipPort);
        this.ipPort.setMaxStringLength(21);
        this.ipPort.changeFocus(true);
        this.children.add(this.ipPort);

        this.username = new TextFieldWidget(this.font, positionX, positionY[4], buttonLength, 20, new StringTextComponent(""));
        this.username.setMaxStringLength(255);
        this.username.setText(ProxyServer.proxy.username);
        this.children.add(this.username);

        this.password = new TextFieldWidget(this.font, positionX, positionY[5], buttonLength, 20, new StringTextComponent(""));
        this.password.setMaxStringLength(255);
        this.password.setText(ProxyServer.proxy.password);
        this.children.add(this.password);

        //int posXButtons = (this.width / 2) - (((buttonLength / 2) * 3) / 2);
        int posXButtons = (this.width / 2) - ((buttonLength / 2) * 3) / 3;

        Button apply = new Button(posXButtons, positionY[8], buttonLength / 2 - 3, 20, new StringTextComponent("Apply"), (button) -> {
            if (setProxy()) {
                AccountsProxy.setDefaultProxy(ProxyServer.proxy);
                AccountsProxy.saveProxyAccounts();
                ProxyServer.proxyEnabled = enabledCheck.isChecked();
                Minecraft.getInstance().displayGuiScreen(this.parentScreen);
            }
        });
        this.addButton(apply);

        Button test = new Button(posXButtons + buttonLength / 2 + 3, positionY[8], buttonLength / 2  - 3, 20, new StringTextComponent("Test"), (button) -> {
            if(ipPort.getText().isEmpty() || ipPort.getText().equalsIgnoreCase("none")){
                msg = TextFormatting.RED + "Specify proxy to test";
                return;
            }
            if (setProxy()) {
                testPing = new TestPing();
                testPing.run("mc.funtime.su", 25565, ProxyServer.proxy);
            }
        });
        //this.addButton(test);

        this.enabledCheck = new CheckboxButton((this.width / 2) - (15 + font.getStringWidth("Proxy Enabled")) / 2, positionY[7], buttonLength, 20, new StringTextComponent("Proxy Enabled"), ProxyServer.proxyEnabled);
        this.addButton(this.enabledCheck);

        //Button cancel = new Button(posXButtons + (buttonLength / 2 + 3) * 2, positionY[8], buttonLength / 2 - 3, 20, new StringTextComponent("Cancel"), (button) -> {
        Button cancel = new Button(posXButtons + buttonLength / 2 + 3, positionY[8], buttonLength / 2 - 3, 20, new StringTextComponent("Cancel"), (button) -> {
            ProxyServer.proxy = oldProxy;
            Minecraft.getInstance().displayGuiScreen(parentScreen);
        });
        this.addButton(cancel);
    }
}
