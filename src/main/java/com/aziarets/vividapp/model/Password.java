package com.aziarets.vividapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Password {

    @JsonProperty("pws")
    private List<String> values;

    private Object error;

    public Password() {
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }
}
