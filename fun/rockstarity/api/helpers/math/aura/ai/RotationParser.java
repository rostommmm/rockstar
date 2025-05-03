package fun.rockstarity.api.helpers.math.aura.ai;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.math.aura.Rotation;
import fun.rockstarity.api.helpers.system.ThreadManager;
import fun.rockstarity.api.secure.Debugger;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

/**
 * @author ConeTin
 * @since 18 янв. 2025 г.
 */

@UtilityClass
public class RotationParser implements IAccess {
	
//	
//	public void rotate(LivingEntity player) {
//		ClientPlayerEntity target = mc.player;
//
//		if (timer.passed(2000)) return;
//		
//        float yawDiff = Math.abs(getAngleDifference(player.rotationYaw, Rotation.get(target.getPositionVec()).x));
//        float pitchDiff = Math.abs(player.rotationPitch - Rotation.get(target.getPositionVec()).y);
//
//        player.rotations.add(new RotationData(
//                (float) Math.abs(player.interpTargetYaw - player.lastInterpTargetYaw), (float) Math.abs(player.interpTargetYaw - player.lastInterpTargetYaw),
//                yawDiff, pitchDiff,
//                timer.getElapsed(),
//                (float) player.getDistance(target),
//                player.isOnGround() ? 1 : 0,
//                player.isElytraFlying() || player.isSwimming() ? 1 : 0
//        ));
//
//        if (player.rotations.size() == 80) {
//        	
//        	Chat.bot(player.getName().getString(), "Генерирую информацию о ротации ");
//
//            JSONObject blockData = new JSONObject();
//            blockData.put("yaw", MathHelper.wrapDegrees(mc.player.rotationYaw));
//            JSONArray rotationDataArray = getObjects(player);
//            blockData.put("rotationData", rotationDataArray);
//
//            File pluginFolder = new File("C:/Rockstar/");
//            if (!pluginFolder.exists()) {
//                pluginFolder.mkdirs();
//            }
//
//            File file = new File(pluginFolder, "rotation_data.json");
//
//            JsonObject jsonObject = new JsonObject();
//            try (FileReader fileReader = new FileReader(file)) {
//                JsonReader reader = new JsonReader(fileReader);
//                reader.setLenient(true);
//                JsonElement element = new JsonParser().parse(reader);
//                if (element.isJsonObject()) {
//                    jsonObject = element.getAsJsonObject();
//                }
//            } catch (IOException e) {
//                jsonObject = new JsonObject();
//            }
//
//            int blockNumber = jsonObject.size() + 1;
//
//            jsonObject.add(String.valueOf(blockNumber), new JsonParser().parse(blockData.toString()));
//
//            try (FileWriter fileWriter = new FileWriter(file)) {
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                gson.toJson(jsonObject, fileWriter);
//            	Chat.bot(player.getName().getString(), "Сохраняю информацию о ротации");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
///*
//            List<Float> values = new ArrayList<>();
////['yawDelta', 'yawDiff', 'pitchDiff', 'sinceAttack', 'inputForward', 'inputStrafe', 'distance', 'onGround_1.0', 'miniHitbox', 'isSprinting_1.0']
//            for (RotationData data : player.rotations) {
//                values.add(data.yawDelta);
//                values.add(data.yawDiff);
//                values.add(data.pitchDiff);
//                values.add(data.sinceAttack);
//                values.add(data.inputForward);
//                values.add(data.inputStrafe);
//                values.add(data.distance);
//                values.add(data.onGround);
//                values.add(data.miniHitbox);
//                values.add(data.isSprinting);
//            }
//
//            float[] valuesArray = new float[values.size()];
//            for (int i = 0; i < values.size(); i++) {
//                valuesArray[i] = values.get(i);
//            }
//
//            try {
//                CatBoostPredictions prediction = GrimAPI.INSTANCE.getModel().predict(valuesArray, new String[]{});
//                double value = prediction.get(0, 0);
//               // player.nurik("model value is " + value);
//
//                if (value > 0f) {
//                    buffer++;
//                    if (buffer >= 3) {
//                        flagAndAlert("buffer=" + buffer);
//                    }
//                } else {
//                    buffer = 0;
//                }
//            } catch (CatBoostError e) {
//                e.printStackTrace();
//            }
//            */
//
//
//
//
//            /* сохры */
//            player.rotations.clear();
//        }
//
//	}
	/*
	public void rotate(LivingEntity target) {
		 if (System.currentTimeMillis() - attack > 1000) return;

		 float yawDiff = getAngleDifference(mc.player.rotationYaw, Rotation.get(target.getPositionVec()).x);
		 float pitchDiff = mc.player.rotationPitch - Rotation.get(target.getPositionVec()).y;

		 RotationData data = new RotationData(
                yawDiff, pitchDiff,
                (float) (System.currentTimeMillis() - attack),
                (float) mc.player.movementInput.moveForward, (float) mc.player.movementInput.moveStrafe,
                (float) mc.player.getDistance(target),
                mc.player.isOnGround() ? 1 : 0,
                mc.player.isElytraFlying() || mc.player.isSwimming() ? 1 : 0,
                mc.player.isSprinting() ? 1 : 0
        );
		
        
		 List<Float> values = new ArrayList<>();
		//['yawDiff', 'sinceAttack', 'distance', 'onGround', 'inputForward', 'inputStrafe', 'pitchDiff']
         values.add(data.yawDiff);
         values.add(data.sinceAttack);
         values.add(data.distance);
         values.add(data.onGround);
         values.add(data.inputForward);
         values.add(data.inputStrafe);
         values.add(data.pitchDiff);

         float[] valuesArray = new float[values.size()];
         for (int i = 0; i < values.size(); i++) {
             valuesArray[i] = values.get(i);
         }

         try {
        	    // Предсказание модели
        	    CatBoostPredictions prediction = rock.getModel().predict(valuesArray, new String[]{});

        	    // Получаем предсказания для yaw и pitch
        	    double yawValue = prediction.get(0, 0);  // Предсказание для yaw

        	    // Выводим результат
        	    Chat.debug("Predicted: " + yawValue + " - ");
        	    mc.player.rotationYaw = (float) yawValue;
        	} catch (CatBoostError e) {
        	    e.printStackTrace();
        	}
	}
	*/
	
	public final List<RotationData> rotations = new ObjectArrayList<>(20);
	private LivingEntity target;
	private final TimerUtility timer = new TimerUtility();
	private final TimerUtility attackTimer = new TimerUtility();
	
	public void onEvent(Event event) {
		if (event instanceof EventAttack e) {
			target = e.getTarget();
			timer.reset();
			attackTimer.reset();
		}
		
		if (event instanceof EventReceivePacket e && e.getPacket() instanceof SPlaySoundEffectPacket play &&
				(play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT || play.getSound() == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP)) {
			ThreadManager.run(() -> {
				if (!attackTimer.passed(200)) {
					JSONObject blockData = new JSONObject();
		            JSONArray rotationDataArray = getObjects(mc.player);
		            blockData.put("rotationData", rotationDataArray);

		            File pluginFolder = new File("C:/Rockstar/");
		            if (!pluginFolder.exists()) {
		                pluginFolder.mkdirs();
		            }

		            File file = new File(pluginFolder, "rotation_data.json");

		            JsonObject jsonObject = new JsonObject();
		            try (FileReader fileReader = new FileReader(file)) {
		                JsonReader reader = new JsonReader(fileReader);
		                reader.setLenient(true);
		                JsonElement element = new JsonParser().parse(reader);
		                if (element.isJsonObject()) {
		                    jsonObject = element.getAsJsonObject();
		                }
		            } catch (IOException e2) {
		                jsonObject = new JsonObject();
		            }
		            
		            int blockNumber = jsonObject.size() + 1;

		            jsonObject.add(String.valueOf(blockNumber), new JsonParser().parse(blockData.toString()));

		            try (FileWriter fileWriter = new FileWriter(file)) {
		                Gson gson = new GsonBuilder().setPrettyPrinting().create();
		                gson.toJson(jsonObject, fileWriter);
		                Debugger.overlay("Сохраняю информацию о ротации");
		            } catch (IOException e2) {
		                e2.printStackTrace();
		            }
				}
				
				rotations.clear();
			});
        }
		
		if (event instanceof EventUpdate && target != null) {
			parse(target);
		}
	}

	public void parse(LivingEntity player) {
		 if (timer.passed(2000) || player.isDead() || !mc.world.getPlayers().contains(player)) return;

        float yawDiff = MathHelper.wrapDegrees(Rotation.get(target.getPositionVec()).x);
        float pitchDiff = MathHelper.wrapDegrees(Rotation.get(target.getPositionVec()).y);

        rotations.add(new RotationData(
                (float) Math.abs(player.rotationYaw - player.prevRotationYaw), (float) Math.abs(player.rotationPitch - player.prevRotationPitch),
                yawDiff, pitchDiff,
                timer.getElapsed(),
                (float) player.getDistance(target),
                player.isOnGround() ? 1 : 0,
                player.isElytraFlying() || player.isSwimming() ? 1 : 0,
                MathHelper.wrapDegrees(mc.player.rotationYaw), mc.player.rotationPitch
        ));
	}
		

    private JSONArray getObjects(LivingEntity player) {
        JSONArray rotationDataArray = new JSONArray();
        try {
            for (RotationData data : rotations) {
        		JSONObject blockData = new JSONObject();
        		blockData.put("yawDelta", data.yawDelta);
        		blockData.put("pitchDelta", data.pitchDelta);
        		blockData.put("targetYaw", data.targetYaw);
        		blockData.put("targetPitch", data.targetPitch);
        		blockData.put("sinceAttack", data.sinceAttack);
        		blockData.put("distance", data.distance);
        		blockData.put("onGround", data.onGround);
        		blockData.put("miniHitbox", data.miniHitbox);
        		
        		blockData.put("yaw", data.yaw);
        		blockData.put("pitch", data.pitch);
                rotationDataArray.put(blockData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotationDataArray;
    }

    
	public static float normalizeTo360(float angle) {
        angle %= 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static float normalizeTo180(float angle) {
        angle = normalizeTo360(angle);
        if (angle > 180) {
            angle -= 360;
        }
        return angle;
    }
    
    public static float getAngleDifference(float angle1, float angle2) {
        float normalizedAngle1 = normalizeTo360(angle1);
        float normalizedAngle2 = normalizeTo360(angle2);

        float difference = normalizedAngle1 - normalizedAngle2;

        return normalizeTo180(difference);
    }


}
