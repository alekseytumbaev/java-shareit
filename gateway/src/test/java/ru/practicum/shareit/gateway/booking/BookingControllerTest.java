package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.util.constant.Header;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@MockBean(BookingClient.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should throw exception when start is before end")
    public void addWhenStartBeforeEnd() throws Exception {
        long bookerId = 1L;

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingRequestDto = new BookingRequestDto(now.plusDays(2), now.plusDays(1), 1);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/bookings")
                        .header(Header.USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Should throw exception when unknown booking state")
    public void getAllByBookerIdSortedByStartTimeDesc() throws Exception {
        long bookerId = 1L;
        String state = "Unknown";
        int from = 0;
        int size = 10;
        mockMvc.perform(get("/bookings")
                        .header(Header.USER_ID_HEADER, bookerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}