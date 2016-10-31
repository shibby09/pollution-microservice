package model.api_entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class StationData {
    private String label;
    private Map<Long, Integer> data;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<Long, Integer> getData() {
        return data;
    }

    public void setData(Map<Long, Integer> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StationData{" +
                "label='" + label + '\'' +
                ", data=" + data +
                '}';
    }
}
