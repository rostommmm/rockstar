package fun.rockstarity.api.render.ui.screen;

import java.net.InetSocketAddress;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.proxy.ProxyUtility;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.LazyValue;
import net.minecraft.util.text.TranslationTextComponent;

public class ProxyScreen extends Screen {
	
	private InputWidget input;
	private Button readyButton;
	
	public ProxyScreen() {
		super(new TranslationTextComponent("Proxy Manager"));
	}
	
	@Override
	protected void init() {
		input = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 100, 20,
				new TranslationTextComponent("Proxy adres"));
		
		if (ProxyUtility.getProxyIp() != null && ProxyUtility.getProxyPort() != 0) input.setText(ProxyUtility.getProxyIp() + ":" + ProxyUtility.getProxyPort());
		
		addButton(readyButton = new Button(this.width / 2 - 50, height / 2 + 26, 100, 20, new TranslationTextComponent("Применить"), random -> {
			applyProxy();
		}));
		super.init();
	}
	
	protected void applyProxy() {
		String proxyInput = input.getText();
		
		if (proxyInput.isEmpty()) {
			ProxyUtility.disableProxy();
			return;
		}
		
		String[] split = proxyInput.split(":");
		
		if (split.length != 2) {
			ProxyUtility.disableProxy();
			return;
		}
		
		String ip = split[0];
		int port;
		
		try {
			port = Integer.parseInt(split[1]);
		} catch (NumberFormatException e) {
			ProxyUtility.disableProxy();
			return;
		}
		
		ProxyUtility.setProxy(ip, port);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderBackground(matrixStack);
		input.setMaxStringLength(21);
		bold.get(16).draw(matrixStack, "Введите прокси", input.x, input.y - 13f, FixColor.WHITE);
		Round.draw(matrixStack, new Rect(input.x, input.y, input.getWidth(), input.getHeightRealms()), 1, rock.getThemes().getFirstColor());
		input.render(matrixStack, mouseX, mouseY, partialTicks);
		input.renderButton(matrixStack, mouseX, mouseY, partialTicks);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void tick() {
		input.tick();
		super.tick();
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		input.charTyped(typedChar, keyCode);
		return false;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		input.mouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		input.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
