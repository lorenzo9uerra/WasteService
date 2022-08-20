package it.unibo.radarSystem22.domain;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SafeUpdateSet<E> extends HashSet<E> {
    /**
     * Set with a safeForEach method that allows
     * changing the set elements (add/remove) safely during
     * iteration
     */

    private boolean runningForEach = false;
    private final Set<E> toAddAfterUpdates = new HashSet<>();
    private final Set<E> toRemoveAfterUpdates = new HashSet<>();

    public void safeForEach(Consumer<E> func) {
        // Per permettere esecuzione di subscribe/unsubscribe
        // dentro a ISonarObserver.update,activate,ecc. senza deadlock
        runningForEach = true;
        synchronized (this) {
            this.forEach(func);
            runningForEach = false;
            // Synchronized non necessario, visto che
            // runningForEach è false
            // e tutte le modifiche ai due set temporanei
            // sono eseguite con esso a true
            // (e sono privati, quindi nessuna sottoclasse
            // potrebbe interferire
            toAddAfterUpdates.forEach(super::add);
            toRemoveAfterUpdates.forEach(super::remove);
            toAddAfterUpdates.clear();
            toRemoveAfterUpdates.clear();
        }
    }

    @Override
    public boolean add(E e) {
        if (runningForEach) {
            synchronized (toAddAfterUpdates) {
                toAddAfterUpdates.add(e);
            }
            return false; // non è cambiato (ora)
        } else {
            synchronized (this) {
                return super.add(e);
            }
        }
    }

    @Override
    public boolean remove(Object o) {
        if (runningForEach) {
            if (o != null) {
                synchronized (toRemoveAfterUpdates) {
                    toRemoveAfterUpdates.add((E) o);
                }
                return false;
            } else {
                throw new IllegalArgumentException("remove: Object is null!");
            }
        } else {
            synchronized (this) {
                return super.remove(o);
            }
        }
    }
}
