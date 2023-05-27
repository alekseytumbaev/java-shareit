package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerIdException;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepo;
    private final UserService userService;

    public ItemService(ItemRepository itemRepo, UserService userService) {
        this.itemRepo = itemRepo;
        this.userService = userService;
    }

    public Item add(Item item) throws UserNotFoundException {
        if (!userService.existsById(item.getOwner().getId())) {
            throw new UserNotFoundException(
                    String.format("Cannot add item, because owner with id=%d not found", item.getOwner().getId()));
        }
        return itemRepo.save(item);
    }

    public Item getById(long itemId) throws ItemNotFoundException {
        Optional<Item> itemOpt = itemRepo.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id=%d not found", itemId));
        }
        return itemOpt.get();
    }

    public Collection<Item> getByOwnerId(long ownerId) {
        return itemRepo.findAllByOwnerId(ownerId);
    }

    public Collection<Item> searchByNameOrDescription(String text) {
        if (text.isBlank()) {
            return new ArrayList<>(0);
        }
        return itemRepo.searchByNameOrDescription(text);
    }

    public Item update(Item item) throws ItemNotFoundException {
        Item presentedItem = getById(item.getId());

        if (item.getOwner().getId() != presentedItem.getOwner().getId()) {
            throw new WrongOwnerIdException(String.format("User with id=%d cannot update item owned by user with id=%d",
                    item.getOwner().getId(), presentedItem.getOwner().getId()));
        }

        //replace null fields with values from existing item
        if (item.getName() == null) {
            item.setName(presentedItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(presentedItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(presentedItem.getAvailable());
        }
        item.setRequest(presentedItem.getRequest());

        return itemRepo.save(item);
    }
}
