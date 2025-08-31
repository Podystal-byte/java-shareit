package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

import static java.lang.String.format;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(format("%s%s", serverUrl, API_PREFIX))).requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public ResponseEntity<Object> getAllItemsByUserId(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        return get(format("/%d", itemId), userId);
    }

    public ResponseEntity<Object> getBySearchByNameAndDescription(String text) {
        Map<String, Object> parameters = Map.of("text", text);

        return get("/search", 0L, parameters);
    }

    public ResponseEntity<Object> postCreateItem(Long userId, ItemDto requestBody) {
        return post("", userId, requestBody);
    }

    public ResponseEntity<Object> patchItem(Long userId, Long itemId, ItemDto itemDto) {
        return patch(format("/%d", itemId), userId, itemDto);
    }


    public ResponseEntity<Object> postSaveCommentToItem(Long userId, Long itemId, CommentDto requestCommentDto) {
        return post(format("/%d/comment", itemId), userId, requestCommentDto);
    }
}