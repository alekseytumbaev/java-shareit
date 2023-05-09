package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerIdException;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemService(ItemStorage itemStorage, UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    public Item add(Item item) throws UserNotFoundException {
        if (!userService.existsById(item.getOwnerId()))
            throw new UserNotFoundException(
                    String.format("Cannot add item, because owner with id=%d not found", item.getOwnerId()));
        return itemStorage.add(item);
    }

    public Item getById(long itemId) throws ItemNotFoundException {
        Optional<Item> itemOpt = itemStorage.getById(itemId);
        if (itemOpt.isEmpty())
            throw new ItemNotFoundException(String.format("Item with id=%d not found", itemId));

        return itemOpt.get();
    }

    public Collection<Item> getByOwnerId(long ownerId) {
        return itemStorage.getByOwnerId(ownerId);
    }

    public Collection<Item> searchByNameOrDescription(String text) {
        return itemStorage.searchByNameOrDescription(text);
    }

    public Item update(Item item) throws ItemNotFoundException {
        Optional<Item> itemOpt = itemStorage.getById(item.getId());
        if (itemOpt.isEmpty())
            throw new ItemNotFoundException(String.format("Item with id=%d not found", item.getId()));

        Item presentedItem = itemOpt.get();
        if (item.getOwnerId() != presentedItem.getOwnerId())
            throw new WrongOwnerIdException(String.format("User with id=%d cannot update item owned by user with id=%d",
                            item.getOwnerId(), presentedItem.getOwnerId()));

        //replace null fields with values from existing item
        if (item.getName() == null)
            item.setName(presentedItem.getName());
        if (item.getDescription() == null)
            item.setDescription(presentedItem.getDescription());
        if (item.getAvailable() == null)
            item.setAvailable(presentedItem.getAvailable());
        item.setRequest(presentedItem.getRequest());

        return itemStorage.update(item);
    }
}
