package net.minecraft.util;
public class ResourceLocation {
    private final String domain,path;
    public ResourceLocation(String value){
        int split=value.indexOf(':');
        domain=split<0?"minecraft":value.substring(0,split);
        path=split<0?value:value.substring(split+1);
    }
    public ResourceLocation(String domain,String path){this.domain=domain;this.path=path;}
    public String func_110623_a(){return path;}
    public String func_110624_b(){return domain;}
    @Override public String toString(){return domain+":"+path;}
    @Override public boolean equals(Object other){return other instanceof ResourceLocation&&toString().equals(other.toString());}
    @Override public int hashCode(){return toString().hashCode();}
}
