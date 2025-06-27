package mt.setting;

import arc.*;
import arc.func.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

@Deprecated
public class ConfigHandler{
    protected Mod mod;
    protected JsonValue cfg;
    public boolean changed = false;
    /**
     * Tip text when hovering a setting's ui.
     */
    public String cfgtip = "";
    /**
     * Cute Cat-girl Anuke.
     */
    public SettingsMenuDialog.SettingsCategory cat;

    /**
     * Init json config handler of a mod.
     */
    public static ConfigHandler request(Mod mod){
        ConfigHandler jc = new ConfigHandler();
        jc.loadCfg(mod);
        Events.run(EventType.Trigger.update, () -> {
            if(jc.changed){
                jc.saveCfg();
                jc.changed = false;
            }
        });
        return jc;
    }

    public void newSettingsCategory(String name, @Nullable Drawable icon, Cons<SettingsMenuDialog.SettingsTable> builder){
        if(cat == null){
            Vars.ui.settings.addCategory(name, icon, builder);
            cat = Vars.ui.settings.getCategories().find(b -> b.name.equals(name));
        }
    }

    public void loadCfg(Mod mod){
        this.mod = mod;
        if(mod.getConfig().exists()){
            try{
                cfg = new JsonReader().parse(mod.getConfig());
            }catch(Exception e){
                Log.err(e);
            }

        }
        if(cfg == null){
            cfg = new JsonValue("MOD CONFIG");
        }
        cfg.setType(JsonValue.ValueType.object);
    }

    public void saveCfg(){
        try{
            var writer = mod.getConfig().writer(false);
            cfg.prettyPrint(JsonWriter.OutputType.json, writer);
            writer.flush();
            writer.close();
        }catch(Exception e){
            Log.err(e);
        }
    }

    public boolean getb(String name, boolean def){
        return cfg.getBoolean(name, def);
    }

    public int geti(String name, int def){
        return cfg.getInt(name, def);
    }

    public String getstr(String name, String def){
        return cfg.getString(name, def);
    }

    public JsonValue get(String name){
        var jv = cfg.get(name);
        if(jv == null){
            jv = new JsonValue("");
            cfg.addChild(name, jv);
            changed = true;
        }
        return jv;
    }

    public void putb(String name, boolean v){
        var jv = cfg.get(name);
        if(jv == null){
            cfg.addChild(name, new JsonValue(v));
        }else{
            jv.set(v);
        }
        changed = true;
    }

    public void puti(String name, int v){
        var jv = cfg.get(name);
        if(jv == null){
            cfg.addChild(name, new JsonValue(v));
        }else{
            jv.set(v, null);
        }
        changed = true;
    }

    public void putstr(String name, String v){
        var jv = cfg.get(name);
        if(jv == null){
            cfg.addChild(name, new JsonValue(v));
        }else{
            jv.set(v);
        }
        changed = true;
    }

    public void buildTip(Table t){
        t.pane(tt -> {
            tt.background(Styles.grayPanel);
            tt.add(cfgtip).update(l -> {
                if(!l.textEquals(cfgtip)) l.setText(cfgtip);
            }).growX().get().setWrap(true);
        }).height(200f).growX();
        t.row();
    }

    public void checkb(String name, boolean def, String text, String tip, Table t){
        if(!cfg.has(name)) putb(name, def);
        t.check(text, b -> putb(name, b)).checked(getb(name, def)).left().get().hovered(() -> cfgtip = tip);
        t.row();
    }

    public void fieldi(String name, int def, String text, String tip, Table t, TextField.TextFieldFilter tf, TextField.TextFieldValidator tv){
        if(!cfg.has(name)) puti(name, def);
        t.table(tt -> {
            tt.field("" + geti(name, def), tf, s -> puti(name, Strings.parseInt(s))).width(100f).get().setValidator(tv);
            tt.add(text).right();
        }).growX().get().hovered(() -> cfgtip = tip);
        t.row();
    }

    public void slideri(String name, int def, String text, String tip, Table t, int min, int max, int step){
        if(!cfg.has(name)) puti(name, def);
        var label = new Label(text);
        label.touchable = Touchable.disabled;
        var slider = new Slider(min, max, step, false);
        slider.setValue(geti(name, def));
        t.stack(slider, label).growX().left().minWidth(300f).get().hovered(() -> cfgtip = tip);
        slider.changed(() -> {
            puti(name, (int)slider.getValue());
            label.setText(Core.bundle.get(Strings.replace(new StringBuilder(text), "@", "").toString()) + (int)slider.getValue());
        });

        slider.change();
        t.row();
    }
}
