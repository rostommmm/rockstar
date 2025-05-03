package fun.rockstarity.api.events.list.game;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.widget.TextFieldWidget;

@Getter @Setter
@AllArgsConstructor
public class EventChatScreen extends Event {
	private final TextFieldWidget field;
	private final MatrixStack matrixStack;
}
