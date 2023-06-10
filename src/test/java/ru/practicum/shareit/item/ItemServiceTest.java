package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.item.exception.CommentingRestrictedException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepo;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepo;
    @Mock
    private CommentRepository commentRepo;
    @Mock
    private ItemRequestRepository requestRepo;

    @InjectMocks
    private ItemService itemService;


    @Test
    @DisplayName("Should return an empty list when the search query is blank")
    void searchByNameOrDescriptionReturnsEmptyListWhenQueryIsBlank() {
        String text = "";
        int from = 0;
        int size = 10;
        Collection<ItemDto> result = itemService.searchByNameOrDescription(text, from, size);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return an empty list when no items match the search query")
    void searchByNameOrDescriptionReturnsEmptyListWhenNoMatches() {
        String searchText = "test";
        int from = 0;
        int size = 10;
        List<Item> itemList = new ArrayList<>();
        Page<Item> itemPage = new PageImpl<>(itemList);
        when(itemRepo.searchByNameOrDescription(anyString(), any(PageRequest.class))).thenReturn(itemPage);

        Collection<ItemDto> result = itemService.searchByNameOrDescription(searchText, from, size);

        assertTrue(result.isEmpty());
        verify(itemRepo, times(1)).searchByNameOrDescription(searchText, PageRequest.of(0, size));
    }

    @Test
    @DisplayName("Should return a list of items with matching name or description")
    void searchByNameOrDescriptionReturnsMatchingItems() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(1L, "Item 1", "Description 1", true, new User(1L, "User 1", "user1@example.com"), null));
        itemList.add(new Item(2L, "Item 2", "Description 2", false, new
                User(2L, "User 2", "user2@example.com"), null));
        itemList.add(new Item(3L, "Item 3", "Description 3", true, new
                User(3L, "User 3", "user3@example.com"), null));
        Page<Item> itemPage = new PageImpl<>(itemList);

        when(itemRepo.searchByNameOrDescription(anyString(), any(PageRequest.class))).thenReturn(itemPage);

        Collection<ItemDto> result = itemService.searchByNameOrDescription("Item", 0, 10);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(itemDto -> itemDto.getId() == 1L));
        assertTrue(result.stream().anyMatch(itemDto -> itemDto.getId() == 2L));
        assertTrue(result.stream().anyMatch(itemDto -> itemDto.getId() == 3L));
    }

    @Test
    public void testGetDtoById() throws ItemNotFoundException, UserNotFoundException {
        long itemId = 1L;
        long userId = 2L;

        Item item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setDescription("This is a test item");

        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.existsById(userId)).thenReturn(true);

        List<Comment> comments = new ArrayList<>();
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(owner);
        comment.setItem(item);
        comments.add(comment);

        when(commentRepo.findAllByItem_IdAsMap(List.of(itemId))).thenReturn(Map.of(itemId, comments));

        List<Booking> bookings = new ArrayList<>();
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(owner);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookings.add(booking);

        when(bookingRepo.findAllByItem_IdAsMap(List.of(itemId))).thenReturn(Map.of(itemId, bookings));

        ItemWithBookingsResponseDto result = itemService.getDtoById(itemId, userId);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getOwner().getId(), result.getOwnerId());
        assertEquals(comments.size(), result.getComments().size());
        CommentResponseDto commentDto = result.getComments().get(0);
        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(comment.getAuthor().getName(), commentDto.getAuthorName());
        assertEquals(booking.getId(), result.getLastBooking().getId());
        assertEquals(booking.getBooker().getId(), result.getLastBooking().getBookerId());
        assertEquals(booking.getItem().getId(), result.getLastBooking().getItemId());
        assertEquals(booking.getStart(), result.getLastBooking().getStart());
        assertEquals(booking.getEnd(), result.getLastBooking().getEnd());
    }

    @Test
    @DisplayName("Should return an empty list when no items are found for the given owner ID")
    void getAllByOwnerIdReturnsEmptyListWhenNoItemsFound() {
        long ownerId = 1L;
        int from = 0;
        int size = 10;

        when(itemRepo.findAllByOwner_Id(anyLong(), any())).thenReturn(Page.empty());

        Collection<ItemWithBookingsResponseDto> result = itemService.getAllByOwnerId(ownerId, from, size);

        assertTrue(result.isEmpty());
        verify(itemRepo, times(1)).findAllByOwner_Id(ownerId, PageRequest.of(from / size, size));
    }

    @Test
    @DisplayName("Should return items with correct last and next bookings for the given owner ID")
    void getAllByOwnerIdWithCorrectLastAndNextBookings() {
        User owner = new User(1L, "John Doe", "john.doe@example.com");
        Item item1 = new Item(1L, "Item 1", "Description 1", true, owner, null);
        Item item2 = new Item(2L, "Item 2", "Description 2", true, owner, null);

        LocalDateTime now = LocalDateTime.now();

        User booker = new User(2L, "John Doe", "john.doe@example.com");
        Booking booking1 = new Booking(1L, now.minusDays(2), now.minusDays(1), booker, item1, BookingStatus.APPROVED);
        Booking booking2 = new Booking(2L, now.plusDays(4), now.plusDays(5), booker, item2, BookingStatus.APPROVED);
        Booking booking3 = new Booking(3L, now.minusDays(6), now.minusDays(5), booker, item2, BookingStatus.APPROVED);

        when(itemRepo.findAllByOwner_Id(anyLong(), any())).thenReturn(
                new PageImpl<>(List.of(item1, item2))
        );
        when(bookingRepo.findAllByItem_IdAsMap(any())).thenReturn(
                Map.of(item1.getId(), List.of(booking1),
                        item2.getId(), List.of(booking2, booking3)
                )
        );

        Collection<ItemWithBookingsResponseDto> result = itemService.getAllByOwnerId(owner.getId(), 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return all items by owner ID with correct pagination")
    void getAllByOwnerIdWithPagination() {
        User owner = new User(1L, "John Doe", "john.doe@example.com");
        Item item1 = new Item(1L, "Item 1", "Description 1", true, owner, null);
        Item item2 = new Item(2L, "Item 2", "Description 2", true, owner, null);
        Item item3 = new Item(3L, "Item 3", "Description 3", true, owner, null);

        Page<Item> items = new PageImpl<>(List.of(item1, item2, item3));

        when(itemRepo.findAllByOwner_Id(anyLong(), any())).thenReturn(items);
        when(commentRepo.findAllByItem_IdAsMap(any())).thenReturn(Collections.emptyMap());
        when(bookingRepo.findAllByItem_IdAsMap(any())).thenReturn(Collections.emptyMap());

        Collection<ItemWithBookingsResponseDto> result = itemService.getAllByOwnerId(owner.getId(), 0, 10);

        assertEquals(3, result.size());
        assertEquals(item1.getId(), result.iterator().next().getId());
        verify(itemRepo, times(1)).findAllByOwner_Id(anyLong(), any());
        verify(commentRepo, times(1)).findAllByItem_IdAsMap(any());
        verify(bookingRepo, times(1)).findAllByItem_IdAsMap(any());
    }

    @Test
    @DisplayName("Should throw an exception when the request ID does not exist")
    void updateItemWithNonExistingRequestIdThenThrowException() {
        ItemDto itemDto = new ItemDto(1L, "item1", "description1", true, 1L, 1L);

        User requestAuthor = new User(2L, "user2", "user2@example.com");
        ItemRequest itemRequest = new ItemRequest(2L, "request1", LocalDateTime.now(), requestAuthor);

        User owner = new User(1L, "user1", "user1@example.com");
        Item item = new Item(1L, "item1", "description1", true, owner, itemRequest);

        when(itemRepo.findById(itemDto.getId())).thenReturn(Optional.of(item));
        when(requestRepo.findById(itemDto.getRequestId())).thenReturn(Optional.empty());

        assertThrows(ItemRequestNotFoundException.class, () -> itemService.update(itemDto));

        verify(itemRepo, times(1)).findById(itemDto.getId());
        verify(requestRepo, times(1)).findById(itemDto.getRequestId());
        verifyNoMoreInteractions(itemRepo, requestRepo);
    }

    @Test
    @DisplayName("Should update the item with the given request ID when it exists")
    void updateItemWithExistingRequestId() {
        long itemId = 1L;
        long ownerId = 2L;
        long requestId = 3L;
        String itemName = "Test Item";
        String itemDescription = "Test Item Description";
        Boolean itemAvailable = true;
        ItemDto itemDto = new ItemDto(itemId, itemName, itemDescription, itemAvailable, ownerId, requestId);

        User owner = new User(ownerId, "John Doe", "john.doe@example.com");
        ItemRequest request = new ItemRequest(requestId, "Test Request", LocalDateTime.now(), owner);
        Item item = new Item(itemId, itemName, itemDescription, itemAvailable, owner, request);

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(requestRepo.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepo.save(any(Item.class))).thenReturn(item);

        ItemDto updatedItemDto = itemService.update(itemDto);

        assertEquals(itemId, updatedItemDto.getId());
        assertEquals(itemName, updatedItemDto.getName());
        assertEquals(itemDescription, updatedItemDto.getDescription());
        assertEquals(itemAvailable, updatedItemDto.getAvailable());
        assertEquals(ownerId, updatedItemDto.getOwnerId());
        assertEquals(requestId, updatedItemDto.getRequestId());

        verify(itemRepo, times(1)).findById(itemId);
        verify(requestRepo, times(1)).findById(requestId);
        verify(itemRepo, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Should throw an exception when the owner is different")
    void updateItemWhenOwnerIsDifferentThenThrowException() {
        long itemId = 1L;
        long ownerId = 1L;
        long differentOwnerId = 2L;
        String itemName = "Test Item";
        String itemDescription = "Test Item Description";
        Boolean itemAvailable = true;
        long requestId = 1L;
        ItemDto itemDto = new ItemDto(itemId, itemName, itemDescription, itemAvailable, differentOwnerId, requestId);

        User owner = new User(ownerId, "Owner", "owner@example.com");
        Item item = new Item(itemId, itemName, itemDescription, itemAvailable, owner, null);

        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(UnauthorizedException.class, () -> itemService.update(itemDto));

        verify(itemRepo, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the owner is not found")
    void addItemWhenOwnerNotFoundThenThrowException() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(0L);

        when(userService.getById(itemDto.getOwnerId())).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> itemService.add(itemDto));
        verify(userService, times(1)).getById(itemDto.getOwnerId());
        verifyNoInteractions(itemRepo, bookingRepo, commentRepo, requestRepo);
    }

    @Test
    @DisplayName("Should add an item with a request and return the saved item")
    void addItemWithRequest() {
        ItemDto itemDto = new ItemDto(
                0,
                "Test Item",
                "Test Item Description",
                true,
                1,
                1
        );

        User owner = new User(
                1,
                "Test User",
                "testuser@example.com"
        );

        ItemRequest request = new ItemRequest(
                1,
                "Test Request",
                LocalDateTime.now(),
                owner
        );

        when(userService.getById(itemDto.getOwnerId())).thenReturn(owner);
        when(requestRepo.findById(itemDto.getRequestId())).thenReturn(Optional.of(request));

        Item item = new Item(
                0,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );

        when(itemRepo.save(item)).thenReturn(item);

        ItemDto savedItemDto = itemService.add(itemDto);

        verify(userService, times(1)).getById(itemDto.getOwnerId());
        verify(itemRepo, times(1)).save(item);

        assertEquals(itemDto.getId(), savedItemDto.getId());
        assertEquals(itemDto.getName(), savedItemDto.getName());
        assertEquals(itemDto.getDescription(), savedItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), savedItemDto.getAvailable());
        assertEquals(itemDto.getOwnerId(), savedItemDto.getOwnerId());
        assertEquals(itemDto.getRequestId(), savedItemDto.getRequestId());
    }

    @Test
    @DisplayName("Should add an item without a request and return the saved item")
    void addItemWithoutRequest() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1L);
        itemDto.setRequestId(0L);

        User owner = new User();
        owner.setId(1L);
        owner.setName("Test User");
        owner.setEmail("testuser@example.com");

        when(userService.getById(itemDto.getOwnerId())).thenReturn(owner);

        Item item = ItemMapper.toItem(itemDto, owner, null);

        when(itemRepo.save(any())).thenReturn(item);

        ItemDto savedItemDto = itemService.add(itemDto);

        verify(userService, times(1)).getById(itemDto.getOwnerId());

        assertNotNull(savedItemDto);
        assertEquals(itemDto.getName(), savedItemDto.getName());
        assertEquals(itemDto.getDescription(), savedItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), savedItemDto.getAvailable());
        assertEquals(itemDto.getOwnerId(), savedItemDto.getOwnerId());
        assertEquals(itemDto.getRequestId(), savedItemDto.getRequestId());
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException when the itemId is not found")
    void addCommentWhenItemIdNotFoundThenThrowItemNotFoundException() {
        long itemId = 1L;
        long authorId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Test comment");

        when(itemRepo.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.addComment(commentRequestDto, itemId, authorId));

        verify(itemRepo, times(1)).findById(itemId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the authorId is not found")
    void addCommentWhenAuthorIdNotFoundThenThrowUserNotFoundException() {
        long itemId = 1L;
        long authorId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Test comment");

        when(userService.getById(authorId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> itemService.addComment(commentRequestDto, itemId, authorId));

        verify(userService, times(1)).getById(authorId);
        verifyNoMoreInteractions(userService, itemRepo, bookingRepo, commentRepo, requestRepo);
    }

    @Test
    @DisplayName("Should throw CommentingRestrictedException when the user's booking hasn't ended yet or wasn't approved")
    void addCommentWhenUsersBookingHasNotEndedOrNotApprovedThenThrowCommentingRestrictedException() {
        long itemId = 1L;
        long authorId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Test comment");
        User author = new User(authorId, "John Doe", "john.doe@example.com");
        Item item = new Item(1L, "Test item", "Test description", true, author, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), author, item, BookingStatus.APPROVED);
        Collection<Booking> bookings = new ArrayList<>();
        bookings.add(booking);

        when(userService.getById(authorId)).thenReturn(author);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepo.findAllByItem_IdAndBooker_Id(itemId, authorId)).thenReturn(bookings);

        assertThrows(CommentingRestrictedException.class, () -> itemService.addComment(commentRequestDto, itemId, authorId));

        verify(commentRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw CommentingRestrictedException when the user hasn't booked the item")
    void addCommentWhenUserHasNotBookedItemThenThrowCommentingRestrictedException() {// create test data
        long itemId = 1L;
        long authorId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("Test comment");

        User author = new User(authorId, "John Doe", "john.doe@example.com");
        Item item = new Item(itemId, "Test item", "Test description", true, author, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), author, item, BookingStatus.APPROVED);

        when(userService.getById(authorId)).thenReturn(author);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        Comment comment = CommentMapper.toComment(commentRequestDto, 0, LocalDateTime.now(), item, author);
        when(commentRepo.save(any())).thenReturn(comment);
        when(bookingRepo.findAllByItem_IdAndBooker_Id(itemId, authorId)).thenReturn(Collections.singletonList(booking));

        CommentResponseDto commentResponseDto = itemService.addComment(commentRequestDto, itemId, authorId);

        assertNotNull(commentResponseDto);
        assertEquals(commentRequestDto.getText(), commentResponseDto.getText());
        assertEquals(author.getName(), commentResponseDto.getAuthorName());
    }

    @Test
    @DisplayName("Should add a comment when the user has booked the item and the booking has ended")
    void addCommentWhenUserHasBookedItemAndBookingHasEnded() {
        CommentRequestDto commentRequestDto = new CommentRequestDto("Test comment");
        long itemId = 1L;
        long authorId = 2L;

        User author = new User(authorId, "Test User", "test@example.com");
        Item item = new Item(1L, "Test Item", "Test description", true, author, null);
        Booking booking = new Booking(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now(), author, item, BookingStatus.APPROVED);

        when(userService.getById(authorId)).thenReturn(author);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepo.findAllByItem_IdAndBooker_Id(itemId, authorId)).thenReturn(Collections.singletonList(booking));
        when(commentRepo.save(any(Comment.class))).thenReturn(new Comment(1L, commentRequestDto.getText(), LocalDateTime.now(), item, author));

        CommentResponseDto commentResponseDto = itemService.addComment(commentRequestDto, itemId, authorId);

        assertNotNull(commentResponseDto);
        assertEquals(commentRequestDto.getText(), commentResponseDto.getText());
        assertEquals(author.getName(), commentResponseDto.getAuthorName());
    }
}