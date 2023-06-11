package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.util.constant.Header;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Should add booking")
    public void add() throws Exception {
        long bookerId = 1L;

        LocalDateTime now = LocalDateTime.now();

        BookingRequestDto bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(now.plusDays(1));
        bookingRequestDto.setEnd(now.plusDays(2));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStart(bookingRequestDto.getStart());
        responseDto.setEnd(bookingRequestDto.getEnd());
        responseDto.setStatus(BookingStatus.WAITING);

        when(bookingService.add(bookingRequestDto, bookerId)).thenReturn(responseDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/bookings")
                        .header(Header.USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) responseDto.getId())))
                .andExpect(jsonPath("$.item", nullValue()))
                .andExpect(jsonPath("$.booker", nullValue()))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }


    @Test
    @DisplayName("Should change status")
    public void changeStatus() throws Exception {
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = new BookingResponseDto(bookingId, now.plusDays(1), now.plusDays(2),
                null, null, BookingStatus.WAITING);
        when(bookingService.changeStatus(bookingId, userId, approved)).thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(Header.USER_ID_HEADER, userId)
                        .param("approved", String.valueOf(approved))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.toString()));
    }

    @Test
    @DisplayName("Should get all bookings sorted by start time desc")
    public void getAllSortedByStartTimeDesc() throws Exception {
        long bookerId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = new BookingResponseDto(1L, now.plusDays(1), now.plusDays(2),
                null, null, BookingStatus.WAITING);
        Collection<BookingResponseDto> bookings = Arrays.asList(bookingResponseDto);
        when(bookingService.getAllByBookerIdSortedByStartTimeDesc(bookerId, BookingState.ALL, from, size))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(Header.USER_ID_HEADER, bookerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value(BookingStatus.WAITING.toString()));
    }

    @Test
    @DisplayName("Should get all bookings for user sorted by start time desc")
    public void getAllForUserItemsSortedByStartTimeDesc() throws Exception {
        long itemOwnerId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = new BookingResponseDto(1L, now.plusDays(1), now.plusDays(2),
                null, null, BookingStatus.WAITING);
        Collection<BookingResponseDto> bookings = Arrays.asList(bookingResponseDto);
        when(bookingService.getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, BookingState.ALL, from, size))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(Header.USER_ID_HEADER, itemOwnerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value(BookingStatus.WAITING.toString()));
    }

    @Test
    @DisplayName("Should get booking by id")
    public void getById() throws Exception {
        long bookingId = 1L;
        long userId = 2L;
        BookingResponseDto bookingResponseDto = new BookingResponseDto(bookingId, LocalDateTime.now(),
                LocalDateTime.now().plusHours(1), null, null, BookingStatus.WAITING);
        when(bookingService.getDtoById(bookingId, userId)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(Header.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.toString()));
    }
}
