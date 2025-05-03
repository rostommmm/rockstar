package fun.rockstarity.api.render.ui.mainmenu.screens;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.AssetsLoader;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.helpers.render.gif.GifRender;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import fun.rockstarity.api.render.shaders.list.FrameFreeze;
import fun.rockstarity.api.render.shaders.list.Round;
import fun.rockstarity.api.render.ui.clickgui.ClickGuiRenderer;
import fun.rockstarity.api.render.ui.mainmenu.Page;
import fun.rockstarity.api.render.ui.mainmenu.WorldCustomList;
import fun.rockstarity.api.render.ui.rect.Rect;
import lombok.Getter;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @author ConeTin
 * @since 20 окт. 2024 г.
 */

@Getter
public class SingleScreen extends Screen
{
    protected final Screen prevScreen;
    private List<IReorderingProcessor> worldVersTooltip;
    private Button deleteButton;
    private Button selectButton;
    private Button renameButton;
    private Button copyButton;
    private WorldCustomList selectionList;

    public SingleScreen(Screen screenIn)
    {
        super(new TranslationTextComponent("selectWorld.title"));
        this.prevScreen = screenIn;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void tick()
    {
    }

    protected void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        
        int height = (int) (sr.getScaledHeight()/1.33F);
        
        this.selectionList = new WorldCustomList(this, this.minecraft, this.width, this.height, (int) (this.height/2 - height/2 - sr.getScaledHeight()/12.73F + 10), height+10, 38, this.selectionList);
        selectionList.func_244605_b(false);
        selectionList.func_244606_c(false);
        selectionList.setRenderHeader(false, 0);
        
        int offset = 40;
        
        this.children.add(this.selectionList);
        this.selectButton = this.addButton(new Button(this.width / 2 - 154, this.height - 52 - offset, 150, 20, new TranslationTextComponent("selectWorld.select"), (p_214325_1_) ->
        {
            this.selectionList.func_214376_a().ifPresent(WorldCustomList.Entry::func_214438_a);
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height - 52 - offset, 150, 20, new TranslationTextComponent("selectWorld.create"), (p_214326_1_) ->
        {
            this.minecraft.displayGuiScreen(CreateWorldScreen.func_243425_a(this));
        }));
        this.renameButton = this.addButton(new Button(this.width / 2 - 154, this.height - 28 - offset, 72, 20, new TranslationTextComponent("selectWorld.edit"), (p_214323_1_) ->
        {
            this.selectionList.func_214376_a().ifPresent(WorldCustomList.Entry::func_214444_c);
        }));
        this.deleteButton = this.addButton(new Button(this.width / 2 - 76, this.height - 28 - offset, 72, 20, new TranslationTextComponent("selectWorld.delete"), (p_214330_1_) ->
        {
            this.selectionList.func_214376_a().ifPresent(WorldCustomList.Entry::func_214442_b);
        }));
        this.copyButton = this.addButton(new Button(this.width / 2 + 4, this.height - 28 - offset, 72, 20, new TranslationTextComponent("selectWorld.recreate"), (p_214328_1_) ->
        {
            this.selectionList.func_214376_a().ifPresent(WorldCustomList.Entry::func_214445_d);
        }));
        this.addButton(new Button(this.width / 2 + 82, this.height - 28 - offset, 72, 20, DialogTexts.GUI_CANCEL, (p_214327_1_) ->
        {
            this.minecraft.displayGuiScreen(this.prevScreen);
        }));
        this.func_214324_a(false);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
		rock.getMenuManager().keyPressed(keyCode, scanCode, modifiers);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void closeScreen()
    {
        this.minecraft.displayGuiScreen(this.prevScreen);
    }

    public boolean charTyped(char codePoint, int modifiers)
    {
        return super.charTyped(codePoint, modifiers);
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
		
		// Приветствие
		float titleWidth = bold.get(20).getWidth("Одиночная игра");
		bold.get(20).draw(matrixStack, "Одиночная игра", sr.getScaledWidth()/2F - titleWidth / 2F, rect.getY() - 18, text.alpha(alphaAnim.get()));
		
        this.worldVersTooltip = null;
        
        if (mc.currentScreen == Page.SINGE.getScreen()) {
        	GL11.glEnable(GL11.GL_SCISSOR_TEST);
            Rect toScissor = rect.size(1);
    		Render.scissor(toScissor.getX(), toScissor.getY(), toScissor.getWidth(), toScissor.getHeight());
            this.selectionList.render(matrixStack, mouseX, mouseY, alphaAnim.get());
    		GL11.glDisable(GL11.GL_SCISSOR_TEST);

            super.render(matrixStack, mouseX, mouseY, alphaAnim.get());
        }

        if (this.worldVersTooltip != null)
        {
            this.renderTooltip(matrixStack, this.worldVersTooltip, mouseX, mouseY);
        }
    }

    public void func_239026_b_(List<IReorderingProcessor> p_239026_1_)
    {
        this.worldVersTooltip = p_239026_1_;
    }

    public void func_214324_a(boolean p_214324_1_)
    {
        this.selectButton.active = p_214324_1_;
        this.deleteButton.active = p_214324_1_;
        this.renameButton.active = p_214324_1_;
        this.copyButton.active = p_214324_1_;
    }
    
    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		rock.getMenuManager().clicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}

    public void onClose()
    {
        if (this.selectionList != null)
        {
            this.selectionList.getEventListeners().forEach(WorldCustomList.Entry::close);
        }
    }
}
