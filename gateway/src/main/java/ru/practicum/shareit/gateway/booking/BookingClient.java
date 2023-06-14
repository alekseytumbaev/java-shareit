package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.util.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> add(long userId, BookingRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> changeStatus(long bookingId, boolean approved, long userId) {
        return patch("/" + bookingId + "?approved={approved}", userId, Map.of("approved", approved), null);
    }

    public ResponseEntity<Object> getAllByBookerIdSortedByStartTimeDesc(long bookerId, BookingState bookingState,
                                                                        int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", bookingState.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", bookerId, parameters);
    }

    public ResponseEntity<Object> getAllByItemOwnerIdSortedByStartTimeDesc(long itemOwnerId, BookingState bookingState, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", bookingState.name(),
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", itemOwnerId, parameters);
    }

    public ResponseEntity<Object> getById(long bookingId, long userId) {
        return get("/" + bookingId, userId);
    }
}
