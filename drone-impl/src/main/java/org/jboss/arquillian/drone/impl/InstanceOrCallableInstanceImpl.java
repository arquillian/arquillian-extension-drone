package org.jboss.arquillian.drone.impl;

import java.util.concurrent.Callable;

import org.jboss.arquillian.drone.spi.DroneContext.InstanceOrCallableInstance;

public class InstanceOrCallableInstanceImpl implements InstanceOrCallableInstance {

    private Object holder;

    public InstanceOrCallableInstanceImpl(Object object) {
        this.holder = object;
    }

    @Override
    public InstanceOrCallableInstance set(Object object) throws IllegalArgumentException {
        Validate.notNull(object, "InstanceOrCallableInstance can't set be null");
        this.holder = object;
        return this;
    }

    @Override
    public boolean isInstance() {
        return !(holder instanceof Callable<?>);
    }

    @Override
    public boolean isInstanceCallable() {
        return holder instanceof Callable<?>;
    }

    @Override
    public <T> T asInstance(Class<T> type) throws IllegalStateException {
        if (holder instanceof Callable<?>) {
            throw new IllegalStateException(
                    "Unexpected callable present in Drone Context, should be already instantiated at this moment.");
        }
        return type.cast(holder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Callable<T> asCallableInstance(Class<T> type) throws IllegalStateException {
        if (!(holder instanceof Callable<?>)) {
            throw new IllegalStateException(
                    "Unexpected callable present in Drone Context, should be already instantiated at this moment.");
        }

        return (Callable<T>) holder;
    }

    @Override
    public String toString() {
        return "InstanceOrCallableInstance[" + (isInstanceCallable() ? "callable" : holder.getClass().getSimpleName()) + "]";
    }

}