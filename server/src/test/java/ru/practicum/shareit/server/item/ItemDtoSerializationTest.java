package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.server.item.model.dto.ItemDto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
public class ItemDtoSerializationTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    public void testSerialize() throws Exception {
        ItemDto itemDto = new ItemDto(1, "Item Name", "Item Description", true, 0, 0);
        String jsonContent = json.write(itemDto).getJson();
        assertThat(jsonContent).doesNotContain("ownerId");
    }
}
