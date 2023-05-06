package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.exception.ItemNotFoundException;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Item add(Item item);

    Optional<Item> getById(long itemId);

    Collection<Item> getByOwnerId(long ownerId);

    Collection<Item> searchByNameOrDescription(String text);

    Item update(Item item) throws ItemNotFoundException;
}
