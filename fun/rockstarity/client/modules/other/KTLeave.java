package fun.rockstarity.client.modules.other;

import java.util.concurrent.TimeUnit;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.Binding;
import fun.rockstarity.api.modules.settings.list.CheckBox;
import fun.rockstarity.api.modules.settings.list.Mode;
import lombok.Getter;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 13 Mar 2024 12:53:49
 */


@Info(name="KTLeave", desc="Позволяет выходить в PVP режиме", type=Category.OTHER)
public class KTLeave extends Module {
	
	private final Mode mode = new Mode(this, "Режим");

	private final Mode.Element funsky = new Mode.Element(mode, "FunSky");
	private final Mode.Element darena = new Mode.Element(mode, "/darena");
	private final Mode.Element spooky = new Mode.Element(mode, "Spooky");
	
	private final CheckBox autoRCT = new CheckBox(this, "Авто .rct").set(true).hide(() -> !funsky.get() && !spooky.get());
	private final Binding bind = new Binding(this, "Кнопка лива");
	
	boolean win;
	boolean disabled;
	int code;
	
	@Override
	@NativeInclude
	public void onEvent(Event event) {
		if (event instanceof EventUpdate && funsky.get()) {
			mc.playerController.pickItem(-1);
			
			if (autoRCT.get()) 
				rock.getCommands().execute("rct");
			
			set(false);
		}
		
		if (event instanceof EventUpdate && spooky.get()) {
    		mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(Float.NaN, Float.NaN, true));
			
			if (autoRCT.get()) 
				rock.getCommands().execute("rct");
			
			set(false);
		}
		
		if (darena.get()) {
			if (event instanceof EventReceivePacket e) {
				if (e.getPacket() instanceof SOpenWindowPacket packet) {
					if (win) {
						disabled = true;
						Chat.msg("KTLeave отключен античитом, выйди из кт :(");
					} else { 
						code = packet.getWindowId();
						e.cancel();
					}
				}
				
				if (e.getPacket() instanceof SCloseWindowPacket) {
					disabled = true;
					Chat.msg("КТЛив отключен античитом, выйди из кт :(");
				}
			}
			
			if (event instanceof EventSendPacket e) {
				if (e.getPacket() instanceof CPlayerTryUseItemOnBlockPacket) {
					//Chat.msg("Это взаимодействие недоступно при KTLeave");
		        	//e.cancel();
		        }
			}
			
			if (event instanceof EventWorldChange) {
				Chat.msg("KTLeave активирован :3");
				mc.player.sendChatMessage("/darena");
			}
			
			if (event instanceof EventKey e) {
		        if (bind.check(e) && mc.currentScreen == null) {
		        	Player.packetClick(code, 24, 0, ClickType.PICKUP, mc.player);
					Chat.msg(disabled ? "КТЛив отключен античитом, выйди из кт :(" : "КТЛиваюсь))");
		        }
			}
		}
	}
	
	@Override
	public void onEnable() {
		if (darena.get()) {
			Chat.msg("KTLeave активирован :3");
			mc.player.sendChatMessage("/darena");
			disabled = false;
			win = false;
		}
	}
	
    @Override
	public void onDisable() {
	}

}