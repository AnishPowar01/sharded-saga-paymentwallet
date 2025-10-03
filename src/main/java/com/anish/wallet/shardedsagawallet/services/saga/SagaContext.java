package com.anish.wallet.shardedsagawallet.services.saga;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class SagaContext {
    private Map<String, Object> data;

    public SagaContext(Map<String, Object> value) {
        this.data = value != null ? value : new HashMap<>();
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

}
