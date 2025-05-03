package fun.rockstarity.api.render.ui.mainmenu.loading;

import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import consts.NativeConsts;
import fun.rockstarity.api.AssetsLoader;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.secure.KeyGeneration;
import fun.rockstarity.api.helpers.secure.Web;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.ClientBloom;
import fun.rockstarity.api.render.shaders.list.ClientOutline;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.Glow;
import fun.rockstarity.api.render.shaders.list.Outline2;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import fun.rockstarity.api.render.ui.alerts.AlertUtility;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiScreen;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import fun.rockstarity.api.sounds.Sound;
import lombok.Getter;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
public class LoadingScreen extends Screen {
	
	public static boolean LOADED, FINISH, AUTH;
	private final Animation bgAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	private final Animation showBgAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(500);
	private final Animation showingAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300);
	private final Animation logoAnim = new Animation().setEasing(Easing.EASE_OUT_CIRC).setSpeed(300);
	private final TimerUtility test = new TimerUtility();
	private final InfinityAnimation yAnim = new InfinityAnimation();
	private boolean start, hello;
	InputWidget login, password;

	public LoadingScreen() {
		super(new TranslationTextComponent("Загрузка"));
		test.reset();
		

		login = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 150, 20,
				new TranslationTextComponent("Логин"));
		
		password = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 150, 20,
				new TranslationTextComponent("Пароль"));
	}
	
	@Override
	protected void init() {
		LOADED = true;
		//AssetsLoader.load();
		
		super.init();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		//AssetsLoader.loadGifs();
		
		FixColor first = rock.getThemes().getFirstColor();
		FixColor second = rock.getThemes().getSecondColor();
		FixColor text = rock.getThemes().getTextFirstColor();
		FixColor textSecond = rock.getThemes().getTextSecondColor();
		
		bgAnim.setForward(true && !FINISH);
		showBgAnim.setForward(bgAnim.get() > 0.7f && !FINISH);
		showingAnim.setForward(showBgAnim.isForward() && showBgAnim.get() > 0.8f && !FINISH);
		logoAnim.setForward(showingAnim.finished() && !FINISH);
		
		float logoSize = sr.getScaledWidth() / 1.88f;
		
    	Render.initRotate(sr.getScaledWidth()/2F, sr.getScaledHeight() - 10, -45 * bgAnim.get());
		Render.scale(sr.getScaledWidth()/2F, sr.getScaledHeight() - 10, 1 - bgAnim.get());
		FrameFreeze.draw(matrixStack, bgAnim.get());
		Render.end();
		Render.endRotate();
		
		Round.draw(matrixStack, new Rect(0,0,sr.getScaledWidth(), sr.getScaledHeight()), 0, first.alpha(bgAnim.get()));

		if (bgAnim.get() > 0.7f) {
			if (LoadingScreen.AUTH) {
				float size = 120;
				Rect rect = new Rect(sr.getScaledWidth()/2F - size/2, sr.getScaledHeight()/2F - 25-10, size, 20);
				Round.draw(matrixStack, rect, 3, second);
				bold.get(14).draw(matrixStack, "Логин или почта", rect.getX(), rect.getY() - 11, textSecond);
				Stencil.init();
				Round.draw(matrixStack, rect, 3, second.move(FixColor.RED, 0.05f));
				Stencil.read(1);
				login.setMaxStringLength(64);
				login.x = (int) (rect.getX());
				login.y = (int) (rect.getY()+1);
				login.setWidth((int) (rect.getWidth()));
				login.renderButton(matrixStack, mouseX, mouseY, partialTicks, bgAnim.get());
				Stencil.finish();
				
				rect = new Rect(sr.getScaledWidth()/2F - size/2, sr.getScaledHeight()/2F + 7-10, size, 20);
				Round.draw(matrixStack, rect, 3, second);
				bold.get(14).draw(matrixStack, "Пароль", rect.getX(), rect.getY() - 11, textSecond);
				Stencil.init();
				Round.draw(matrixStack, rect, 3, second.move(FixColor.RED, 0.05f));
				Stencil.read(1);
				password.setMaxStringLength(64);
				password.x = (int) (rect.getX());
				password.y = (int) (rect.getY()+1);
				password.setWidth((int) (rect.getWidth()));
				password.renderButton(matrixStack, mouseX, mouseY, partialTicks, bgAnim.get());
				Stencil.finish();
				
				rect = new Rect(sr.getScaledWidth()/2F - size/2, sr.getScaledHeight()/2F + 23, size, 20);
				Round.draw(matrixStack, rect, 3, second);
				bold.get(14).draw(matrixStack, "Войти", rect.getX() + rect.getWidth() / 2F - bold.get(14).getWidth("Войти")/2F, rect.getY() + 4.5f, Hover.isHovered(rect, mouseX, mouseY) ? text : textSecond);
			} else {
				if (!start && logoAnim.finished()) {
					ThreadManager.run(() -> {
						rock.onStart();
						if (!LoadingScreen.AUTH)
							AssetsLoader.load();
					});
					start = true;
				}
				
				/*
				Render.image("masks/ui/mainmenu/bglogo.png", -logoSize/9.14F - logoSize + logoSize * showBgAnim.get(), sr.getScaledHeight() + logoSize/7.31f - logoSize, logoSize, logoSize, second.move(first, rock.getThemes().getCurrent() == rock.getThemes().getDarkTheme() ? 0.5f : 0).alpha(showBgAnim.get()));
				Render.grid(matrixStack, rock.getThemes().getThirdColor().alpha(bgAnim.get()*0.1f), sr.getScaledWidth()/15);
				Render.gridMask(matrixStack, FixColor.WHITE, new Rect(mouseX-5, mouseY-5, 10, 10), 100);
				*/
				
				float offY = MathUtility.interpolate(-LoadingStage.values().length * 50 - 20, sr.getScaledHeight()/2, logoAnim.get());
				int i = 0;
				float summ = -50;
				
				for (LoadingStage stage : LoadingStage.values()) {
					if (stage == LoadingStage.current && stage.afterReady.passed(300)) {
						yAnim.animate(i * 60, 50);
					}
		    		i++;
				}
				
				summ = yAnim.get();
				
				i = 0;
				
				AlertUtility.setTicks(AlertUtility.getTicks()+1);
				
				for (LoadingStage stage : LoadingStage.values()) {
					/*
					if (test.passed(i * 1000)) {
						stage.getActiveAnim().setForward(true);
						if (i < LoadingStage.values().length-1 && LoadingStage.values()[i+1].getActiveAnim().get() > 0) {
							stage.getReadyAnim().setForward(true);
						}
					}
					*/
					
					String title = stage.getTitle();
					
					bold.get(20).draw(matrixStack, title, sr.getScaledWidth()/2F - sr.getScaledWidth()/7 + 16, offY - summ, rock.getThemes().getTextFirstColor().alpha(logoAnim.get()));
					bold.get(16).draw(matrixStack, stage.getStage(), sr.getScaledWidth()/2F - sr.getScaledWidth()/7 + 16, offY - summ + 11, rock.getThemes().getTextSecondColor().alpha(logoAnim.get() * stage.getActiveAnim().get()));
		    		
		    		Round.draw(matrixStack, new Rect(sr.getScaledWidth()/2F - sr.getScaledWidth()/7 + 2 - 10 * stage.getActiveAnim().get(), offY + 2 - summ, 10, 10), 5, rock.getThemes().getSecondColor().alpha(logoAnim.get() * (1-stage.getActiveAnim().get())));
		    		AlertUtility.drawSmallIcon(matrixStack, AlertType.WAIT, sr.getScaledWidth()/2F - sr.getScaledWidth()/7 + 10 - 10 * stage.getActiveAnim().get() - 10 * stage.getReadyAnim().get(), offY - 1 - summ, logoAnim.get() * stage.getActiveAnim().get() * (1-stage.getReadyAnim().get()));
		    		AlertUtility.drawSmallIcon(matrixStack, AlertType.SUCCESS, sr.getScaledWidth()/2F - sr.getScaledWidth()/7 + 10 - 10 * stage.getReadyAnim().get(), offY - 1 - summ, logoAnim.get() * stage.getReadyAnim().get());
		    		
		    		offY += 30 + 30 * stage.getActiveAnim().get();
		    		i++;
				}
				
				/*
				if (test.passed(LoadingStage.values().length * 1000)) {
					for (LoadingStage stage : LoadingStage.values()) {
						stage.getActiveAnim().setForward(false);
						stage.getReadyAnim().setForward(false);
					}
					test.reset();
				}
				*/
				
				FixColor c = rock.getThemes().getFirstColor();
				FixColor c1 = c.alpha(0);
				Round.draw(matrixStack, new Rect(0, 0, sr.getScaledWidth(), sr.getScaledHeight()/4f), 0.1f, c);
				Round.draw(matrixStack, new Rect(0, sr.getScaledHeight() - sr.getScaledHeight()/4f, sr.getScaledWidth(), sr.getScaledHeight()/4f), 0.1f, c);
				
				Round.draw(matrixStack, new Rect(0, sr.getScaledHeight()/4f-1, sr.getScaledWidth(), sr.getScaledHeight()/4f), 0.1f, c, c, c1, c1);
				Round.draw(matrixStack, new Rect(0, sr.getScaledHeight()/2f+1, sr.getScaledWidth(), sr.getScaledHeight()/4f), 0.1f, c1, c1, c, c);
				Render.grid(matrixStack, rock.getThemes().getThirdColor().alpha(bgAnim.get()*0.1f), sr.getScaledWidth()/15);
				Render.gridMask(matrixStack, FixColor.WHITE, new Rect(mouseX-5, mouseY-5, 10, 10), 100);
			}
		}
		
	 	if (!FINISH) {
	 		float alpha = 0;
	 		
	 		float off = 300;
	    	
	    	List<String> texts = TextUtility.wrapText(ResourceLoadProgressGui.tooltip, bold.get(16), 300);
	    	float val = bold.get(16).getHeight();
	    	
	    	bold.get(16).start();
	    	bold.get(16).draw(matrixStack, ResourceLoadProgressGui.prefix, sr.getScaledWidth()/2F - bold.get(16).getWidth(ResourceLoadProgressGui.prefix)/2f, MathUtility.interpolate(sr.getScaledHeight() - 37 - texts.size() * val + val, sr.getScaledHeight()+10, logoAnim.get()), rock.getThemes().getTextSecondColor().alpha(1-alpha));
	    	for (String text1 : texts) {
	    		bold.get(16).draw(matrixStack, text1, sr.getScaledWidth()/2F - bold.get(16).getWidth(text1)/2f, MathUtility.interpolate(sr.getScaledHeight() - 25 - texts.size() * val + (1+texts.indexOf(text1)) * val, sr.getScaledHeight()+10, logoAnim.get()), rock.getThemes().getTextFirstColor().alpha(1-alpha));
	    	}
	    	bold.get(16).end();
	 	}

		if (bgAnim.finished(false)) {
			if (!hello) {
				ThreadManager.run(() -> {
					if (NativeConsts.SOUND)
						new Sound("nastya/hello").play();
					
					if (rock.getUser().getStarts() <= 1) {
						rock.getInstance().handlePotatoPC();
						ThreadManager.run(() -> {
							try {
								Thread.sleep(3000L);
								new Sound("nastya/telegram").play();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						});
					}
				});
				
				new Glow();
				new Outline2();
				new ClientBloom();
				new ClientOutline();
				rock.setClickGui(new ClickGuiScreen());
				rock.getConfigHandler().load(rock.getCfgToLoad(), true);
				AssetsLoader.loadGifs();
				
				
				hello = true;
			}
			mc.displayGuiScreen(Page.MAIN.getScreen());
		}
		//super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void tick() {
		login.tick();
		password.tick();
		super.tick();
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		login.charTyped(codePoint, modifiers);
		password.charTyped(codePoint, modifiers);
		return super.charTyped(codePoint, modifiers);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		login.keyPressed(keyCode, scanCode, modifiers);
		password.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		login.mouseClicked(mouseX, mouseY, button);
		password.mouseClicked(mouseX, mouseY, button);
		
		float size = 120;
		if (Hover.isHovered(sr.getScaledWidth()/2F - size/2, sr.getScaledHeight()/2F + 23, size, 20, mouseX, mouseY)) {
			LoadingScreen.AUTH = false;
			ThreadManager.run(() -> {
				rock.onStart();
				if (!LoadingScreen.AUTH)
					AssetsLoader.load();
			});
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		login.mouseReleased(mouseX, mouseY, button);
		password.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public void closeScreen() {
		super.closeScreen();
	}

}
