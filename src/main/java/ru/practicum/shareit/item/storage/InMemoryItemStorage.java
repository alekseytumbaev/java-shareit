package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.exception.ItemNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemStorage {
    private long nextId;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item add(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Collection<Item> getByOwnerId(long ownerId) {
        return items.values().stream().filter(item -> item.getOwnerId() == ownerId).collect(Collectors.toList());
    }

    @Override
    public Collection<Item> searchByNameOrDescription(String text) {
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (containsIgnoreCase(item.getName(), text) || containsIgnoreCase(item.getDescription(), text)))
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String baseString, String subString) {
        return baseString.toLowerCase().contains(subString.toLowerCase());
    }

    @Override
    public Item update(Item item) throws ItemNotFoundException {
        if (!items.containsKey(item.getId())) {
            throw new ItemNotFoundException(String.format("Item with id=%d not found", item.getId()));
        }
        items.put(item.getId(), item);
        return item;
    }

    private long getNextId() {
        return ++nextId;
    }
}
