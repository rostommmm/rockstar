package fun.rockstarity.api.helpers.game.toolTip;

import fun.rockstarity.api.IAccess;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class FixToolTip {
	public int x, width;
    private int mouseX;
    boolean flipped;

    public void set(int x, int width) {
    	FixToolTip.x = Math.max(0, x);
    	FixToolTip.width = width - 20;
        mouseX = FixToolTip.x - 24;
        flipped = false;
    }

    public List<ITextComponent> doFix(List<ITextComponent> text, FontRenderer textRenderer) {
        List<ITextComponent> originalText = text;
        text = new ArrayList<>(text);

        if (text.size() == 0 || (text.size() == 1 && text.get(0).getString().length() <= 12)) {
            return text;
        }

        for (int i = 0; i < text.size(); i++) {
            if (isTooWide(textRenderer, text.get(i).getString())) {
                Style style = text.get(i).getStyle();
                List<String> words = new ArrayList<>(Arrays.asList(text.get(i).getString().split(" ")));
                if (words.isEmpty()) return text;

                String newLine = words.remove(0);
                if (isTooWide(textRenderer, newLine)) {
                    if (!flipped && x > width / 2) {
                        flipped = true;
                        return doFix(originalText, textRenderer);
                    } else {
                        String oldLine = newLine;
                        while (isTooWide(textRenderer, newLine + "-")) {
                            newLine = newLine.substring(0, newLine.length() - 1);
                        }
                        words.add(0, "-" + oldLine.substring(newLine.length()));
                        newLine = newLine + "-";
                    }
                } else {
                    while (words.size() > 0) {
                        if (!isTooWide(textRenderer, newLine + " " + words.get(0))) {
                            newLine += " " + words.remove(0);
                        } else {
                            break;
                        }
                    }
                }
                text.set(i, new StringTextComponent(newLine).setStyle(style));
                if (words.size() > 0) {
                    text.add(i + 1, new StringTextComponent(String.join(" ", words)).setStyle(style));
                }
            }
        }
        return text;
    }

    private boolean isTooWide(FontRenderer textRenderer, String line) {
        if (flipped) {
            if (mouseX - textRenderer.getStringWidth(line) > 0) {
                attemptUpdateMaxWidth(line, textRenderer);
                return false;
            }
            return true;
        }
        return x + textRenderer.getStringWidth(line) > width;
    }

    private void attemptUpdateMaxWidth(String newLine, FontRenderer textRenderer) {
        int lineWidth = textRenderer.getStringWidth(newLine);
        if (lineWidth > mouseX - x) {
            x = mouseX - lineWidth;
        }
    }
}
