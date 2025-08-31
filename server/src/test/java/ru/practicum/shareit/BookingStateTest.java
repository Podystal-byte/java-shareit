package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BookingStateTest {

    @Test
    public void from_whenValidStringIsProvided_thenReturnsCorrectState() {
        String allStateString = "ALL";
        String currentStateString = "CURRENT";
        String futureStateString = "FUTURE";
        String pastStateString = "PAST";
        String waitingStateString = "WAITING";
        String rejectedStateString = "REJECTED";

        BookingState allState = BookingState.from(allStateString);
        BookingState currentState = BookingState.from(currentStateString);
        BookingState futureState = BookingState.from(futureStateString);
        BookingState pastState = BookingState.from(pastStateString);
        BookingState waitingState = BookingState.from(waitingStateString);
        BookingState rejectedState = BookingState.from(rejectedStateString);

        assertEquals(BookingState.ALL, allState);
        assertEquals(BookingState.CURRENT, currentState);
        assertEquals(BookingState.FUTURE, futureState);
        assertEquals(BookingState.PAST, pastState);
        assertEquals(BookingState.WAITING, waitingState);
        assertEquals(BookingState.REJECTED, rejectedState);
    }

    @Test
    public void from_whenLowercaseStringIsProvided_thenReturnsCorrectState() {
        String allStateString = "all";
        String futureStateString = "future";

        BookingState allState = BookingState.from(allStateString);
        BookingState futureState = BookingState.from(futureStateString);

        assertEquals(BookingState.ALL, allState);
        assertEquals(BookingState.FUTURE, futureState);
    }

    @Test
    public void from_whenInvalidStringIsProvided_thenThrowsException() {
        String invalidStateString = "UNKNOWN";

        assertThrows(IllegalArgumentException.class, () -> BookingState.from(invalidStateString));
    }
}
