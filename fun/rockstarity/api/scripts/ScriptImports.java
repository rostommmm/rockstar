package fun.rockstarity.api.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import fun.rockstarity.api.scripts.wrappers.Client;
import fun.rockstarity.api.scripts.wrappers.Events;
import fun.rockstarity.api.scripts.wrappers.Player;
import fun.rockstarity.api.scripts.wrappers.Render;
import fun.rockstarity.api.scripts.wrappers.base.FilesBase;
import fun.rockstarity.api.scripts.wrappers.base.GL11Base;
import fun.rockstarity.api.scripts.wrappers.base.MathBase;
import fun.rockstarity.api.scripts.wrappers.base.ScriptBase;
import fun.rockstarity.api.scripts.wrappers.base.WorldBase;
import fun.rockstarity.api.scripts.wrappers.factory.AnimFactory;
import fun.rockstarity.api.scripts.wrappers.factory.BindFactory;
import fun.rockstarity.api.scripts.wrappers.factory.CheckBoxFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ColorFactory;
import fun.rockstarity.api.scripts.wrappers.factory.DragFactory;
import fun.rockstarity.api.scripts.wrappers.factory.InputFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ModeFactory;
import fun.rockstarity.api.scripts.wrappers.factory.ModuleFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PickerFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PositionFactory;
import fun.rockstarity.api.scripts.wrappers.factory.PotionFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SelectFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SliderFactory;
import fun.rockstarity.api.scripts.wrappers.factory.SoundFactory;
import fun.rockstarity.api.scripts.wrappers.factory.TimerFactory;
import fun.rockstarity.api.scripts.wrappers.factory.VectorFactory;
import lombok.AllArgsConstructor;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CJigsawBlockGeneratePacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CMarkRecipeSeenPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CQueryEntityNBTPacket;
import net.minecraft.network.play.client.CQueryTileEntityNBTPacket;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateBeaconPacket;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateJigsawBlockPacket;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateRecipeBookStatusPacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;

/**
 * @author ConeTin
 * @since 2 С„РµРІСЂ. 2025вЂЇРі.
 */

@AllArgsConstructor
public class ScriptImports {
	
	Globals engine;
	
	public class LoadString extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue code) {
            return engine.load(code.tojstring());
        }
    }
	
	public void imported(Script script) {
//		try {
//			parse("module", ModuleBase.class);
//			parse("checkbox", CheckBoxBase.class);
//			parse("slider", SliderBase.class);
//			parse("colorpicker", PickerBase.class);
//			parse("mode", ModeBase.class);
//			parse("select", SelectBase.class);
//			//parse("info", InfoBase.class);
//			parse("input", InputBase.class);
//			parse("bind", BindBase.class);
//			
//			parse("drag", DragBase.class);
//			parse("animation", AnimBase.class);
//			parse("sound", SoundBase.class);
//			//parse("staff", StaffBase.class);
//			parse("potion", PotionBase.class);
//			
//			parse("items", Items.class);
//			parse("matrixstack", MatrixBase.class);
//			parse("color", ColorBase.class);
//			parse("screen", ScreenBase.class);
//			parse("entity", EntityBase.class);
//			parse("living_entity", LivingEntityBase.class);
//			parse("vector", VectorBase.class);
//			//parse("gl11", GL11.class);
//			
//			//parse("net.minecraft.network.play.client");
//			parsePackets();
//			//parse("Script", ScriptBase.class);
//		} catch (Exception e) {
//		//	e.printStackTrace();
//		}

		engine.set("module", CoerceJavaToLua.coerce(new ModuleFactory()));
		engine.set("checkbox", CoerceJavaToLua.coerce(new CheckBoxFactory()));
		engine.set("slider", CoerceJavaToLua.coerce(new SliderFactory()));
		engine.set("colorpicker", CoerceJavaToLua.coerce(new PickerFactory()));
		engine.set("mode", CoerceJavaToLua.coerce(new ModeFactory()));
		engine.set("select", CoerceJavaToLua.coerce(new SelectFactory()));
		engine.set("position", CoerceJavaToLua.coerce(new PositionFactory()));
		engine.set("input", CoerceJavaToLua.coerce(new InputFactory()));
		engine.set("bind", CoerceJavaToLua.coerce(new BindFactory()));
		engine.set("drag", CoerceJavaToLua.coerce(new DragFactory()));
		engine.set("animation", CoerceJavaToLua.coerce(new AnimFactory()));
		engine.set("sound", CoerceJavaToLua.coerce(new SoundFactory()));
		engine.set("potion", CoerceJavaToLua.coerce(new PotionFactory()));
		engine.set("items", CoerceJavaToLua.coerce(new Items()));
		engine.set("color", CoerceJavaToLua.coerce(new ColorFactory()));
		engine.set("vector", CoerceJavaToLua.coerce(new VectorFactory()));
		engine.set("timer", CoerceJavaToLua.coerce(new TimerFactory()));
		
		engine.set("___conus228___", CoerceJavaToLua.coerce(this));
		
		engine.set("gl11", CoerceJavaToLua.coerce(new GL11Base()));
		engine.set("client", CoerceJavaToLua.coerce(new Client()));
		engine.set("player", CoerceJavaToLua.coerce(new Player()));
		engine.set("render", CoerceJavaToLua.coerce(new Render()));
		engine.set("world", CoerceJavaToLua.coerce(new WorldBase()));
		engine.set("math", CoerceJavaToLua.coerce(new MathBase()));
		engine.set("events", CoerceJavaToLua.coerce(new Events()));
		engine.set("files", CoerceJavaToLua.coerce(new FilesBase()));
		engine.set("script", CoerceJavaToLua.coerce(new ScriptBase(script)));
		
		//engine.set("loadstring", new LoadString());
	}
	
	public void parsePackets() {
		try {
			parse("CAnimateHandPacket", CAnimateHandPacket.class);
			parse("CChatMessagePacket", CChatMessagePacket.class);
			parse("CClickWindowPacket", CClickWindowPacket.class);
			parse("CClientSettingsPacket", CClientSettingsPacket.class);
			parse("CClientStatusPacket", CClientStatusPacket.class);
			parse("CCloseWindowPacket", CCloseWindowPacket.class);
			parse("CConfirmTeleportPacket", CConfirmTeleportPacket.class);
			parse("CConfirmTransactionPacket", CConfirmTransactionPacket.class);
			parse("CCreativeInventoryActionPacket", CCreativeInventoryActionPacket.class);
			parse("CCustomPayloadPacket", CCustomPayloadPacket.class);
			parse("CEditBookPacket", CEditBookPacket.class);
			parse("CEnchantItemPacket", CEnchantItemPacket.class);
			parse("CEntityActionPacket", CEntityActionPacket.class);
			parse("CHeldItemChangePacket", CHeldItemChangePacket.class);
			// parse("CClientStatusPacket.State.class");
			// parse("CEntityActionPacket.Action.class");
			parse("CInputPacket", CInputPacket.class);
			parse("CJigsawBlockGeneratePacket", CJigsawBlockGeneratePacket.class);
			parse("CKeepAlivePacket", CKeepAlivePacket.class);
			parse("CLockDifficultyPacket", CLockDifficultyPacket.class);
			parse("CMarkRecipeSeenPacket", CMarkRecipeSeenPacket.class);
			parse("CMoveVehiclePacket", CMoveVehiclePacket.class);
			parse("CPickItemPacket", CPickItemPacket.class);
			parse("CPlaceRecipePacket", CPlaceRecipePacket.class);
			parse("CPlayerAbilitiesPacket", CPlayerAbilitiesPacket.class);
			// parse("CPlayerDiggingPacket.Action.class");
			parse("CPlayerDiggingPacket", CPlayerDiggingPacket.class);
			// parse("CPlayerPacket.PositionPacket.class");
			parse("CPlayerPacket", CPlayerPacket.class);
			parse("CPlayerTryUseItemOnBlockPacket", CPlayerTryUseItemOnBlockPacket.class);
			parse("CPlayerTryUseItemPacket", CPlayerTryUseItemPacket.class);
			parse("CQueryEntityNBTPacket", CQueryEntityNBTPacket.class);
			parse("CQueryTileEntityNBTPacket", CQueryTileEntityNBTPacket.class);
			parse("CRenameItemPacket", CRenameItemPacket.class);
			parse("CResourcePackStatusPacket", CResourcePackStatusPacket.class);
			parse("CSeenAdvancementsPacket", CSeenAdvancementsPacket.class);
			parse("CSelectTradePacket", CSelectTradePacket.class);
			parse("CSetDifficultyPacket", CSetDifficultyPacket.class);
			parse("CSpectatePacket", CSpectatePacket.class);
			parse("CSteerBoatPacket", CSteerBoatPacket.class);
			parse("CTabCompletePacket", CTabCompletePacket.class);
			parse("CUpdateBeaconPacket", CUpdateBeaconPacket.class);
			parse("CUpdateCommandBlockPacket", CUpdateCommandBlockPacket.class);
			parse("CUpdateJigsawBlockPacket", CUpdateJigsawBlockPacket.class);
			parse("CUpdateMinecartCommandBlockPacket", CUpdateMinecartCommandBlockPacket.class);
			parse("CUpdateRecipeBookStatusPacket", CUpdateRecipeBookStatusPacket.class);
			parse("CUpdateSignPacket", CUpdateSignPacket.class);
			parse("CUpdateStructureBlockPacket", CUpdateStructureBlockPacket.class);
			parse("CUseEntityPacket", CUseEntityPacket.class);

			
			
			parse("SUpdateChunkPositionPacket", net.minecraft.network.play.server.SUpdateChunkPositionPacket.class);
			parse("SUpdateHealthPacket", net.minecraft.network.play.server.SUpdateHealthPacket.class);
			parse("SUpdateLightPacket", net.minecraft.network.play.server.SUpdateLightPacket.class);
			parse("SUpdateRecipesPacket", net.minecraft.network.play.server.SUpdateRecipesPacket.class);
			parse("SUpdateScorePacket", net.minecraft.network.play.server.SUpdateScorePacket.class);
			parse("SUpdateTileEntityPacket", net.minecraft.network.play.server.SUpdateTileEntityPacket.class);
			parse("SUpdateTimePacket", net.minecraft.network.play.server.SUpdateTimePacket.class);
			parse("SUpdateViewDistancePacket", net.minecraft.network.play.server.SUpdateViewDistancePacket.class);
			parse("SWindowItemsPacket", net.minecraft.network.play.server.SWindowItemsPacket.class);
			parse("SWindowPropertyPacket", net.minecraft.network.play.server.SWindowPropertyPacket.class);
			parse("SWorldBorderPacket", net.minecraft.network.play.server.SWorldBorderPacket.class);
			parse("SAdvancementInfoPacket", net.minecraft.network.play.server.SAdvancementInfoPacket.class);
			parse("SAnimateBlockBreakPacket", net.minecraft.network.play.server.SAnimateBlockBreakPacket.class);
			parse("SAnimateHandPacket", net.minecraft.network.play.server.SAnimateHandPacket.class);
			parse("SBlockActionPacket", net.minecraft.network.play.server.SBlockActionPacket.class);
			parse("SCameraPacket", net.minecraft.network.play.server.SCameraPacket.class);
			parse("SChangeBlockPacket", net.minecraft.network.play.server.SChangeBlockPacket.class);
			parse("SChangeGameStatePacket", net.minecraft.network.play.server.SChangeGameStatePacket.class);
			parse("SChatPacket", net.minecraft.network.play.server.SChatPacket.class);
			parse("SChunkDataPacket", net.minecraft.network.play.server.SChunkDataPacket.class);
			parse("SCloseWindowPacket", net.minecraft.network.play.server.SCloseWindowPacket.class);
			parse("SCollectItemPacket", net.minecraft.network.play.server.SCollectItemPacket.class);
			parse("SCombatPacket", net.minecraft.network.play.server.SCombatPacket.class);
			parse("SCommandListPacket", net.minecraft.network.play.server.SCommandListPacket.class);
			parse("SConfirmTransactionPacket", net.minecraft.network.play.server.SConfirmTransactionPacket.class);
			parse("SCooldownPacket", net.minecraft.network.play.server.SCooldownPacket.class);
			parse("SCustomPayloadPlayPacket", net.minecraft.network.play.server.SCustomPayloadPlayPacket.class);
			parse("SDestroyEntitiesPacket", net.minecraft.network.play.server.SDestroyEntitiesPacket.class);
			parse("SDisconnectPacket", net.minecraft.network.play.server.SDisconnectPacket.class);
			parse("SDisplayObjectivePacket", net.minecraft.network.play.server.SDisplayObjectivePacket.class);
			parse("SEntityEquipmentPacket", net.minecraft.network.play.server.SEntityEquipmentPacket.class);
			parse("SEntityHeadLookPacket", net.minecraft.network.play.server.SEntityHeadLookPacket.class);
			parse("SEntityMetadataPacket", net.minecraft.network.play.server.SEntityMetadataPacket.class);
			parse("SEntityPacket", net.minecraft.network.play.server.SEntityPacket.class);
			parse("SEntityPropertiesPacket", net.minecraft.network.play.server.SEntityPropertiesPacket.class);
			parse("SEntityStatusPacket", net.minecraft.network.play.server.SEntityStatusPacket.class);
			parse("SEntityTeleportPacket", net.minecraft.network.play.server.SEntityTeleportPacket.class);
			parse("SEntityVelocityPacket", net.minecraft.network.play.server.SEntityVelocityPacket.class);
			parse("SExplosionPacket", net.minecraft.network.play.server.SExplosionPacket.class);
			parse("SHeldItemChangePacket", net.minecraft.network.play.server.SHeldItemChangePacket.class);
			parse("SJoinGamePacket", net.minecraft.network.play.server.SJoinGamePacket.class);
			parse("SKeepAlivePacket", net.minecraft.network.play.server.SKeepAlivePacket.class);
			parse("SMapDataPacket", net.minecraft.network.play.server.SMapDataPacket.class);
			parse("SMerchantOffersPacket", net.minecraft.network.play.server.SMerchantOffersPacket.class);
			parse("SMountEntityPacket", net.minecraft.network.play.server.SMountEntityPacket.class);
			parse("SMoveVehiclePacket", net.minecraft.network.play.server.SMoveVehiclePacket.class);
			parse("SOpenBookWindowPacket", net.minecraft.network.play.server.SOpenBookWindowPacket.class);
			parse("SOpenHorseWindowPacket", net.minecraft.network.play.server.SOpenHorseWindowPacket.class);
			parse("SOpenSignMenuPacket", net.minecraft.network.play.server.SOpenSignMenuPacket.class);
			parse("SOpenWindowPacket", net.minecraft.network.play.server.SOpenWindowPacket.class);
			parse("SPlaceGhostRecipePacket", net.minecraft.network.play.server.SPlaceGhostRecipePacket.class);
			parse("SPlayEntityEffectPacket", net.minecraft.network.play.server.SPlayEntityEffectPacket.class);
			parse("SPlayerAbilitiesPacket", net.minecraft.network.play.server.SPlayerAbilitiesPacket.class);
			parse("SPlayerDiggingPacket", net.minecraft.network.play.server.SPlayerDiggingPacket.class);
			parse("SPlayerListHeaderFooterPacket", net.minecraft.network.play.server.SPlayerListHeaderFooterPacket.class);
			parse("SPlayerListItemPacket", net.minecraft.network.play.server.SPlayerListItemPacket.class);
			parse("SPlayerLookPacket", net.minecraft.network.play.server.SPlayerLookPacket.class);
			parse("SPlayerPositionLookPacket", net.minecraft.network.play.server.SPlayerPositionLookPacket.class);
			parse("SPlaySoundEffectPacket", net.minecraft.network.play.server.SPlaySoundEffectPacket.class);
			parse("SPlaySoundEventPacket", net.minecraft.network.play.server.SPlaySoundEventPacket.class);
			parse("SPlaySoundPacket", net.minecraft.network.play.server.SPlaySoundPacket.class);
			parse("SQueryNBTResponsePacket", net.minecraft.network.play.server.SQueryNBTResponsePacket.class);
			parse("SRecipeBookPacket", net.minecraft.network.play.server.SRecipeBookPacket.class);
			parse("SRemoveEntityEffectPacket", net.minecraft.network.play.server.SRemoveEntityEffectPacket.class);
			parse("SRespawnPacket", net.minecraft.network.play.server.SRespawnPacket.class);
			parse("SScoreboardObjectivePacket", net.minecraft.network.play.server.SScoreboardObjectivePacket.class);
			parse("SSelectAdvancementsTabPacket", net.minecraft.network.play.server.SSelectAdvancementsTabPacket.class);
			parse("SSendResourcePackPacket", net.minecraft.network.play.server.SSendResourcePackPacket.class);
			parse("SServerDifficultyPacket", net.minecraft.network.play.server.SServerDifficultyPacket.class);
			parse("SSetExperiencePacket", net.minecraft.network.play.server.SSetExperiencePacket.class);
			parse("SSetPassengersPacket", net.minecraft.network.play.server.SSetPassengersPacket.class);
			parse("SSetSlotPacket", net.minecraft.network.play.server.SSetSlotPacket.class);
			parse("SSpawnExperienceOrbPacket", net.minecraft.network.play.server.SSpawnExperienceOrbPacket.class);
			parse("SSpawnMobPacket", net.minecraft.network.play.server.SSpawnMobPacket.class);
			parse("SSpawnMovingSoundEffectPacket", net.minecraft.network.play.server.SSpawnMovingSoundEffectPacket.class);
			parse("SSpawnObjectPacket", net.minecraft.network.play.server.SSpawnObjectPacket.class);
			parse("SSpawnPaintingPacket", net.minecraft.network.play.server.SSpawnPaintingPacket.class);
			parse("SSpawnParticlePacket", net.minecraft.network.play.server.SSpawnParticlePacket.class);
			parse("SSpawnPlayerPacket", net.minecraft.network.play.server.SSpawnPlayerPacket.class);
			parse("SStatisticsPacket", net.minecraft.network.play.server.SStatisticsPacket.class);
			parse("SStopSoundPacket", net.minecraft.network.play.server.SStopSoundPacket.class);
			parse("STabCompletePacket", net.minecraft.network.play.server.STabCompletePacket.class);
			parse("STagsListPacket", net.minecraft.network.play.server.STagsListPacket.class);
			parse("STeamsPacket", net.minecraft.network.play.server.STeamsPacket.class);
			parse("STitlePacket", net.minecraft.network.play.server.STitlePacket.class);
			parse("SUnloadChunkPacket", net.minecraft.network.play.server.SUnloadChunkPacket.class);
			parse("SUpdateBossInfoPacket", net.minecraft.network.play.server.SUpdateBossInfoPacket.class);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parse(String packageName) {
        String packagePath = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            // РџРѕР»СѓС‡Р°РµРј СЃРїРёСЃРѕРє РєР»Р°СЃСЃРѕРІ РІ СѓРєР°Р·Р°РЅРЅРѕРј РїР°РєРµС‚Рµ
            List<Class<?>> classes = new ArrayList<>();
            String packageDirName = packagePath;

            java.net.URL packageURL;
            packageURL = classLoader.getResource(packageDirName);
            String packageURLStr = packageURL.getFile();

            for (String fileName : new File(packageURLStr).list()) {
                if (fileName.endsWith(".class")) {
                    String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                    Class<?> clazz = Class.forName(className);
                    String packageName1 = clazz.getCanonicalName();
                    
                    /*
                    packageName1 = packageName1
                    		.replace(".State", "$State")
                    		.replace(".Action", "$Action")
                    		.replace(".PositionPacket", "$PositionPacket")
                    		.replace(".PositionRotationPacket", "$PositionRotationPacket")
                    		.replace(".RotationPacket", "$RotationPacket")
                    		.replace(".Event", "$Event")
                    		.replace(".Entry", "$Entry")
                    		.replace(".LookPacket", "$LookPacket")
                    		.replace(".MovePacket", "$MovePacket")
                    		.replace(".RelativeMovePacket", "$RelativeMovePacket")
                    		.replace(".Snapshot", "$Snapshot")
                    		.replace(".AddPlayerData", "$AddPlayerData")
                    		.replace(".Flags", "$Flags")
                    		.replace(".Type", "$Type");
                    */
                    
                    parse(clazz.getSimpleName(), clazz.getName());
                }
            }
        } catch (Exception e) {
        //    e.printStackTrace();
        }
    }
	
	private void set(String name, String ru, LuaValue val) {
		engine.set(name, val);
		engine.set(ru, val);
	}
	
	private void parse(String name, String class1) throws Exception {
		LuaValue javaClass = engine.get("luajava").get("bindClass");
	    LuaValue myClass = javaClass.call(class1);
	    engine.set(name, myClass);
		//run(String.format("local %s = luajava.bindClass(\"%s\", \"%s\");", name, name, class1.getCanonicalName()));
	}
	
	private void parse(Class<?> class1) throws Exception {
		LuaValue javaClass = engine.get("luajava").get("bindClass");
	    LuaValue myClass = javaClass.call(class1.getCanonicalName());
	    engine.set(class1.getSimpleName(), myClass);
		//run(String.format("local %s = luajava.bindClass(\"%s\", \"%s\");", name, name, class1.getCanonicalName()));
	}
	
	private void parse(String name, Class<?> class1) throws Exception {
		engine.set(name, CoerceJavaToLua.coerce(class1));
		//run(String.format("local %s = luajava.bindClass(\"%s\", \"%s\");", name, name, class1.getCanonicalName()));
	}
	
	public void run(String script) {
		try {
			engine.load(script); 
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
	}
}
