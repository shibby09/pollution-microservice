package model.api_entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentInformation {
    private String code;
    private String name;
    private String description;
    private ComponentData[] components;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComponentData[] getComponents() {
        return components;
    }

    public void setComponents(ComponentData[] components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "ComponentInformation{" +
                "code='" + code + '\'' +
                ", label='" + name + '\'' +
                ", description='" + description + '\'' +
                ", components=" + Arrays.toString(components) +
                '}';
    }
}
