package fun.rockstarity.api.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.binds.Bind;
import fun.rockstarity.client.modules.combat.AimAssist;
import fun.rockstarity.client.modules.combat.AimBot;
import fun.rockstarity.client.modules.combat.Aura;
import fun.rockstarity.client.modules.combat.AutoArmor;
import fun.rockstarity.client.modules.combat.AutoExplosion;
import fun.rockstarity.client.modules.combat.AutoGApple;
import fun.rockstarity.client.modules.combat.AutoPotion;
import fun.rockstarity.client.modules.combat.AutoSoup;
import fun.rockstarity.client.modules.combat.AutoTotem;
import fun.rockstarity.client.modules.combat.BackTrack;
import fun.rockstarity.client.modules.combat.Criticals;
import fun.rockstarity.client.modules.combat.ElytraTarget;
import fun.rockstarity.client.modules.combat.FastBow;
import fun.rockstarity.client.modules.combat.HitBoxes;
import fun.rockstarity.client.modules.combat.SuperBow;
import fun.rockstarity.client.modules.combat.TriggerBot;
import fun.rockstarity.client.modules.combat.Velocity;
import fun.rockstarity.client.modules.move.AutoLoot;
import fun.rockstarity.client.modules.move.AutoSprint;
import fun.rockstarity.client.modules.move.BoatControl;
import fun.rockstarity.client.modules.move.Flight;
import fun.rockstarity.client.modules.move.Jesus;
import fun.rockstarity.client.modules.move.LongJump;
import fun.rockstarity.client.modules.move.NoClip;
import fun.rockstarity.client.modules.move.Speed;
import fun.rockstarity.client.modules.move.Spider;
import fun.rockstarity.client.modules.move.Strafe;
import fun.rockstarity.client.modules.move.Timer;
import fun.rockstarity.client.modules.move.WaterLeave;
import fun.rockstarity.client.modules.move.WaterSpeed;
import fun.rockstarity.client.modules.other.Assist;
import fun.rockstarity.client.modules.other.AuctionUtils;
import fun.rockstarity.client.modules.other.AutoAccept;
import fun.rockstarity.client.modules.other.AutoAuth;
import fun.rockstarity.client.modules.other.AutoBrew;
import fun.rockstarity.client.modules.other.AutoDuels;
import fun.rockstarity.client.modules.other.AutoDupe;
import fun.rockstarity.client.modules.other.AutoFarmNew;
import fun.rockstarity.client.modules.other.AutoInvisible;
import fun.rockstarity.client.modules.other.AutoJoin;
import fun.rockstarity.client.modules.other.AutoKit;
import fun.rockstarity.client.modules.other.AutoLeave;
import fun.rockstarity.client.modules.other.AutoPilot;
import fun.rockstarity.client.modules.other.Baritone;
import fun.rockstarity.client.modules.other.ChatUtils;
import fun.rockstarity.client.modules.other.DeathCoordinates;
import fun.rockstarity.client.modules.other.ElytraUtils;
import fun.rockstarity.client.modules.other.Globals;
import fun.rockstarity.client.modules.other.GodExploit;
import fun.rockstarity.client.modules.other.ItemTimer;
import fun.rockstarity.client.modules.other.KTLeave;
import fun.rockstarity.client.modules.other.MultiActions;
import fun.rockstarity.client.modules.other.NameProtect;
import fun.rockstarity.client.modules.other.Panic;
import fun.rockstarity.client.modules.other.PotionCombiner;
import fun.rockstarity.client.modules.other.PotionRemover;
import fun.rockstarity.client.modules.other.RussianRoulette;
import fun.rockstarity.client.modules.other.Sounds;
import fun.rockstarity.client.modules.player.AutoCreeperFarm;
import fun.rockstarity.client.modules.player.AutoDodge;
import fun.rockstarity.client.modules.player.AutoEat;
import fun.rockstarity.client.modules.player.AutoPearl;
import fun.rockstarity.client.modules.player.AutoSwap;
import fun.rockstarity.client.modules.player.AutoUse;
import fun.rockstarity.client.modules.player.Blink;
import fun.rockstarity.client.modules.player.ClickTrough;
import fun.rockstarity.client.modules.player.EnderChest;
import fun.rockstarity.client.modules.player.FreeCam;
import fun.rockstarity.client.modules.player.InvCleaner;
import fun.rockstarity.client.modules.player.InvUtils;
import fun.rockstarity.client.modules.player.MiddleClick;
import fun.rockstarity.client.modules.player.MineHelper;
import fun.rockstarity.client.modules.player.NoDelay;
import fun.rockstarity.client.modules.player.NoFall;
import fun.rockstarity.client.modules.player.NoInteract;
import fun.rockstarity.client.modules.player.NoPush;
import fun.rockstarity.client.modules.player.NoRotate;
import fun.rockstarity.client.modules.player.NoSlow;
import fun.rockstarity.client.modules.player.Nuker;
import fun.rockstarity.client.modules.player.Phase;
import fun.rockstarity.client.modules.player.PlayerUtils;
import fun.rockstarity.client.modules.player.Reach;
import fun.rockstarity.client.modules.player.Sneak;
import fun.rockstarity.client.modules.player.SpeedMine;
import fun.rockstarity.client.modules.player.Stealer;
import fun.rockstarity.client.modules.player.WebUtils;
import fun.rockstarity.client.modules.render.AntiInvisible;
import fun.rockstarity.client.modules.render.Arrows;
import fun.rockstarity.client.modules.render.Beautifully;
import fun.rockstarity.client.modules.render.BlockESP;
import fun.rockstarity.client.modules.render.ClickGui;
import fun.rockstarity.client.modules.render.Constructor;
import fun.rockstarity.client.modules.render.Cosmetics;
import fun.rockstarity.client.modules.render.ESP;
import fun.rockstarity.client.modules.render.FreeLook;
import fun.rockstarity.client.modules.render.Hand;
import fun.rockstarity.client.modules.render.Interface;
import fun.rockstarity.client.modules.render.KillEffects;
import fun.rockstarity.client.modules.render.NameTags;
import fun.rockstarity.client.modules.render.NoRender;
import fun.rockstarity.client.modules.render.ObjectsInfo;
import fun.rockstarity.client.modules.render.Particles;
import fun.rockstarity.client.modules.render.Prediction;
import fun.rockstarity.client.modules.render.TNTTimer;
import fun.rockstarity.client.modules.render.TargetESP;
import fun.rockstarity.client.modules.render.Torus;
import fun.rockstarity.client.modules.render.World;
import fun.rockstarity.client.modules.render.XRay;
import lombok.Getter;

/**
 * @author ConeTin
 * @since 2 дек. 2023 г.
 */


@Getter
/*
 * Мы познакомились с тобой на говно-коде, вроде.
Я обсирал твой говно-код, и ты была не против.
Я показывал свой селфкод тебе, совсем тебя не помня,
И мы пошли учить тебя на скоростном солиде.

Ты не пастер, ты не такая, как все пастерки.
Ты особенная и с говнокодом в душе.
Хоть я нормально кодю, но кажется мне,
Что я не пастер.
 */
public class Modules extends HashMap<Class, Module> implements IAccess {
	
	private List<Module> modules = new ArrayList<>();
	
	public Modules() {
		Arrays.stream(new Module[] {
				//Combat
				new AimAssist(),
				new AutoArmor(),
				new Aura(),
				new AutoGApple(),
				new AutoTotem(),
				new AutoPotion(),
				new Velocity(),
				new HitBoxes(),
				new TriggerBot(),
				new FastBow(),
				new SuperBow(),
				new BackTrack(),
				new AutoExplosion(),
				new AutoSoup(),
				new AimBot(),
				new Criticals(),
				new ElytraTarget(),
				//Move
				new AutoSprint().set(true),
				new Flight(),
				new Strafe(),
				new Speed(),
				new Jesus(),
				new LongJump(),
				new NoSlow(),
				new Timer(),
				new WaterSpeed(),
				new Spider(),
				new WaterLeave(),
				new NoClip(),
				new BoatControl(),
				new AutoLoot(),
				//Render
				new ESP(),
				new World(),
				new Interface().set(true),
				new NoRender(),
				new NameTags(),
			//	new GlowESP(),
				new ClickGui().addBind(new Bind(GLFW.GLFW_KEY_RIGHT_SHIFT)),
				new Hand(),
				new Prediction(),
				new AntiInvisible(),
				new TNTTimer(),
				new Arrows(),
				new Particles(),
				new XRay(),
				new Constructor(),
				new TargetESP().set(true),
				new Beautifully(),
				new FreeLook(),
				new BlockESP(),
				new KillEffects(),
				new Cosmetics().set(true),
				new ObjectsInfo(),
				new Torus(),
				//Player
				new EnderChest(),
				new Phase(),
				new Sounds().set(true),
				new FreeCam(),
				new EnderChest(),
				new AutoUse(),
				new AutoEat(),
				new InvUtils().set(true),
				new Blink(),
				new InvCleaner(),
				new WebUtils(),
				new NoDelay(),
				new AutoSwap(),
				new NoInteract(),
				new Stealer(),
				new SpeedMine(),
				new PlayerUtils(),
				new Sneak(),
				new NoRotate(),
				new Reach(),
				new NoFall(),
				new AutoCreeperFarm(),
				new MiddleClick(),
				new AutoPearl(),
				new Nuker(),
				new ClickTrough(),
				new PotionRemover(),
				new AutoDodge(),
				new MineHelper(),
				//Other
				new AutoKit(),
				new Baritone(),
				new AutoFarmNew(),
				new KTLeave(),
				new PotionCombiner(),
				new Globals(),
				new ElytraUtils(),
				new AutoLeave(),
				new Panic(),
				new AutoInvisible(),
				new Assist().set(true),
				new AuctionUtils(),
				new ItemTimer(),
				new AutoDuels(),
				new AutoPilot(),
				new DeathCoordinates(),
				new AutoAuth(),
				new MultiActions(),
				new NoPush(),
				new AutoJoin(),
				new GodExploit(),
				new NameProtect(),
				new RussianRoulette(),
				new ChatUtils().set(true),
				new AutoAccept(),
				new AutoBrew(),
				new AutoDupe(),
		}).forEach(this::add);
	}


	public void add(Module module) {
		if (module.getClass().isAnnotationPresent(Info.class)) {
			Info info = module.getClass().getAnnotation(Info.class);
			module.setInfo(new ModuleInfo(info.name(), info.desc(), info.type(), info.module()));
		}
		
		this.put(module.getClass(), module);
		
		modules = new ArrayList<>(values());
		modules.sort(Comparator.comparingInt(Module::getPriority));
	}

	public void remove(Class moduleClass) {
		super.remove(moduleClass);
	}
	

	public <T extends Module> T get(Class<T> moduleClass) {
        return moduleClass.cast(super.get(moduleClass));
    }
	
}
