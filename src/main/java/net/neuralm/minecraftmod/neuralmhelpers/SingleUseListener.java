package net.neuralm.minecraftmod.neuralmhelpers;

import net.neuralm.minecraftmod.Neuralm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

/***
 * A property change listener that removes itself after the listener activated.
 * This way we dont get duplicated calls.
 */
public class SingleUseListener implements PropertyChangeListener {

    private Consumer<PropertyChangeEvent> handler;
    private String eventName;

    /***
     * Create the single use listener
     * @param handler The handler itself.
     * @param eventName The event it should listen to.
     */
    public SingleUseListener(Consumer<PropertyChangeEvent> handler, String eventName) {
        Neuralm.instance.client.addListener(eventName, this);
        this.handler = handler;
        this.eventName = eventName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.handler.accept(evt);
        Neuralm.instance.client.removeListener(eventName, this);
    }
}