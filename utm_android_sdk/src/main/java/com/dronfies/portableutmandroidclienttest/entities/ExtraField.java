package com.dronfies.portableutmandroidclienttest.entities;

import java.util.Objects;

public class ExtraField {

    public enum EnumExtraFieldType { STRING, DATE, FILE, BOOL, NUMBER }

    private String name;
    private EnumExtraFieldType type;
    private boolean required;

    public ExtraField(String name, EnumExtraFieldType type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumExtraFieldType getType() {
        return type;
    }

    public void setType(EnumExtraFieldType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtraField that = (ExtraField) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
