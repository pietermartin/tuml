package org.umlg.runtime.notification;

import org.umlg.runtime.adaptor.TransactionThreadNotificationVar;
import org.umlg.runtime.collection.UmlgRuntimeProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Date: 2014/08/31
 * Time: 12:53 PM
 */
public class UmlgNotificationManager {

    private Map<UmlgRuntimeProperty, NotificationListener> listenerMap = new HashMap<>();

    public final static UmlgNotificationManager INSTANCE = new UmlgNotificationManager();

    private UmlgNotificationManager() {
    }

    public void registerListener(UmlgRuntimeProperty umlgRuntimeProperty, NotificationListener listener) {
        this.listenerMap.put(umlgRuntimeProperty, listener);
    }

    public NotificationListener get(UmlgRuntimeProperty umlgRuntimeProperty) {
        return this.listenerMap.get(umlgRuntimeProperty);
    }

    public void notify(UmlgRuntimeProperty umlgRuntimeProperty, ChangeHolder changeHolder) {

        Optional.ofNullable(UmlgNotificationManager.INSTANCE.get(umlgRuntimeProperty))
                .ifPresent(
                        n -> TransactionThreadNotificationVar.add(n, changeHolder)
                );

    }

}
