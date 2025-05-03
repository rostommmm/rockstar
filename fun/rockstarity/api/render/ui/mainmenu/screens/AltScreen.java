package fun.rockstarity.api.render.ui.mainmenu.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.helpers.game.GameUtility;
import fun.rockstarity.api.helpers.math.Hover;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.Stencil;
import fun.rockstarity.api.helpers.system.TextUtility;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.animation.infinity.InfinityAnimation;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.color.themes.Style;
import fun.rockstarity.api.render.shaders.list.Outline;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.alt.Alt;
import fun.rockstarity.api.render.ui.mainmenu.alt.AltServer;
import fun.rockstarity.api.render.ui.mainmenu.alt.BanProcessor;
import fun.rockstarity.api.render.ui.mainmenu.alt.Filter;
import fun.rockstarity.api.render.ui.rect.Rect;
import fun.rockstarity.api.render.ui.widgets.InputWidget;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class AltScreen extends Screen {
	InputWidget input, search, note;
	//Button login;
	//Button randomLogin;
	
	@Getter private final List<Alt> alts = new ArrayList<>();
	private Rect listWindow, random, add, filterBtn, closeBtn;
	
	private float scroll;
	private final InfinityAnimation scrollAnim = new InfinityAnimation();

	@Getter private final Animation selectAnim = new Animation().setEasing(Easing.EASE_IN_OUT_QUART).setSpeed(300);
	@Getter private final Animation filtersAnim = new Animation().setEasing(Easing.BOTH_SINE).setSpeed(300);
	private Alt selected, lastSelected;
	
	private boolean filters;
	
	public AltScreen() {
		super(new TranslationTextComponent("Alt Manager"));
	}

	@Override
	public void init() {
		this.buttons.clear();
		input = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 200, 20,
				new TranslationTextComponent("Nickname"));
		input.setText(mc.getSession().getUsername());

		search = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 150, 20,
				new TranslationTextComponent("Поиск"));
		
		note = new InputWidget(bold.get(14), this.width / 2 - 50, height / 2, 150, 20,
				new TranslationTextComponent("Заметка"));
		
		/*
		addButton(login = new Button(this.width / 2 - 50, height / 2 + 26, 100, 20, new TranslationTextComponent("Login"),
				nick -> {
					if (input.getText() == null || input.getText().equals(""))
						return;
					GameUtility.changeName(input.getText());
				}));
		
		addButton(randomLogin = new Button(this.width / 2 - 50, height / 2 + 50, 100, 20, new TranslationTextComponent("Random Login"), random -> {
			String randomNick = TextUtility.getRandomNick();
			input.setText(randomNick);
			GameUtility.changeName(randomNick);
		}));
		*/
		
		for (Alt alt : alts) {
			BanProcessor.checkBan(alt);
		}
	}

	@Override
	public void tick() {
		input.tick();
		search.tick();
		note.tick();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Animation alphaAnim = rock.getMenuManager().getAlphaAnim();
		FixColor first = rock.getThemes().getFirstColor().alpha(alphaAnim.get());
		FixColor second = rock.getThemes().getSecondColor().alpha(alphaAnim.get());
		FixColor text = rock.getThemes().getTextFirstColor().alpha(alphaAnim.get());
		FixColor textSecond = rock.getThemes().getTextSecondColor().alpha(alphaAnim.get());
		alphaAnim.setForward(rock.getMenuManager().getTarget() == null);
		
		rock.getMenuManager().drawBackground(matrixStack, mouseX, mouseY, partialTicks);
		rock.getMenuManager().drawHotbar(matrixStack, mouseX, mouseY, 1);
		
		listWindow = rock.getMenuManager().drawWindow(matrixStack, mouseX, mouseY, 1);
		
		if (mc.currentScreen == Page.ALT.getScreen()) {
			
			Collections.sort(alts, (a, b) -> {
				return Boolean.compare(!a.isFavorite(), !b.isFavorite());
			});
			
			/*
			this.input.setMaxStringLength(16);
			bold.get(16).draw(matrixStack, "Ваш ник - " + mc.getSession().getUsername(), input.x, input.y - 13f, rock.getThemes().getTextFirstColor());
			Round.draw(matrixStack, new Rect(input.x, input.y, input.getWidth(), input.getHeightRealms()), 2, rock.getThemes().getFirstColor());
			input.renderButton(matrixStack, mouseX, mouseY, partialTicks, alphaAnim.get());
			*/

			selectAnim.setForward(selected != null);
			
			if (!selectAnim.finished()) { // Список акков
				GL11.glPushMatrix();
				GL11.glTranslated(- listWindow.getWidth() * selectAnim.get(), 0, 0);
				
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Rect scissor = listWindow.size(1);
				Render.scissor(scissor.getX(), scissor.getY(), Math.max(0, scissor.getWidth()), Math.max(0, scissor.getHeight()));
				
				bold.get(16).draw(matrixStack, "Список аккаунтов", listWindow.getX() + 7, listWindow.getY() + 5, text);
				
				// Поиск
				Rect search = new Rect(listWindow.getX() + 8, listWindow.getY() + 20, listWindow.getWidth() - 75, 15);
				Round.draw(matrixStack, search, 2, rock.getThemes().getSecondColor());
				Render.image("icons/mainmenu/alt/search.png", search.getX() + 4, search.getY() + 4, 7, 7, text);
				
				this.search.setMaxStringLength(16);
				this.search.x = (int) (listWindow.getX() + 16);
				this.search.y = (int) (listWindow.getY() + 19);
				if (this.search.isFocused() || !this.search.getText().isEmpty()) {
					this.search.renderButton(matrixStack, mouseX, mouseY, partialTicks, alphaAnim.get());
				} else {
					bold.get(14).draw(matrixStack, "Поиск..", search.getX() + 14, search.getY() + 2.5f, textSecond);
				}
				
				// Фильтры
				filterBtn = new Rect(listWindow.getX() + listWindow.getWidth() - 63, listWindow.getY() + 20, 55, 15);
				Round.draw(matrixStack, filterBtn, 2, rock.getThemes().getSecondColor());
				semibold.get(14).draw(matrixStack, "Фильтры", filterBtn.getX() + 5, filterBtn.getY() + 2.5f, text);
				Render.image("icons/mainmenu/alt/filter.png", filterBtn.getX() + 44, filterBtn.getY() + 4, 7, 7, text);
				
				// Разделитель
				Round.draw(matrixStack, new Rect(listWindow.getX() + 8, listWindow.getY() + 42, listWindow.getWidth() - 16, 0.5f), 0, rock.getThemes().getFoursColor().alpha(alphaAnim.get() * 0.7f));
				
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
				
				scrollAnim.animate(scroll + 50 - 50 * alphaAnim.get(), 100);
				
				// Список
				float height = 17;
				float off = 0;
				
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(scissor.getX(), scissor.getY() + 43, Math.max(0, scissor.getWidth()), Math.max(0, scissor.getHeight() - 43.5f));
				
				for (Alt alt : alts) {
					boolean skip = true;
					boolean servCheck = true;
					
					for (Filter filter : Filter.values()) {
						if (filter.apply(alt))
							skip = false;
					}
					
					for (AltServer server : alt.getServers()) {
						if (server.getIp().toLowerCase().contains(this.search.getText().toLowerCase()))
							servCheck = false;
					}
					
					if (!alt.getUsername().toLowerCase().contains(this.search.getText().toLowerCase())
							&& !alt.getNote().toLowerCase().contains(this.search.getText().toLowerCase())
							&& servCheck
							|| skip) continue;
					
					Rect rect1 = new Rect(listWindow.getX() + 8, listWindow.getY() + 50 + off + scrollAnim.get(), listWindow.getWidth() - 16, height);

					drawAlt(alt, matrixStack, rect1, mouseX, mouseY, false);
					
					off += height + 2;
				}
				Round.draw(matrixStack, new Rect(scissor.getX() + 7, scissor.getY() + 42, scissor.getWidth() - 14, 7), 0.1f, first, first, first.alpha(0), first.alpha(0));
				Round.draw(matrixStack, new Rect(scissor.getX() + 7, scissor.getY() + scissor.getHeight() - 7.5f, scissor.getWidth() - 14, 7), 0.1f, first.alpha(0), first.alpha(0), first, first);
				GL11.glDisable(GL11.GL_SCISSOR_TEST);

				if (scroll > 0 || off < scissor.getHeight() - 55) {
					scroll = 0;
				} else if (scroll < -off + scissor.getHeight() - 55) {
					scroll = -off + scissor.getHeight() - 55;
				}
				
				GL11.glPopMatrix();
				
				filtersAnim.setForward(filters && selected == null);
				if (!filtersAnim.finished(false)) {
					float width = 100;
					Rect window = new Rect(listWindow.getX() + listWindow.getWidth() - width - 8, listWindow.getY() + 40, width, 119);
					Round.draw(matrixStack, window, 3, first.alpha(filtersAnim.get()));
					Round.drawOutlined(matrixStack, window.size(0.5f), 3, 1, first.alpha(0), second.alpha(filtersAnim.get()));
					bold.get(14).draw(matrixStack, "Выберите", window.getX() + 5, window.getY() + 4, text.alpha(filtersAnim.get()));
					
					float yOff = 0;
					for (Filter filter : Filter.values()) {
						bold.get(12).draw(matrixStack, filter.getName(), window.getX() + 5, window.getY() + 16 + yOff, text.alpha(filtersAnim.get()));
						filter.getTextBlock().set(window.getX() + 5, window.getY() + 24 + yOff, width / 1.2f, 100);
						filter.getTextBlock().render(matrixStack, filter.getDesc(), semibold.get(10), textSecond.alpha(filtersAnim.get()));
						
						filter.getCheckAnim().setForward(filter.isEnabled());
						
						if (!filter.getCheckAnim().finished(false))
							Render.image("icons/checkmark.png", window.getX() + window.getWidth() - 13, window.getY() + 16 + (filter.getTextBlock().getHeight()+7) / 2F + yOff, 7, 7, text.alpha(filtersAnim.get() * filter.getCheckAnim().get()));
						
						yOff += filter.getTextBlock().getHeight() + 18;
					}
				}
			}
			
			// Инфа о акке
			if (!selectAnim.finished(false) && lastSelected != null) {
				GL11.glPushMatrix();
				GL11.glTranslated(listWindow.getWidth() - listWindow.getWidth() * selectAnim.get(), 0, 0);
				
				float height = 17;
				float off = 0;
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Rect scissor = listWindow.size(1);
				Render.scissor(scissor.getX(), scissor.getY(), Math.max(0, scissor.getWidth()), Math.max(0, scissor.getHeight()));
				
				bold.get(16).draw(matrixStack, "Информация о аккаунте", listWindow.getX() + 7, listWindow.getY() + 5, text);
				closeBtn = new Rect(listWindow.getX() + listWindow.getWidth() - 15, listWindow.getY() + 7, 8, 8);
				Render.image("icons/close.png", closeBtn.getX(), closeBtn.getY(), closeBtn.getWidth(), closeBtn.getHeight(), text);
				
				Rect rect = new Rect(listWindow.getX() + 8, listWindow.getY() + 20, listWindow.getWidth() - 16, height);
				drawAlt(lastSelected, matrixStack, rect, 0, 0, true);


				float xOff = 0;
				if (lastSelected.getDonate() != null) {
					xOff += bold.get(14).getWidth(lastSelected.getDonate() + " ") + 1;
				}

				xOff += bold.get(14).getWidth(lastSelected.getUsername());
				

				Stencil.init();
				Round.draw(matrixStack, rect.width(rect.getWidth()-5), 3, second.move(FixColor.RED, 0.05f));
				Stencil.read(1);
				search.setFocused2(false);
				note.setMaxStringLength(64);
				note.x = (int) (listWindow.getX() + 15 + xOff);
				note.y = (int) (listWindow.getY() + 20);
				note.setWidth((int) (listWindow.getWidth() - xOff));
				note.renderButton(matrixStack, mouseX, mouseY, partialTicks, alphaAnim.get() * 0.7f);
				if (selected != null) {
					selected.setNote(note.getText());
				}
				Stencil.finish();
				
				if (note.getText().isBlank() && !note.isFocused()) {
					bold.get(14).draw(matrixStack, "Заметка", note.x + 5, note.y + 4, textSecond);
				}
				
				Round.draw(matrixStack, new Rect(listWindow.getX() + 8, listWindow.getY() + 42, listWindow.getWidth() - 16, 0.5f), 0, rock.getThemes().getFoursColor().alpha(alphaAnim.get() * 0.7f));
				
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
				
				scrollAnim.animate(scroll + 50 - 50 * alphaAnim.get(), 100);
				
				AltServer hovered = null;
				
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				Render.scissor(scissor.getX(), scissor.getY() + 43, Math.max(0, scissor.getWidth()), Math.max(0, scissor.getHeight() - 44));
				
				for (AltServer server : lastSelected.getServers()) {
					Rect rect1 = new Rect(listWindow.getX() + 8, listWindow.getY() + 50 + off + scrollAnim.get(), listWindow.getWidth() - 16, height);
					drawServer(server, matrixStack, rect1, mouseX, mouseY);
					
					if (Hover.isHovered(rect1, mouseX, mouseY))
					hovered = server;
					
					off += height + 2;
				}
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
				
				if (hovered != null && hovered.getBan() != null && selectAnim.finished()) {
					float yOff = 0;
					for (String str : hovered.getBan().split("\\n")) {
						bold.get(14).draw(matrixStack, str, listWindow.getX() + listWindow.getWidth() + 5, listWindow.getY() + yOff, text.alpha(hovered.getHoverAnim().get()));
						yOff += 9;
					}
				}
				
				Round.draw(matrixStack, new Rect(scissor.getX() + 7, scissor.getY() + 42, scissor.getWidth() - 14, 7), 0.1f, first, first, first.alpha(0), first.alpha(0));
				Round.draw(matrixStack, new Rect(scissor.getX() + 7, scissor.getY() + scissor.getHeight() - 7.5f, scissor.getWidth() - 14, 7), 0.1f, first.alpha(0), first.alpha(0), first, first);

				if (scroll > 0 || off < scissor.getHeight() - 55) {
					scroll = 0;
				} else if (scroll < -off + scissor.getHeight() - 55) {
					scroll = -off + scissor.getHeight() - 55;
				}
				GL11.glPopMatrix();
			} else {
				note.setFocused2(false);
			}
			
			{ // Добавление акка
				Rect rect = new Rect(listWindow.getX(), listWindow.getY() + listWindow.getHeight() + 5, listWindow.getWidth(), 55 * alphaAnim.get());
				Round.draw(matrixStack, rect, 8, first, first, first, first);
				Outline.draw(matrixStack, rect, 8, 0.1f, rock.getThemes().getFoursColor().alpha(alphaAnim.get() * 0.7f));
				
				Stencil.init();
				Round.draw(matrixStack, rect, 8, first, first, first, first);
				Stencil.read(1);
				
				bold.get(16).draw(matrixStack, "Добавление аккаунта", rect.getX() + 7, rect.getY() + 5, text);
				semibold.get(14).draw(matrixStack, "Введите ник в поле и кликните кнопку справа", rect.getX() + 7, rect.getY() + 16, text);
				Round.draw(matrixStack, new Rect(rect.getX() + 7, rect.getY() + 31, rect.getWidth() - 52, 15), 2, rock.getThemes().getSecondColor());
				
				input.setMaxStringLength(16);
				input.x = (int) (rect.getX() + 7);
				input.y = (int) (rect.getY() + 30);
				input.renderButton(matrixStack, mouseX, mouseY, partialTicks, alphaAnim.get());
				
				// Рандом
				random = new Rect(rect.getX() + rect.getWidth() - 41, rect.getY() + 31, 15, 15);
				Round.draw(matrixStack, random, 2, rock.getThemes().getSecondColor());
				Render.image("icons/mainmenu/alt/magic.png", random.getX() + 2, random.getY() + 2, 11, 11, text);
				
				// Добавление
				Round.draw(matrixStack, add = new Rect(rect.getX() + rect.getWidth() - 22, rect.getY() + 31, 15, 15), 2, second);
				Round.draw(matrixStack, new Rect(rect.getX() + rect.getWidth() - 22 + 6.5f, rect.getY() + 34, 2, 9), 1.5f, text);
				Round.draw(matrixStack, new Rect(rect.getX() + rect.getWidth() - 22 + 3, rect.getY() + 31 + 6.5f, 9, 2), 1.5f, text);
				Stencil.finish();
				
				drawString(matrixStack, sr.getScaledWidth()/2F - (bold.get(20).getWidth("Сейчас вы: " + mc.getSession().getUsername())) / 2F, rect.getY() + rect.getHeight() + 2, "Сейчас вы: ", mc.getSession().getUsername(), text, Style.getCurrent().getColors()[1].alpha(alphaAnim.get()));
			}
			
			
			super.render(matrixStack, mouseX, mouseY, alphaAnim.get());
		}
		
		bold.get(16).draw(matrixStack, "тише", -100, -100, textSecond);
	}
	
	private void drawServer(AltServer server, MatrixStack matrixStack, Rect rect, double mouseX, double mouseY) {
		Animation alphaAnim = rock.getMenuManager().getAlphaAnim();
		FixColor second = rock.getThemes().getSecondColor().alpha(alphaAnim.get());
		FixColor text = rock.getThemes().getTextFirstColor().alpha(alphaAnim.get());
		FixColor textSecond = rock.getThemes().getTextSecondColor().alpha(alphaAnim.get());
		alphaAnim.setForward(rock.getMenuManager().getTarget() == null);
		
		
		FixColor color = second;
		if (server.getBan() != null && !server.getBan().isEmpty()) {
			color = color.move(FixColor.RED.alpha(alphaAnim.get()), 0.05f);
		}
		
		server.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		
		Round.draw(matrixStack, rect, 3, color);
		
		if (MultiScreen.KOSTIL) {
			Stencil.init();
			Round.draw(matrixStack, rect, 6f, second);
			Stencil.read(1);
			Rect serv = rect.size(-10 * server.getHoverAnim().get());
			Render.image(new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(server.getIp()) + "/icon"), serv.getX(), serv.getY() - serv.getWidth() / 2, serv.getWidth(), serv.getWidth(), FixColor.WHITE.alpha(alphaAnim.get()));
			Stencil.finish();
		}
		
		Round.draw(matrixStack, rect, 3, color.alpha(0.9f));

		if (server.getBan() != null && !server.getBan().isEmpty()) {
			Stencil.init();
			Round.draw(matrixStack, rect, 3, second);
			Stencil.read(1);
			Render.image("icons/mainmenu/alt/ban.png", rect.getX() - 2, rect.getY() + 2, 20, 20, FixColor.RED.alpha(alphaAnim.get() * 0.22f));
			Stencil.finish();
		}
		
		//bold.get(14).draw(matrixStack, server.getIp(), rect.getX() + 5.5f, rect.getY() + 4, FixColor.BLACK.alpha(0.2));
		bold.get(14).draw(matrixStack, server.getIp(), rect.getX() + 5, rect.getY() + 3.5f, text);
	}

	private void drawAlt(Alt alt, MatrixStack matrixStack, Rect rect, double mouseX, double mouseY, boolean preview) {
		Animation alphaAnim = rock.getMenuManager().getAlphaAnim();
		FixColor second = rock.getThemes().getSecondColor().alpha(alphaAnim.get());
		FixColor text = rock.getThemes().getTextFirstColor().alpha(alphaAnim.get());
		FixColor textSecond = rock.getThemes().getTextSecondColor().alpha(alphaAnim.get());
		alphaAnim.setForward(rock.getMenuManager().getTarget() == null);
		
		alt.getHoverAnim().setForward(Hover.isHovered(rect, mouseX, mouseY));
		alt.getSelectedAnim().setForward(mc.getSession().getUsername().equals(alt.getUsername()));
		
		FixColor color = second/*.move(FixColor.YELLOW, 0.05f)*/.move(Style.getCurrent().getColors()[1].alpha(alphaAnim.get()), alt.getSelectedAnim().get());
		if (alt.isFavorite()) {
			color = color.move(FixColor.YELLOW.alpha(alphaAnim.get()), 0.05f);
		} else if (alt.isBanned()) {
			color = color.move(FixColor.RED.alpha(alphaAnim.get()), 0.05f);
		}
		
		Round.draw(matrixStack, rect, 3, color);
		
		if (alt.isFavorite() && alt.isBanned()) {
			Stencil.init();
			Round.draw(matrixStack, rect, 3, second.move(FixColor.YELLOW, 0.05f));
			Stencil.read(1);
			Render.image("icons/mainmenu/alt/star.png", rect.getX() - 2, rect.getY() + 2, 20, 20, new FixColor(225, 255, 0).alpha(alphaAnim.get() * 0.22f));
			Stencil.finish();
			
			Stencil.init();
			Round.draw(matrixStack, rect, 3, second.move(FixColor.RED, 0.05f));
			Stencil.read(1);
			Render.image("icons/mainmenu/alt/ban.png", rect.getX() + 10, rect.getY() + 2, 20, 20, FixColor.RED.alpha(alphaAnim.get() * 0.22f));
			Stencil.finish();
		} else if (alt.isFavorite()) {
			Stencil.init();
			Round.draw(matrixStack, rect, 3, second.move(FixColor.YELLOW, 0.05f));
			Stencil.read(1);
			Render.image("icons/mainmenu/alt/star.png", rect.getX() - 2, rect.getY() + 2, 20, 20, new FixColor(225, 255, 0).alpha(alphaAnim.get() * 0.22f));
			Stencil.finish();
		} else if (alt.isBanned()) {
			Stencil.init();
			Round.draw(matrixStack, rect, 3, second.move(FixColor.YELLOW, 0.05f));
			Stencil.read(1);
			Render.image("icons/mainmenu/alt/ban.png", rect.getX() - 2, rect.getY() + 2, 20, 20, FixColor.RED.alpha(alphaAnim.get() * 0.22f));
			Stencil.finish();
		}
		
		alt.setDeleteBtn(new Rect(rect.getX() + rect.getWidth() - 12, rect.getY() + 5, 7, 7));
		alt.getDeleteAnim().setForward(Hover.isHovered(alt.getDeleteBtn(), mouseX, mouseY));
		Render.image("icons/mainmenu/alt/trash.png", alt.getDeleteBtn().getX(), alt.getDeleteBtn().getY(), alt.getDeleteBtn().getWidth(), alt.getDeleteBtn().getHeight(), text.move(FixColor.RED, 0.5f * alt.getDeleteAnim().get()).alpha(alt.getHoverAnim().get() * 0.5f + alt.getDeleteAnim().get() * 0.5f));
		
		alt.setFavoriteBtn(new Rect(rect.getX() + rect.getWidth() - 21, rect.getY() + 5, 7, 7));
		alt.getFavoriteAnim().setForward(Hover.isHovered(alt.getFavoriteBtn(), mouseX, mouseY));
		Render.image(alt.isFavorite() ? "icons/mainmenu/alt/small-star.png" : "icons/mainmenu/alt/stroke-star.png", alt.getFavoriteBtn().getX(), alt.getFavoriteBtn().getY(), alt.getFavoriteBtn().getWidth(), alt.getFavoriteBtn().getHeight(), text.alpha(alt.getHoverAnim().get() * 0.5f + alt.getFavoriteAnim().get() * 0.5f));
		
		float xOff = 0;
		if (alt.getDonate() != null) {
			bold.get(14).draw(matrixStack, alt.getDonate(), rect.getX() + 5, rect.getY() + 3.5f, alt.getDonateColor() == null ? text : alt.getDonateColor().alpha(alphaAnim.get()));
			xOff += bold.get(14).getWidth(alt.getDonate() + " ") + 1;
		}

		bold.get(14).draw(matrixStack, alt.getUsername(), rect.getX() + 5 + xOff, rect.getY() + 3.5f, text);
		xOff += bold.get(14).getWidth(alt.getUsername());
		
		if (alt.getNote() != null && !alt.getNote().isBlank() && !preview) {
			Stencil.init();
			Round.draw(matrixStack, rect.width(rect.getWidth() - 5 - 20 * alt.getHoverAnim().get()), 3, second.move(FixColor.YELLOW, 0.05f));
			Stencil.read(1);
			bold.get(14).draw(matrixStack, alt.getNote(), rect.getX() + 11 + xOff, rect.getY() + 3.5f, textSecond);
			Stencil.finish();
			
			Round.draw(matrixStack, new Rect(rect.getX() + rect.getWidth() - 15 - 20 * alt.getHoverAnim().get(), rect.getY(), 10, rect.getHeight()), 3, color.alpha(0), color, color.alpha(0), color);
		}
		

		boolean up = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
		boolean down = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
		float offset = .1f / Math.max((float) Minecraft.debugFPS, 5) * 25;
		
		if (up || down)
			this.mouseScrolled(mouseX, mouseY, up ? offset : down ? -offset : 0);
	}
	
	@Override
	public boolean charTyped(char typedChar, int keyCode) {
		input.charTyped(typedChar, keyCode);
		search.charTyped(typedChar, keyCode);
		note.charTyped(typedChar, keyCode);
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		rock.getMenuManager().keyPressed(keyCode, scanCode, modifiers);
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && selected != null) {
			selected = null;
			return true;
		}
		
		if (keyCode == GLFW.GLFW_KEY_ENTER && input.isFocused() && !input.getText().isEmpty()) {
			GameUtility.changeName(input.getText());
			return true;
		}
		input.keyPressed(keyCode, scanCode, modifiers);
		search.keyPressed(keyCode, scanCode, modifiers);
		note.keyPressed(keyCode, scanCode, modifiers);
		
		super.keyPressed(keyCode, scanCode, modifiers);
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (Hover.isHovered(closeBtn, mouseX, mouseY)) {
			selected = null;
			return true;
		}
		
		if (Hover.isHovered(add, mouseX, mouseY) && !input.getText().isBlank()) {
			for (Alt alt : alts) {
				if (alt.getUsername().contains(input.getText()))
					return false;
			}
			
			alts.add(new Alt(input.getText().replace("\\", ""), false));
			if (button == 0) {
				GameUtility.changeName(input.getText());
			}
		}
		
		if (Hover.isHovered(random, mouseX, mouseY)) {
			//ThreadManager.run(() -> {
				String rand = TextUtility.getRandomNick().replace(" ", "");
				
				if (!rand.isBlank()) {
					input.setText(rand);
					if (button == 0) {
						alts.add(new Alt(input.getText().replace("\\", ""), true));
						GameUtility.changeName(input.getText());
					}
				}
			//});
		}
		
		if (selected == null && listWindow != null) {
			Rect window = new Rect(listWindow.getX() + listWindow.getWidth() - 100 - 8, listWindow.getY() + 40, 100, 119);
			if (!Hover.isHovered(window, mouseX, mouseY)) {
				filters = false;
			} else {
				if (filters) {
					float yOff = 0;
					for (Filter filter : Filter.values()) {
						if (Hover.isHovered(window.getX(), window.getY() + 16 + yOff, window.getWidth(), filter.getTextBlock().getHeight() + 18, mouseX, mouseY)) {
							filter.setEnabled(!filter.isEnabled());
						}
						
						yOff += filter.getTextBlock().getHeight() + 18;
					}
					return true;
				}
			}
			
			if (Hover.isHovered(filterBtn, mouseX, mouseY)) {
				filters = true;
			}
		}
		
		float height = 17;
		float off = 0;
		if (listWindow != null) {
			Rect scissor = listWindow.size(1);
			
			if (Hover.isHovered(scissor.getX(), scissor.getY() + 42, scissor.getWidth(), scissor.getHeight() - 42, mouseX, mouseY) && selected == null) {
				Alt toRemove = null;
				
				for (Alt alt : alts) {
					boolean skip = true;
					boolean servCheck = true;
					
					for (Filter filter : Filter.values()) {
						if (filter.apply(alt))
							skip = false;
					}
					
					for (AltServer server : alt.getServers()) {
						if (server.getIp().toLowerCase().contains(this.search.getText().toLowerCase()))
							servCheck = false;
					}
					
					if (!alt.getUsername().toLowerCase().contains(this.search.getText().toLowerCase())
							&& !alt.getNote().toLowerCase().contains(this.search.getText().toLowerCase())
							&& servCheck
							|| skip) continue;
					
					Rect rect1 = new Rect(listWindow.getX() + 8, listWindow.getY() + 50 + off + scrollAnim.get(), listWindow.getWidth() - 16, height);
					
					if (Hover.isHovered(alt.getFavoriteBtn(), mouseX, mouseY)) {
						alt.setFavorite(!alt.isFavorite());
					} else if (Hover.isHovered(alt.getDeleteBtn(), mouseX, mouseY)) {
						toRemove = alt;
					} else if (Hover.isHovered(rect1, mouseX, mouseY)) {
						if (button == 0) {
							GameUtility.changeName(alt.getUsername());
						} else {
							selected = alt;
							lastSelected = alt;
							
							note.setMaxStringLength(64);
							note.setText(alt.getNote());
							note.setFocused2(false);
						}
					}
					
					off += height + 2;
				}
				
				if (toRemove != null)
					alts.remove(toRemove);
			}
		}
		rock.getMenuManager().clicked(mouseX, mouseY, button);
		input.mouseClicked(mouseX, mouseY, button);
		search.mouseClicked(mouseX, mouseY, button);
		note.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		scroll += delta * 15;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	private float drawString(MatrixStack matrixStack, float x, float y, String left, String right, FixColor color, FixColor color1) {
		float titleOffset = 0;
		
		bold.get(20).draw(matrixStack, left, x + titleOffset, y, color);
		titleOffset += bold.get(20).getWidth(left);
		bold.get(20).draw(matrixStack, right, x + titleOffset, y, color1);
		
		return titleOffset;
	}
	
}