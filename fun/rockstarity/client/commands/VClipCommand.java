package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.commands.CommandParameter;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.RussianNumberParser;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;
@NativeInclude
/**
 * @author Malecharik
 * @since 9 Mar 2024 13:59:07
 */

@CmdInfo(names = {"vclip", "вклип"}, desc = "Телепортирует игрока вверх/вниз")
public class VClipCommand extends Command {
	
	CommandParameter up = new CommandParameter(this, "up", "top", "upper");
	CommandParameter down = new CommandParameter(this, "down", "bottom", "under");
	
	private final TimerUtility boatTimer = new TimerUtility();
	private Vector3d target;

	@Override
	public void execute(String[] args) {
		if (args.length == 0)
			this.error();
		else {
			String input = String.join(" ", args);
			double distance;
			if (contains(input, down)){
				int i;
				float y = 0f;

				for (i = 2; i < 255; ++i) {
					if (Player.getBlock(0, -i, 0) == Blocks.AIR && Player.getBlock(0, -i -1, 0) == Blocks.AIR) {
						y = -i - 1;
						break;
					}

					if (Player.getBlock(0, -i, 0) == Blocks.BEDROCK) {
						rock.getAlertHandler().alert("Нету пустого места", AlertType.INFO);
						break;
					}
				}
				distance = y;
			} else if (contains(input, up)){
				int i;
				float y = 0f;

				for (i = 2; i < 255; ++i) {
					if (Player.getBlock(0, i + 1, 0) == Blocks.AIR && Player.getBlock(0, i + 2, 0) == Blocks.AIR) {
							y = i + 1;
							break;
					}
				}

				distance = y;
			} else {
				try {
					distance = Double.parseDouble(input);
				} catch (NumberFormatException e) {
					distance = RussianNumberParser.parseRussianNumber(input);
				}
			}

			String path = distance < 0 ? " вниз" : " вверх";
			if (distance == 300) {
				Chat.msg("Отсоси у тракториста");
			}
			
			Entity boat = mc.player.getRidingEntity();
			if (boat == null) {
				for (Entity ent : mc.world.getAllEntities()) {
					if (ent instanceof BoatEntity entity && mc.player.getDistance(entity) < 3f) {
						boat = ent;
						break;
					}
				}
			}
			
            if (boat != null && Server.isHW()) {
            	if (!boatTimer.passed(1500)) return;
                mc.player.connection.sendPacket(new CUseEntityPacket(boat, Hand.MAIN_HAND, false));
                boat.setPosition(boat.getPosX(), boat.getPosY() + distance, boat.getPosZ());
                mc.player.connection.sendPacket(new CMoveVehiclePacket(boat));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(boat.getPosX(), boat.getPosY() + distance, boat.getPosZ(), false));
                boatTimer.reset();
            } else {
    			mc.player.setPosition(this.target = mc.player.getPositionVec().add(0, distance, 0));
				if (distance != 0) {
					rock.getAlertHandler().alert("Телепортируем на " + distance + path, AlertType.INFO);
				}
            }
		}
	}
}
