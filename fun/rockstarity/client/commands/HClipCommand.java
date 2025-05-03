package fun.rockstarity.client.commands;

import fun.rockstarity.api.commands.CmdInfo;
import fun.rockstarity.api.commands.Command;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.RussianNumberParser;
import fun.rockstarity.api.render.ui.alerts.AlertType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import ru.kotopushka.j2c.sdk.annotations.NativeInclude;

/**
 * @author Malecharik
 * @since 9 Mar 2024 18:23:03
 */
@NativeInclude
@CmdInfo(names = { "hclip"}, desc = "Телепортирует игрока вперед/назад")
public class HClipCommand extends Command {
	
	@Override
	public void execute(String[] args) {
		if (args.length == 0) 
			this.error();
		else {

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
            	float rotationYawInRadians = mc.player.rotationYaw * 0.017453292F; // Преобразование в радианы
    			String input = String.join(" ", args);
    			double speed = 0;
    			try {
    				speed = Double.parseDouble(input);
    			} catch (NumberFormatException e) {
    				speed = RussianNumberParser.parseRussianNumber(input);
    			}
    			double x = -Math.sin(rotationYawInRadians) * speed; //Синусу и косинусу задаем значение в радианах
    			double z = Math.cos(rotationYawInRadians) * speed;
    			
    			mc.player.connection.sendPacket(new CUseEntityPacket(boat, Hand.MAIN_HAND, false));
    			boat.setPosition(boat.getPosX(), boat.getPosY(), boat.getPosZ() + z);
                mc.player.connection.sendPacket(new CMoveVehiclePacket(boat));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(boat.getPosX(), boat.getPosY(), boat.getPosZ() + z, false));
            } else {
            	float rotationYawInRadians = mc.player.rotationYaw * 0.017453292F; // Преобразование в радианы
    			String input = String.join(" ", args);
    			double speed = 0;
    			try {
    				speed = Double.parseDouble(input);
    			} catch (NumberFormatException e) {
    				speed = RussianNumberParser.parseRussianNumber(input);
    			}
    			String direction = speed < 0 ? " назад" : " вперед";
    			double x = -Math.sin(rotationYawInRadians) * speed; //Синусу и косинусу задаем значение в радианах
    			double z = Math.cos(rotationYawInRadians) * speed;
    			
    			mc.player.setMotion(0.0, 0.0, 0.0);
    	        mc.player.setVelocity(0.0, 0.0, 0.0);
    	        mc.player.setPosition(mc.player.getPosX() + x, mc.player.getPosY(), mc.player.getPosZ() + z);
    			mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX() + x, mc.player.getPosY() + 0.01, mc.player.getPosZ() + z, mc.player.isOnGround()));
    	        mc.player.setSneaking(false);
    			rock.getAlertHandler().alert("Телепортируем на " + Math.abs(speed) + direction, AlertType.INFO);

            }
		}
	}
}
