package com.sal.handler.spi;

import com.sal.config.SalProperties;
import com.sal.domain.StorageType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for resolving storage handlers by type.
 */
@Component
public class StorageHandlerFactory {

    private final Map<StorageType, StorageHandler> handlers;
    private final SalProperties properties;

    public StorageHandlerFactory(List<StorageHandler> handlerList, SalProperties properties) {
        this.properties = properties;
        this.handlers = new EnumMap<>(StorageType.class);
        
        for (StorageHandler handler : handlerList) {
            handlers.put(handler.getStorageType(), handler);
        }
    }

    /**
     * Get handler for storage type.
     */
    public StorageHandler getHandler(StorageType storageType) {
        StorageHandler handler = handlers.get(storageType);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for storage type: " + storageType);
        }
        return handler;
    }

    /**
     * Get handler by type name.
     */
    public StorageHandler getHandler(String storageTypeName) {
        StorageType type = StorageType.valueOf(storageTypeName.toUpperCase());
        return getHandler(type);
    }

    /**
     * Get default handler based on configuration.
     */
    public StorageHandler getDefaultHandler() {
        String defaultType = properties.getDefaultStorageType();
        return getHandler(defaultType);
    }

    /**
     * Check if handler is available.
     */
    public boolean hasHandler(StorageType storageType) {
        return handlers.containsKey(storageType);
    }

    /**
     * Get all registered storage types.
     */
    public List<StorageType> getRegisteredTypes() {
        return List.copyOf(handlers.keySet());
    }
}
