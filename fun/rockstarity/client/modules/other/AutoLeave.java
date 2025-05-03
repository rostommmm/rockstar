package fun.rockstarity.client.modules.other;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.friends.Friend;
import fun.rockstarity.api.friends.FriendsHandler;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Input;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.text.ITextComponent;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

import java.util.List;

/**
 * @author Malecharik
 * @since 15 Mar 2024 12:31:27
 */


@Info(name="AutoLeave", desc="Выполняет действие когда рядом сущность", type=Category.OTHER)
public class AutoLeave extends Module {
	
	private final Slider distance = new Slider(this, "Дистанция").min(10f).max(100).set(20).inc(5f);

	private final Mode mode = new Mode(this, "Что делать");

	private final Mode.Element svo = new Mode.Element(mode, "Своя команда");
	private final Input inputSVO = new Input(this, "Команда").hide(() -> !mode.is(svo));

	private final Mode.Element hub = new Mode.Element(mode, "Ливать в хаб");
	private final Mode.Element leave = new Mode.Element(mode, "Ливать с сервера");
	private final Mode.Element spawn = new Mode.Element(mode, "Ливать на спавн");
	private final Mode.Element darena = new Mode.Element(mode, "Ливать на darena");
	
	private final CheckBox predict = new CheckBox(this, "Заранее").desc("Делает действие заранее");
	
	private final Slider slider = new Slider(this, "Время до действия").max(7).inc(1).min(1).set(7).hide(() -> !predict.get());
	
	private boolean shouldLeave;
	boolean darenaopenet = false;
	@Override
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && shouldLeave) {
			if (predict.get()) {
				if (Server.getTimeCT() == slider.get()) {
					leave();
					shouldLeave = false;
					this.toggle();
				}
			} else {
				for (PlayerEntity e : mc.world.getPlayers()) {
					if (e == null || rock.getFriendsHandler().isFriend(e.getName().getString()) || e == mc.player || e.getDistance(e) > this.distance.get() || Server.hasCT()) continue;
					
					leave();
					shouldLeave = false;
					rock.getAlertHandler().alert("Рядом обнаружен " + e.getName().getString() + ", ливаю", AlertType.SUCCESS);
					this.toggle();
					break;
				}
			}
		}
		if (darenaopenet && event instanceof EventReceivePacket e) {

			if (e.getPacket() instanceof SOpenWindowPacket packet) {
				if (packet.getTitle().getString().contains("Арена смерти")) {
					e.cancel();
					mc.getGameSettings().keyBindSneak.setPressed(true);
				}
			}

			if (e.getPacket() instanceof SPlaySoundEffectPacket packet) {
				if (packet.getSound().getName().toString().contains("glass.place") || packet.getSound().getName().toString().contains("note_block.xylophone")) {
					e.cancel();
				}
			}

			if (e.getPacket() instanceof SSetSlotPacket packet) {
				ItemStack stack = packet.getStack();

				if (stack.getDisplayName().getString().contains("На Смотровую")) {
					mc.getGameSettings().keyBindSneak.setPressed(false);

					boolean click = true;

					List<ITextComponent> itemTooltip = stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL);
					for (ITextComponent str : itemTooltip) {
						if (str.getString().contains("Стоп")) {
							mc.player.connection.sendPacket(new CCloseWindowPacket(packet.getWindowId()));
							click = false;
							break;
						}
					}

					if (click)
						mc.player.connection.sendPacket(new CClickWindowPacket(packet.getWindowId(),
								24, 0, ClickType.PICKUP,
								mc.player.openContainer.getSlot(24).getStack(),
								mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
				}
			}
		}
	}
	@NativeInclude
	protected void leave() {
	    if (this.mode.is(this.svo)) {
	        if (!inputSVO.get().isEmpty()) {
	            mc.player.sendChatMessage(inputSVO.get());
	            rock.getAlertHandler().alert("Используем свою команду: " + inputSVO.get(), AlertType.SUCCESS);
	        } else {
	            rock.getAlertHandler().alert("Ошибка: Команда не указана!", AlertType.ERROR);
	        }
	    } else if (this.mode.is(this.hub)) {
	        mc.player.sendChatMessage("/hub");
	        rock.getAlertHandler().alert("Телепортируемся в хаб!", AlertType.SUCCESS);
	    } else if (this.mode.is(this.leave)) {
	        mc.world.sendQuittingDisconnectingPacket();
	    } else if (this.mode.is(this.spawn)) {
	        mc.player.sendChatMessage("/spawn");
	        rock.getAlertHandler().alert("Телепортируемся на спавн!", AlertType.SUCCESS);
	    } else if (this.mode.is(this.darena)) {
			darenaopenet = true;
	        mc.player.sendChatMessage("/darena");
	        rock.getAlertHandler().alert("Телепортируемся на darena!", AlertType.SUCCESS);
	    }
	}
	@NativeInclude
    @Override
    public void onEnable() {
        shouldLeave = true;
    }

    @Override
    public void onDisable() {
        shouldLeave = false;
    }
}
