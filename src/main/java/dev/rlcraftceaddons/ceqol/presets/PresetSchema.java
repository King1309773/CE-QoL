package dev.rlcraftceaddons.ceqol.presets;

import com.google.gson.*;
import com.northwestwind.forgeautofish.AutoFishSettings;
import dev.rlcraftceaddons.sdtemperaturehud.config.HudConfig;
import dev.rlcraftceaddons.sdtemperaturehud.client.theme.HexColorParser;
import dev.rlcraftceaddons.thirstpreview.ThirstPreviewConfig;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/** Explicitly registered own fields only. JSON never supplies reflection targets or file paths. */
public final class PresetSchema {
    public static final List<String> GROUPS=Collections.unmodifiableList(Arrays.asList("temperature","hydration","fishing"));
    private final Map<String,List<Setting>> groups=new LinkedHashMap<>();
    public PresetSchema() {
        try {
            List<Setting> temperature=new ArrayList<>();groups.put("temperature",temperature);
            for(Field f:HudConfig.class.getFields())if(!Modifier.isFinal(f.getModifiers())) {
                Object def;
                if(f.getType()==boolean.class)def=!f.getName().equals("warningSoundEnabled");
                else if(f.getType()==double.class)def=1D;
                else def=f.getName().equals("backgroundAlpha")?214:f.getName().equals("changeHintThreshold")?2:f.getName().equals("changeHintDurationSeconds")?4:0;
                temperature.add(new Setting(f,null,def,"","general"));
            }
            nested(temperature,"layout",new HudConfig.Layout());nested(temperature,"analysis",new HudConfig.Analysis());
            nested(temperature,"visibility",new HudConfig.Visibility());nested(temperature,"theme",new HudConfig.Theme());
            nested(temperature,"mapIntegration",new HudConfig.MapIntegration());
            List<Setting> hydration=new ArrayList<>();groups.put("hydration",hydration);
            for(String key:Arrays.asList("showTooltip","showHud","showDetailsWithShift"))
                hydration.add(new Setting(ThirstPreviewConfig.class.getField(key),null,true,"","client"));
            List<Setting> fishing=new ArrayList<>();groups.put("fishing",fishing);
            for(String key:Arrays.asList("showStatus","protectRod","durabilityReserve","hudX","hudBottom")) {
                Object def=key.equals("showStatus") || key.equals("protectRod")?Boolean.TRUE:key.equals("durabilityReserve")?10:key.equals("hudX")?8:58;
                Setting setting=new Setting(AutoFishSettings.class.getField(key),null,def,"","fishing");
                if(setting.type==int.class){setting.min=key.equals("durabilityReserve")?1:0;setting.max=key.equals("durabilityReserve")?100:4096;}
                fishing.add(setting);
            }
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Preset schema does not match this build",e);}
    }
    private void nested(List<Setting> settings,String name,Object defaults)throws ReflectiveOperationException {
        Object live=HudConfig.class.getField(name).get(null);
        for(Field f:defaults.getClass().getFields())settings.add(new Setting(f,live,f.get(defaults),name+".","general."+name.toLowerCase(Locale.ROOT)));
    }
    public JsonObject capture(String scope,boolean defaults)throws IOException {
        JsonObject result=new JsonObject();
        if(!"all".equals(scope) && !groups.containsKey(scope))throw new IOException("Unknown preset scope");
        for(String group:GROUPS)if("all".equals(scope)||group.equals(scope)) {
            JsonObject values=new JsonObject();for(Setting s:groups.get(group))values.add(s.key,s.json(defaults?s.def:s.get()));
            result.add(group,values);
        }
        validate(result);return result;
    }
    public void validate(JsonObject data)throws IOException {
        if(data.size()==0)throw new IOException("Preset has no settings");
        for(Map.Entry<String,JsonElement> group:data.entrySet()) {
            List<Setting> settings=groups.get(group.getKey());
            if(settings==null || !group.getValue().isJsonObject())throw new IOException("Unknown or malformed group");
            JsonObject values=group.getValue().getAsJsonObject();
            if(values.size()!=settings.size())throw new IOException("Incomplete or unknown settings");
            for(Setting s:settings)s.value(values.get(s.key));
        }
    }
    public void applyLive(JsonObject data)throws IOException {
        validate(data);
        for(Map.Entry<String,JsonElement> group:data.entrySet())for(Setting s:groups.get(group.getKey()))s.set(s.value(group.getValue().getAsJsonObject().get(s.key)));
    }
    public JsonObject subsetCurrent(JsonObject shape)throws IOException {
        JsonObject result=new JsonObject();
        for(Map.Entry<String,JsonElement> group:shape.entrySet())result.add(group.getKey(),capture(group.getKey(),false).get(group.getKey()));
        return result;
    }
    public String file(String group) {
        return group.equals("temperature")?"sdtemperaturehud.cfg":group.equals("hydration")?"thirstpreview.cfg":"ceqol.cfg";
    }
    public void write(Configuration config,String group,JsonObject values)throws IOException {
        for(Setting s:groups.get(group)) {
            Object v=s.value(values.get(s.key));Property p;
            if(s.type==boolean.class)p=config.get(s.category,s.configKey,(Boolean)s.def,s.comment);
            else if(s.type==int.class)p=config.get(s.category,s.configKey,((Number)s.def).intValue(),s.comment);
            else if(s.type==double.class)p=config.get(s.category,s.configKey,((Number)s.def).doubleValue(),s.comment);
            else p=config.get(s.category,s.configKey,s.text(s.def),s.comment);
            if(!s.matches(p,v)) {
                if(s.type==boolean.class)p.set((Boolean)v);else if(s.type==int.class)p.set(((Number)v).intValue());
                else if(s.type==double.class)p.set(((Number)v).doubleValue());else p.set(s.text(v));
            }
        }
    }
    public boolean matches(Configuration config,String group,JsonObject values) {
        try {for(Setting s:groups.get(group))if(!config.hasKey(s.category,s.configKey)
                || !s.matches(config.getCategory(s.category).get(s.configKey),s.value(values.get(s.key))))return false;
            return true;
        }catch(IOException|RuntimeException e){return false;}
    }
    private static final class Setting {
        final Field field;final Object owner,def;final Class<?> type;final String key,category,configKey,comment;
        double min=-Double.MAX_VALUE,max=Double.MAX_VALUE;
        Setting(Field f,Object owner,Object def,String prefix,String category) {
            field=f;this.owner=owner;this.def=def;type=f.getType();key=prefix+f.getName();this.category=category;
            Config.Name name=f.getAnnotation(Config.Name.class);configKey=name==null?f.getName():name.value();
            Config.Comment c=f.getAnnotation(Config.Comment.class);comment=c==null?"CE QoL preset setting.":String.join("\n",c.value());
            Config.RangeInt ri=f.getAnnotation(Config.RangeInt.class);if(ri!=null){min=ri.min();max=ri.max();}
            Config.RangeDouble rd=f.getAnnotation(Config.RangeDouble.class);if(rd!=null){min=rd.min();max=rd.max();}
            if(key.equals("layout.version")){min=0;max=1;}
        }
        Object get() {try{return field.get(owner);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
        void set(Object value){try{field.set(owner,value);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
        String text(Object v){return v instanceof Enum?((Enum<?>)v).name():v.toString();}
        JsonPrimitive json(Object v){return v instanceof Boolean?new JsonPrimitive((Boolean)v):v instanceof Number?new JsonPrimitive((Number)v):new JsonPrimitive(text(v));}
        @SuppressWarnings({"unchecked","rawtypes"}) Object value(JsonElement value)throws IOException {
            try {
                if(value==null || !value.isJsonPrimitive())throw new IllegalArgumentException();JsonPrimitive p=value.getAsJsonPrimitive();
                if(type==boolean.class){if(!p.isBoolean())throw new IllegalArgumentException();return p.getAsBoolean();}
                if(type==int.class || type==double.class) {
                    if(!p.isNumber())throw new IllegalArgumentException();double n=p.getAsDouble();
                    if(!Double.isFinite(n)||n<min||n>max)throw new IllegalArgumentException();
                    if(type==int.class)return p.getAsBigDecimal().intValueExact();return n;
                }
                if(!p.isString())throw new IllegalArgumentException();String text=p.getAsString();
                if(type.isEnum())return Enum.valueOf((Class)type,text);
                if(type==String.class){HexColorParser.parse(text);return text;}
                throw new IllegalArgumentException();
            }catch(RuntimeException e){throw new IOException("Invalid preset value: "+key,e);}
        }
        boolean matches(Property p,Object v) {
            if(type==boolean.class)return p.getType()==Property.Type.BOOLEAN && p.isBooleanValue() && p.getBoolean()==(Boolean)v;
            if(type==int.class)return p.getType()==Property.Type.INTEGER && p.isIntValue() && p.getInt()==((Number)v).intValue();
            if(type==double.class)return p.getType()==Property.Type.DOUBLE && p.isDoubleValue() && Double.compare(p.getDouble(),((Number)v).doubleValue())==0;
            return p.getType()==Property.Type.STRING && p.getString().equals(text(v));
        }
    }
}
