package fun.rockstarity.client.modules.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.EventWorldChange;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.helpers.game.Server;
import fun.rockstarity.api.helpers.math.MathUtility;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.helpers.player.Player;
import fun.rockstarity.api.helpers.render.PositionTracker;
import fun.rockstarity.api.helpers.render.Render;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Info;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.list.ColorPicker;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Position;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.modules.settings.list.Select.Element;
import fun.rockstarity.api.modules.settings.list.Slider;
import fun.rockstarity.api.render.animation.Animation;
import fun.rockstarity.api.render.animation.Easing;
import fun.rockstarity.api.render.color.FixColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;

/**
 * @author ConeTin
 * @since 4 июн. 2024 г.
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Info(name="Particles", desc="Красивые частицы", type=Category.RENDER)
public class Particles extends Module {
	@Getter Map<Element, ParticleSettings> elementSettings = new HashMap<>();
	
	Select spawn = new Select(this, "Спавнить").min(1).desc("Выбор того, при каких событиях будут спавниться партиклы");
	Element always = new Element(spawn, "Просто так").set(true);
	Element hit = new Element(spawn, "При ударе");
	@Getter Element totem = new Element(spawn, "При сносе тотема");
	Element kill = new Element(spawn, "При убийстве");
	Element walk = new Element(spawn, "При ходьбе");
	Element jump = new Element(spawn, "При прыжке");
	
	Select alwaysAdd = new Select(always, "Просто так").hide(() -> !always.get()).desc("Разные косметические дополнения для \"Просто так\"");
	Element onGround = new Element(alwaysAdd, "Спавнить на земле");
	Element alwaysPhysic = new Element(alwaysAdd, "Физика").set(true);
	
	Select hitAdd = new Select(hit, "При ударе").hide(() -> !hit.get()).desc("Разные косметические дополнения для \"При ударе\"");
	Element health = new Element(hitAdd, "Зависеть от здоровья").set(true);
	Element golden = new Element(hitAdd, "Золотые сердечки").set(true);
	Element hitPhysic = new Element(hitAdd, "Физика").set(true);
	
	Select killAdd = new Select(kill, "При убийстве").hide(() -> !kill.get()).desc("Разные косметические дополнения для \"При убийстве\"");
	Element killPhysic = new Element(killAdd, "Физика").set(true);
	Element killUp = new Element(killAdd, "Подбрасывать вверх").hide(() -> !killPhysic.get()).set(true);
	
	Select walkAdd = new Select(walk, "При ходьбе").hide(() -> !walk.get()).desc("Разные косметические дополнения для \"При ходьбе\"");
	Element mass = new Element(walkAdd, "Кучковать").set(true);
	Element walkPhysic = new Element(walkAdd, "Физика").set(true);
	Element walkFirstPerson = new Element(walkAdd, "От первого лица");
	
	Select jumpAdd = new Select(jump, "При прыжке").hide(() -> !jump.get()).desc("Разные косметические дополнения для \"При прыжке\"");
	Element jumpPhysic = new Element(jumpAdd, "Физика").set(true);
	Element jumpFirstPerson = new Element(jumpAdd, "От первого лица");
	
	Select totemAdd = new Select(totem, "При сносе тотема").hide(() -> !totem.get()).desc("Разные косметические дополнения для \"При сносе тотема\"");
	Element totemPhysic = new Element(totemAdd, "Физика").set(true);
	
	Slider range = new Slider(always, "Дистанция").hide(() -> !always.get()).min(4).max(32).inc(1).set(16).desc("Дистанция спавна частиц при \"Просто так\"");
	
	Mode directionMode = new Mode(always, "Направление полета").hide(() -> !always.get()).desc("Настройка направления полета при \"Просто так\"");;

	Mode.Element off = new Mode.Element(directionMode, "Прямо");
	Mode.Element player = new Mode.Element(directionMode, "Относительно игрока");
	Mode.Element world = new Mode.Element(directionMode, "Относительно мира");
	
	Position direction = new Position(always, "Направление").hide(() -> !always.get() || directionMode.is(off));
	
	private final List<Particle> particles = new ArrayList<>();

    private void clear() {
        particles.clear();
    }
    
    public Particles() {
    	createEventSettings(always, "Просто так");
        createEventSettings(hit, "При ударе");
        createEventSettings(kill, "При убийстве");
        createEventSettings(walk, "При ходьбе");
        createEventSettings(jump, "При прыжке");
        createEventSettings(totem, "При сносе тотема");
    }

	@Override
	public void onEvent(Event event) {
		if (event instanceof EventWorldChange) clear();
		
		if (event instanceof EventMotion e && always.get()) {
			ParticleSettings settings = elementSettings.get(always);
			float range = this.range.get();
	        for (int i = 0; i < settings.count.get(); i++) {
	            Vector3d additional = mc.player.getPositionVec().add(MathUtility.random(-range, range), 0, MathUtility.random(-range, range));
	            BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));
	            
	            Vector3d addMotion = Vector3d.ZERO;
	            float mul = 2;
	            
	            if (directionMode.is(player)) {
	    			double xY = Math.sin(Math.toRadians(mc.player.rotationYaw));
	    			double zY = -Math.cos(Math.toRadians(mc.player.rotationYaw));
	    			
	    			double xX = -Math.sin(Math.toRadians(mc.player.rotationYaw+90));
	    			double zX = Math.cos(Math.toRadians(mc.player.rotationYaw+90));
	    			
	            	addMotion = new Vector3d(
	            			xY * direction.getY() * mul
	            			+ 
	            			xX * direction.getX() * mul,
	            			
	            			0, 
	            			
	            			zY * direction.getY() * mul
	            			+ 
	            			zX * direction.getX() * mul
	            	);
	            } else if (directionMode.is(world)) {
	            	addMotion = new Vector3d(direction.getX() * mul, 0, direction.getY() * mul);
	            }
	            
	            spawnParticle(new Vector3d(pos.getX() + MathUtility.random(0, 1), onGround.get() ? pos.getY() : mc.player.getPosY() + MathUtility.random(mc.player.getHeight(), range), pos.getZ() + MathUtility.random(0, 1)), new Vector3d(addMotion.x, MathUtility.random(0.0, 1) * (onGround.get() ? 1 : -1), addMotion.z), settings.color.get(MathUtility.randomInt(0, 500)), alwaysPhysic.get(), always);
	        }
		}
		
		if (event instanceof EventMotion e && walk.get() && mc.player.getMotion().length() > 0.1f && (mc.getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON || walkFirstPerson.get())) {
			ParticleSettings settings = elementSettings.get(walk);
			LivingEntity entity = mc.player;
			float motion = mass.get() ? 0.1f : 0.5f;
			float range = this.range.get();
			for (int i = 0; i < settings.count.get(); i++) {
	            Vector3d pos = entity.getPositionVec().add(0, mass.get() ? entity.getHeight()/3F : MathUtility.random(0, entity.getHeight()), 0);
	            spawnParticle(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d(MathUtility.random(-motion, motion), mass.get() && !walkPhysic.get() ? MathUtility.random(-0.05F, 0.05F) : MathUtility.random(-1, 0), MathUtility.random(-motion, motion)), settings.color.get(MathUtility.randomInt(0, 500)), walkPhysic.get(), walk);
	        }
		}
		
		if (event instanceof EventJump e && jump.get() && (mc.getGameSettings().getPointOfView() != PointOfView.FIRST_PERSON || jumpFirstPerson.get())) {
			ParticleSettings settings = elementSettings.get(jump);
			LivingEntity entity = mc.player;
			float motion = 1;
	        for (int i = 0; i < settings.count.get() * 2F; i++) {
	            Vector3d pos = entity.getPositionVec().add(0,0.01f,0);
	            spawnParticle(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d(MathUtility.random(-motion, motion), jumpPhysic.get() ? MathUtility.random(0, 1) : 0, MathUtility.random(-motion, motion)), settings.color.get(MathUtility.randomInt(0, 500)), true, jump);
	        }
		}
		
		if (event instanceof EventAttack e && hit.get()) {
			ParticleSettings settings = elementSettings.get(hit);
			LivingEntity entity = e.getTarget();
			float health = Math.max(0, Math.min(1, (Server.isServerForHPFix() ? entity.getRealHealth() / 20F : (entity.getHealth()) / (entity.getMaxHealth()))));
			float motion = 1;
	        for (int i = 0; i < settings.count.get() * 2F; i++) {
	            Vector3d pos = entity.getPositionVec().add(0, MathUtility.random(0, entity.getHeight()), 0);
	            FixColor pColor = settings.color.get(MathUtility.randomInt(0, 500));
	            spawnParticle(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d(MathUtility.random(-motion, motion), MathUtility.random(-motion, hitPhysic.get() ? motion : 0), MathUtility.random(-motion, motion)), entity.getAbsorptionAmount() > 0 && golden.get() ? FixColor.YELLOW : (this.health.get() ? pColor.move(FixColor.RED, (1-health) * 0.7F) : pColor), hitPhysic.get(), hit);
	        }
		}
		
		if (event instanceof EventKill e && kill.get()) {
			ParticleSettings settings = elementSettings.get(kill);
			LivingEntity entity = e.getTarget();
			float motion = 1;
	        for (int i = 0; i < settings.count.get() * 5F; i++) {
	            Vector3d pos = entity.getPositionVec().add(0, MathUtility.random(0, entity.getHeight()), 0);
	            FixColor pColor = settings.color.get(MathUtility.randomInt(0, 500));
	            spawnParticle(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d(MathUtility.random(-motion, motion), killUp.get() ? MathUtility.random(motion, 3) : MathUtility.random(-motion, killPhysic.get() ? motion : 0), MathUtility.random(-motion, motion)), pColor, killPhysic.get(), kill);
	        }
		}
		
		if (event instanceof EventTotemBreak e && totem.get()) {
			ParticleSettings settings = elementSettings.get(totem);
			Entity entity = e.getEntity();
			float motion = 1;
	        for (int i = 0; i < settings.count.get() * 5F; i++) {
	            Vector3d pos = entity.getPositionVec().add(0, MathUtility.random(0, entity.getHeight()), 0);
	            FixColor pColor = settings.color.get(MathUtility.randomInt(0, 500));
	            spawnParticle(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d(MathUtility.random(-motion, motion), MathUtility.random(-motion, totemPhysic.get() ? motion : 0), MathUtility.random(-motion, motion)), pColor, totemPhysic.get(), totem);
	        }
		}
		
		if (event instanceof EventRender3D e) {
			setupRenderState();
	        renderParticles(e.getMatrixStack(), particles);
	        resetRenderState();
		}
		
	}
	
	@Override
	public void onDisable() {
		clear();
	}

	@Override
	public void onEnable() {
		clear();
	}
	
	private void renderParticles(MatrixStack matrix, List<Particle> particles) {
        removeExpiredParticles(particles);
        if (particles.isEmpty()) return;

        matrix.push();
        for (Particle particle : particles) {
            particle.update();

            if (!PositionTracker.isInView(particle.position)) continue;

            Animation animation = particle.animation;
            float alpha = animation.get();
            
            animation.setSpeed((int) 500);
            animation.setForward(!particle.time.passed((long)particle.liveTime + particle.addTime));

            FixColor color = particle.color.alpha(alpha * (0.3f + 0.7f * ((Math.sin((System.currentTimeMillis() - particle.spawnTime + particle.addTime) / 200D) + 1F) / 2F)));
            Vector3d vec = particle.position;
            float x = (float) vec.x;
            float y = (float) vec.y;
            float z = (float) vec.z;

            renderParticle(matrix, particle, x, y, z, color);
        }
        matrix.pop();
    }
	
	private void removeExpiredParticles(List<Particle> particles) {
        particles.removeIf(particle -> particle.time.passed((long)particle.liveTime + 500 + particle.addTime));
    }

    private void renderParticle(MatrixStack matrix, Particle particle, float x, float y, float z, FixColor color) {
        float pos = particle.size;
        
        matrix.push();
        Render.setupOrientationMatrix(matrix, x, y, z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));
       // if (particle.type().rotatable()) matrix.rotate(Vector3f.ZP.rotationDegrees(particle.rotate()));
        if (particle.type.equals("Треугольник") || particle.type.equals("Ромб") || particle.type.equals("Крест") || particle.type.equals("Звезда") || particle.type.equals("Снежинка") || particle.type.equals("Луна"))
        matrix.rotate(Vector3f.ZP.rotationDegrees((float) ((System.currentTimeMillis() - particle.spawnTime) / 20D)));
        matrix.push();
        matrix.translate(0, -pos, -pos);

        /*
        //if (glow.get()) {
        	mc.getTextureManager().bindTexture(NativeHelper.getImageResource("masks/particles/Glow.png"));
            RectUtil.drawRect(matrix, -pos * 4, -pos * 4, pos * 8, pos * 8, color.alpha(0.1f).getRGB(), true, true);
        //}
            
        mc.getTextureManager().bindTexture(NativeHelper.getImageResource("masks/particles/" + particle.type + ".png"));
        RectUtil.drawRect(matrix, -pos, -pos, pos * 2, pos * 2, color.getRGB(), true, true);
        
        //if (glow.get()) {
            RectUtil.drawRect(matrix, -pos / 2, -pos / 2, pos, pos, color.getRGB(), true, true);
        //}
        */
        
        //if (glow.get()) {
        	Render.drawImage(matrix, "masks/particles/Glow.png", -pos * 4, -pos * 4, 0, pos * 8, pos * 8, color.alpha(0.1f));
	    //}
	        
	    Render.drawImage(matrix, "masks/particles/" + particle.index + ".png", -pos, -pos, 0, pos * 2, pos * 2, color);
	    
        matrix.pop();
        matrix.pop();
        matrix.pop();
    }
	
	private void setupRenderState() {
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableCull();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

	}

	private void resetRenderState() {
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderSystem.clearCurrentColor();
		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
		RenderSystem.enableAlphaTest();
	}
	    
	private void spawnParticle(Vector3d position, Vector3d velocity, FixColor color, boolean physic, Element spawnType) {
		ParticleSettings settings = elementSettings.get(spawnType);
		
		float size = settings.size.get();
		float strength = settings.strength.get();
		float liveTime = settings.liveTime.get();
		
		size = 0.05F + size * 0.2F;
		
		Element elmt = settings.select.getRandomEnabledElement();
		
        particles.add(new Particle(elmt.getName(),
                position.add(0, size, 0),
                velocity,
                settings.select.getElements().indexOf(elmt),
                color,
                size,
                physic,
				spawnType,
				strength,
				liveTime
        ));
    }
	
	private void createEventSettings(Element element, String displayName) {
        ParticleSettings settings = new ParticleSettings(element, displayName);
        elementSettings.put(element, settings);
    }

	@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public class ParticleSettings {
        Slider count;
        Slider strength;
        Slider size;
        ColorPicker color;
        Select select;
    	Slider liveTime;

        ParticleSettings(Element parent, String displayName) {
        	select = new Select(parent, "Виды").min(1).desc("Выбор видов частиц. При спавне будет выбираться случайный из списка");
        	
        	Element star = new Element(select, "Звезда").set(true);
        	Element cross = new Element(select, "Крест").set(true);
        	Element moon = new Element(select, "Луна").set(true);
        	Element lightning = new Element(select, "Молния").set(true);
        	Element cloud = new Element(select, "Облако").set(true);
        	Element rhomb = new Element(select, "Ромб").set(true);
        	Element heart = new Element(select, "Сердце").set(true);
        	Element snow = new Element(select, "Снежинка").set(true);
        	Element triangle = new Element(select, "Треугольник").set(true);
        	Element dota = new Element(select, "Dota").set(true);
        	
        	liveTime = new Slider(parent, "Время жизни").min(300).max(5000).inc(250).set(1250).desc("Время, которое будут жить частицы");
        	
            count = new Slider(parent, "Количество")
                    .min(1).max(32).inc(1).set(16)
                    .desc("Количество частиц для " + displayName);

            strength = new Slider(parent, "Сила")
                    .min(0.1f).max(1).inc(0.1f).set(0.8f)
                    .desc("Сила частиц для " + displayName);

            size = new Slider(parent, "Размер")
                    .min(0.1f).max(1).inc(0.1f).set(0.5f)
                    .desc("Размер частиц для " + displayName);

            color = new ColorPicker(parent, "Цвет")
                    .add(FixColor.WHITE)
                    .desc("Цвет частиц для " + displayName);
        }
    }
	
    class Particle {
        private final long spawnTime = System.currentTimeMillis();
        private long addTime;
        private final String type;
        private final AxisAlignedBB box;
        private Vector3d position;
        private Vector3d velocity;
        private final int index;
        private final FixColor color;
        private final float size;
        private final boolean physic;
		private final Element spawnType;
		private final float strength;
		private final float liveTime;

        private TimerUtility time = new TimerUtility();
        private final Animation animation = new Animation().setEasing(Easing.BOTH_CIRC);

        private long lastUpdateTime = System.currentTimeMillis();
        
        public Particle(String type, final Vector3d position, final Vector3d velocity, final int index, FixColor color, float size, boolean physic, Element spawnType, float strength, float liveTime) {
            this.type = type;
            this.box = new AxisAlignedBB(position, position).grow(size);
            this.position = position;
            this.velocity = velocity.mul(0.01F);
            this.index = index;
            this.color = color;
            this.size = size;
            this.physic = physic;
			this.spawnType = spawnType;
            this.time.reset();
            this.addTime = MathUtility.randomInt(0, 2000);
            this.strength = strength;
            this.liveTime = liveTime;
        }

        public void update() {
            long currentTime = System.currentTimeMillis();
			float deltaTime = (currentTime - lastUpdateTime) / (10.0f - 8 * strength);

            if (physic) {
                if (Player.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                    this.velocity = this.velocity.mul(1, 1, -0.8);
                }
                if (Player.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                    this.velocity = this.velocity.mul(0.999, -0.6, 0.999);
                }
                if (Player.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                    this.velocity = this.velocity.mul(-0.8, 1, 1);
                }
                this.velocity = this.velocity.mul(0.999999).subtract(new Vector3d(0, 0.00005, 0));
            }

            this.position = this.position.add(this.velocity.mul(deltaTime));
            lastUpdateTime = currentTime;
        }
        
        
    }
}
