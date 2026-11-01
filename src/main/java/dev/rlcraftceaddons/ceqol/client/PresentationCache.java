package dev.rlcraftceaddons.ceqol.client;

import java.util.Objects;
import java.util.function.Supplier;

/** A single measured presentation, including null results. Client-thread use only. */
public final class PresentationCache<T> {
    private Object data,settings,context;
    private T value;
    private boolean populated;

    public T get(Object data,Object settings,Object context,Supplier<T> factory) {
        if(populated && Objects.equals(this.data,data) && Objects.equals(this.settings,settings)
                && this.context==context)return value;
        T measured=factory.get();
        this.data=data;this.settings=settings;this.context=context;value=measured;populated=true;
        return measured;
    }

    public void clear() {data=null;settings=null;context=null;value=null;populated=false;}
}
