package model.api_entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class ComponentData {
    private String code;
    private String unit;
    private String period_type;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPeriod_type() {
        return period_type;
    }

    public void setPeriod_type(String period_type) {
        this.period_type = period_type;
    }

    @Override
    public String toString() {
        return "ComponentData{" +
                "code='" + code + '\'' +
                ", unit='" + unit + '\'' +
                ", period_type='" + period_type + '\'' +
                '}';
    }


}
