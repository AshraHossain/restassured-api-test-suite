package com.apitest.payloads;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * UserPayload
 *
 * Builds request body maps for user-related endpoints.
 * Using plain maps keeps the payload visible in test logs without
 * requiring a separate POJO just for construction.
 */
public class UserPayload {

    /** POST /users body */
    public Map<String, Object> createUser(String name, String job) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("job", job);
        return body;
    }

    /** PUT /users/{id} body */
    public Map<String, Object> updateUser(String name, String job) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", name);
        body.put("job", job);
        return body;
    }

    /** PATCH /users/{id} — partial update */
    public Map<String, Object> patchUser(String field, Object value) {
        return Map.of(field, value);
    }
}
