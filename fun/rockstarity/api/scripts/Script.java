package fun.rockstarity.api.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.lib.OneArgFunction;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import fun.rockstarity.api.binds.Bindable;
import fun.rockstarity.api.events.Event;
import fun.rockstarity.api.events.list.game.EventChat;
import fun.rockstarity.api.events.list.game.EventKill;
import fun.rockstarity.api.events.list.game.EventShutdown;
import fun.rockstarity.api.events.list.game.EventTick;
import fun.rockstarity.api.events.list.game.EventTotemBreak;
import fun.rockstarity.api.events.list.game.client.EventAlert;
import fun.rockstarity.api.events.list.game.client.EventToggle;
import fun.rockstarity.api.events.list.game.inputs.EventInput;
import fun.rockstarity.api.events.list.game.inputs.EventKey;
import fun.rockstarity.api.events.list.game.packet.EventReceivePacket;
import fun.rockstarity.api.events.list.game.packet.EventSendPacket;
import fun.rockstarity.api.events.list.lua.EventLuaAttack;
import fun.rockstarity.api.events.list.lua.EventLuaItem;
import fun.rockstarity.api.events.list.lua.EventLuaKill;
import fun.rockstarity.api.events.list.lua.EventLuaTotem;
import fun.rockstarity.api.events.list.player.EventAttack;
import fun.rockstarity.api.events.list.player.EventDeath;
import fun.rockstarity.api.events.list.player.EventJump;
import fun.rockstarity.api.events.list.player.EventMotion;
import fun.rockstarity.api.events.list.player.EventMotionMove;
import fun.rockstarity.api.events.list.player.EventMove;
import fun.rockstarity.api.events.list.player.EventUpdate;
import fun.rockstarity.api.events.list.render.EventRender2D;
import fun.rockstarity.api.events.list.render.EventRender3D;
import fun.rockstarity.api.events.list.render.entity.EventRenderItem;
import fun.rockstarity.api.modules.Category;
import fun.rockstarity.api.modules.Module;
import fun.rockstarity.api.modules.settings.Setting;
import fun.rockstarity.api.modules.settings.list.Mode;
import fun.rockstarity.api.modules.settings.list.Select;
import fun.rockstarity.api.render.ui.clickgui.GlyphType;
import fun.rockstarity.api.render.ui.clickgui.SettingRect;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.scripts.wrappers.Events;
import fun.rockstarity.api.scripts.wrappers.base.LivingEntityBase;
import fun.rockstarity.api.scripts.wrappers.base.MatrixBase;
import fun.rockstarity.api.scripts.wrappers.base.ModuleBase;
import fun.rockstarity.api.scripts.wrappers.base.ScriptBase;
import fun.rockstarity.api.secure.Debugger;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;

@Getter @Setter
public class Script extends Bindable {
    private String name, desc, content;
    private String[] devs;
    private boolean enabled;
    private File file;
    private ArrayList<ModuleBase> bases = new ArrayList<>();
    private ArrayList<Mode.Element> scriptModes = new ArrayList<>();
    private ArrayList<Select.Element> scriptSelects = new ArrayList<>();
    private ArrayList<Setting> scriptSets = new ArrayList<>();
    private ArrayList<Module> scriptModules = new ArrayList<>();
    private ArrayList<Draggable> scriptDrags = new ArrayList<>();
    private Globals engine;
    @Getter
    private static Script current;
    @Getter
    private static Event event;
    private boolean changing = false;
    @Getter
    private static boolean active3d;
    private ScriptImports imports;
    @Getter
    private final boolean market;

    private static final Comparator<Object> SORT_METHOD = Comparator.comparing(m -> {
        Module module = (Module) m;
        return module.getInfo().name();
    }).reversed();
    
    public Events.Event 
    motion_move = new Events.Event(EventMotionMove.class),
    render_2d = new Events.Event(EventRender2D.class),
    render_3d = new Events.Event(EventRender3D.class),
    update = new Events.Event(EventUpdate.class),
    send_packet = new Events.Event(EventSendPacket.class),
    receive_packet = new Events.Event(EventReceivePacket.class),
    motion = new Events.Event(EventMotion.class),
    key = new Events.Event(EventKey.class),
    jump = new Events.Event(EventJump.class),
    chat = new Events.Event(EventChat.class),
    attack = new Events.Event(EventAttack.class),
    shutdown = new Events.Event(EventShutdown.class),
    input = new Events.Event(EventInput.class),
    movefix = new Events.Event(EventMove.class),
    death = new Events.Event(EventDeath.class),
    kill = new Events.Event(EventKill.class),
    swing = new Events.Event(EventRenderItem.class),
    notification = new Events.Event(EventAlert.class),
    tick = new Events.Event(EventTick.class),
    totem_break = new Events.Event(EventTotemBreak.class);
    
    public Script(String name, String desc, String[] devs, String content, File file, boolean market) {
        this.name = name;
        this.desc = desc;
        this.devs = devs;
        this.content = content;
        this.file = file;
        this.market = market;
        imports = new ScriptImports(engine);
    }
    
    public void onEvent(Event event) {
        try {
            if (mc.player != null) {
                if (event instanceof EventUpdate && !market) {
                    if (mc.player != null) {
                        if (mc.player.ticksExisted % 20 == 0) {
                            changing = true;
                            try {
                                String digest = "";
                                {
                                    String luaScript = "";
                                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                                        String line;
                                        StringBuilder sb = new StringBuilder();
                                        while ((line = reader.readLine()) != null) {
                                            sb.append(line).append("\n");
                                        }
                                        luaScript = sb.toString();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                        return;
                                    }
                                    
                                    digest = luaScript;
                                }
                                
                                if (!digest.equals(content)) {
                                    try {
                                        this.unload();
                                        
                                        ArrayList<String> s3 = new ArrayList<>();
                                        @Cleanup
                                        FileReader fileReader = new FileReader(this.getFile());
                                        @Cleanup
                                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                                        
                                        String line;
                                        while ((line = bufferedReader.readLine()) != null) {
                                            s3.add(line);
                                        }

                                        bufferedReader.close();
                                        fileReader.close();

                                        StringBuilder sb = new StringBuilder();
                                        for (String element : s3) {
                                            sb.append(element).append("\n");
                                        }
                                        
                                        this.setContent(sb.toString());
                                        this.load();
                                        this.setName(ScriptBase.getName());
                                        this.setDesc(ScriptBase.getDesc());
                                        this.setDevs(ScriptBase.getDevs());
                                        this.getScriptModules().addAll(ScriptBase.getMod());
                                        ScriptBase.reset();
                                        if (!this.isEnabled()) this.unload();
                                    } catch (FileNotFoundException e) {
                                        Debugger.print(e);
                                    } catch (IOException e) {
                                        Debugger.print(e);
                                    }
                                }
                            } catch (Exception e) {
                                Debugger.print(e);
                            }
                            changing = false;
                        }
                    }
                }
                
                this.event = event;
                
                if (event instanceof EventRender3D) {
                    GL11.glRotated(((EventRender3D)event).getRenderInfo().getPitch(), 1.0, 0.0, 0.0);
                    GL11.glRotated(((EventRender3D)event).getRenderInfo().getYaw() + 180, 0.0, 1.0, 0.0);
                    active3d = true;
                }
                
                if (!changing) call(event);
                
                if (event instanceof EventRender3D) {
                    active3d = false;
                    GL11.glRotated(((EventRender3D)event).getRenderInfo().getYaw() + 180, 0.0, -1.0, 0.0);
                    GL11.glRotated(((EventRender3D)event).getRenderInfo().getPitch(), -1.0, 0.0, 0.0);
                }
            }
        } catch (Exception e) {
            ScriptErrorsHandler.handle(e.getMessage().split("\n")[e.getMessage().split("\n").length-1]);
            enabled = false;
            unload();
        }
    }
    
    public void call(Event event) {
        if (event instanceof EventToggle e) {
            for (ModuleBase base : bases) {
                if (e.getModule() == base.getInstance()) {
                    if (e.isValue() && base.getOnEnable() != null) {
                        base.getOnEnable().call();
                    }
                    
                    if (!e.isValue() && base.getOnDisable() != null) {
                        base.getOnDisable().call();
                    }
                }
            }
        }
        
        if(event instanceof EventMotionMove && motion_move.getFun() != null) {
            motion_move.getFun().call(CoerceJavaToLua.coerce((EventMotionMove) event));
        }
        
        if (event instanceof EventUpdate && update.getFun() != null) {
            update.getFun().call(CoerceJavaToLua.coerce((EventUpdate) event));
        }
        
        if (event instanceof EventRender2D && render_2d.getFun() != null) {
            render_2d.getFun().call(CoerceJavaToLua.coerce((EventRender2D) event));
        }
        
        if (event instanceof EventRender3D && render_3d.getFun() != null) {
            render_3d.getFun().call(CoerceJavaToLua.coerce((EventRender3D) event));
        }
        
        if (event instanceof EventSendPacket && send_packet.getFun() != null) {
            send_packet.getFun().call(CoerceJavaToLua.coerce((EventSendPacket) event));
        }
        
        if (event instanceof EventReceivePacket && receive_packet.getFun() != null) {
            receive_packet.getFun().call(CoerceJavaToLua.coerce((EventReceivePacket) event));
        }
        
        if (event instanceof EventMotion && motion.getFun() != null) {
            motion.getFun().call(CoerceJavaToLua.coerce((EventMotion) event));
        }
        
        if (event instanceof EventTotemBreak e && totem_break.getFun() != null) {
            totem_break.getFun().call(CoerceJavaToLua.coerce(
                new EventLuaTotem(new LivingEntityBase((LivingEntity) e.getEntity()), e.getTotemItem())
            ));
        }
        
        if (event instanceof EventKey && key.getFun() != null &&
            GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), ((EventKey) event).getKey()) == 1) {
            key.getFun().call(CoerceJavaToLua.coerce((EventKey) event));
        }
        
        if (event instanceof EventJump && jump.getFun() != null) {
            jump.getFun().call(CoerceJavaToLua.coerce((EventJump) event));
        }
        
        if (event instanceof EventChat && chat.getFun() != null) {
            chat.getFun().call(CoerceJavaToLua.coerce((EventChat) event));
        }
        
        if (event instanceof EventAttack e && attack.getFun() != null) {
            attack.getFun().call(CoerceJavaToLua.coerce(
                new EventLuaAttack(new LivingEntityBase(e.getTarget()))
            ));
        }
        
        if (event instanceof EventShutdown && shutdown.getFun() != null) {
            shutdown.getFun().call(CoerceJavaToLua.coerce((EventShutdown) event));
        }
        
        if (event instanceof EventInput && input.getFun() != null) {
            input.getFun().call(CoerceJavaToLua.coerce((EventInput) event));
        }
        
        if (event instanceof EventMove && movefix.getFun() != null) {
            movefix.getFun().call(CoerceJavaToLua.coerce((EventMove) event));
        }
        
        if (event instanceof EventDeath && death.getFun() != null) {
            death.getFun().call(CoerceJavaToLua.coerce((EventDeath) event));
        }
        
        if (event instanceof EventKill e && kill.getFun() != null) {
            kill.getFun().call(CoerceJavaToLua.coerce(
                new EventLuaKill(new LivingEntityBase(e.getTarget()))
            ));
        }
        
        if (event instanceof EventRenderItem e && swing.getFun() != null) {
            swing.getFun().call(CoerceJavaToLua.coerce(
                new EventLuaItem(e.isRight(), e.getProgress(), new MatrixBase(e.getMatrixStack()))
            ));
        }
        
        if (event instanceof EventAlert && notification.getFun() != null) {
            notification.getFun().call(CoerceJavaToLua.coerce((EventAlert) event));
        }
        
        if (event instanceof EventTick && tick.getFun() != null) {
            tick.getFun().call(CoerceJavaToLua.coerce((EventTick) event));
        }
    }
    
    public void load() {
        current = this;
        
        engine = JsePlatform.standardGlobals();
        
        try {
            // подготавливаем Lua-скрипт
            String luaScript = content.replace(".new", ":create");
            
            // сбрасываем «старые» ссылки в Events
            Events.render_2d.setFun(null);
            Events.render_3d.setFun(null);
            Events.update.setFun(null);
            Events.send_packet.setFun(null);
            Events.receive_packet.setFun(null);
            Events.motion.setFun(null);
            Events.key.setFun(null);
            Events.jump.setFun(null);
            Events.totem_break.setFun(null);
            Events.chat.setFun(null);
            Events.attack.setFun(null);
            Events.shutdown.setFun(null);
            Events.input.setFun(null);
            Events.movefix.setFun(null);
            Events.death.setFun(null);
            Events.kill.setFun(null);
            Events.swing.setFun(null);
            Events.notification.setFun(null);
            Events.tick.setFun(null);

            // Загружаем основной скрипт
            LuaValue script = engine.load(luaScript);
            imports = new ScriptImports(engine);
            imports.imported(this);

            // ============================
            // Добавляем loadstring в LuaJ,
            // который сразу выполняет код
            // ============================
            engine.set("loadstring", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    String script = arg.checkjstring();
                    LuaValue chunk = engine.load(script, "loadstring");
                    // Вызываем сразу
                    return chunk.call();
                }
            });
            // ----------------------------

            // Выполняем скрипт
            script.call();
            
            // Переносим функции-обработчики обратно в поля текущего Script
            render_2d.setFun(Events.render_2d.getFun());
            render_3d.setFun(Events.render_3d.getFun());
            update.setFun(Events.update.getFun());
            send_packet.setFun(Events.send_packet.getFun());
            receive_packet.setFun(Events.receive_packet.getFun());
            motion.setFun(Events.motion.getFun());
            key.setFun(Events.key.getFun());
            jump.setFun(Events.jump.getFun());
            chat.setFun(Events.chat.getFun());
            attack.setFun(Events.attack.getFun());
            shutdown.setFun(Events.shutdown.getFun());
            input.setFun(Events.input.getFun());
            movefix.setFun(Events.movefix.getFun());
            death.setFun(Events.death.getFun());
            kill.setFun(Events.kill.getFun());
            swing.setFun(Events.swing.getFun());
            notification.setFun(Events.notification.getFun());
            tick.setFun(Events.tick.getFun());
            totem_break.setFun(Events.totem_break.getFun());

            // Снова сбрасываем то, что хранится в Events, чтобы не мешало
            Events.render_2d.setFun(null);
            Events.render_3d.setFun(null);
            Events.update.setFun(null);
            Events.send_packet.setFun(null);
            Events.receive_packet.setFun(null);
            Events.motion.setFun(null);
            Events.key.setFun(null);
            Events.jump.setFun(null);
            Events.chat.setFun(null);
            Events.attack.setFun(null);
            Events.shutdown.setFun(null);
            Events.totem_break.setFun(null);
            Events.input.setFun(null);
            Events.movefix.setFun(null);
            Events.death.setFun(null);
            Events.kill.setFun(null);
            Events.swing.setFun(null);
            Events.notification.setFun(null);
            Events.tick.setFun(null);

        } catch (Exception e) {
            try {
                ScriptErrorsHandler.handle(e.getMessage().split("\n")[e.getMessage().split("\n").length-1]);
                if (enabled) {
                    enabled = false;
                    unload();
                }
            } catch (Exception e1) {
                // игнорируем
            }
            Debugger.print(e);
        }
        
        reloadModules();
        
        if (!market)
            rock.getScriptSender().save(this);
        else
            setDesc(ScriptBase.getDesc());
    }
    
    public void unload() {
        for (Mode.Element elmt : scriptModes) {
            elmt.getParent().remove(elmt);
        }
        
        for (Select.Element elmt : scriptSelects) {
            elmt.getParent().remove(elmt);
        }
        
        for (Module mod : getScriptModules()) {
            if (mod.getInfo().type() == Category.SCRIPTS)
                mod.getSettings().clear();
        }
        
        for (Setting set : getScriptSets()) {
            if (set.getParent() instanceof Module mod) {
                mod.getSettings().remove(set);
            }
        }
        
        for (Module mod : getScriptModules()) {
            if (mod.getInfo().type() == Category.SCRIPTS)
                rock.getModules().remove(mod.getClass());
        }
        
        bases.clear();
        this.getScriptModules().clear();
        
        rock.getDraggableHandler().getDraggables().removeAll(scriptDrags);
        this.scriptDrags.clear();
        getScriptSets().clear();
        reloadModules();
    }
    
    public void reloadModules() {
        if (rock.getClickGui() == null) return;
        
        rock.getClickGui().getWindow().getRenderer().getSettings().clear();
        
        for (Category cat : Category.values()) {
            int i = cat.getIndex();
            rock.getClickGui().getWindow().getRenderer().getGlyphes()[i] = new GlyphType();

            List<Module> modules = new ArrayList<>();
            
            rock.getModules().values().forEach(mod -> modules.add(mod));
            for (Script script : rock.getScriptHandler().getEnabledScripts()) {
                script.getScriptModules().forEach(mod -> modules.add(mod));
            }
            
            modules.sort(SORT_METHOD);
            
            for (Module module : modules) {
                if (module.getInfo().type() == cat) {
                    if (!rock.getClickGui().getWindow().getRenderer().getGlyphes()[i].containsKey(module.getInfo().name().charAt(0)))
                        rock.getClickGui().getWindow().getRenderer().getGlyphes()[i].put(module.getInfo().name().charAt(0), new ArrayList<>());
                    
                    rock.getClickGui().getWindow().getRenderer().getGlyphes()[i]
                        .get(module.getInfo().name().charAt(0)).add(module);
                    
                    for (Setting setting : module.getSettings()) {
                        rock.getClickGui().getWindow().getRenderer().getSettings().add(new SettingRect(setting));
                    }
                }
            }
        }
    }
}
