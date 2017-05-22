package jui_lib;

/**
 * Created by Jiachen on 30/04/2017.
 */
public class EventListener {
    private Event event;
    private Runnable attachedMethod;
    private String id;
    private boolean disabled;

    public EventListener(String id, Event event) {
        this.event = event;
        this.id = id;
        disabled = false;
    }

    public Event getEvent() {
        return event;
    }

    public EventListener setEvent(Event event) {
        this.event = event;
        return this;
    }

    public void invoke() {
        if (attachedMethod != null && !disabled)
            attachedMethod.run();
    }

    public EventListener attachMethod(Runnable runnable) {
        this.attachedMethod = runnable;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String temp) {
        this.id = temp;
    }

    public EventListener setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public boolean isDisabled() {
        return disabled;
    }
}
