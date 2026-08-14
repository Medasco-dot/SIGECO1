package com.carfo.contentieux.dto;

import com.carfo.contentieux.model.TypeContentieux;
import jakarta.validation.constraints.NotNull;

public class TypeContentieuxDTO {

    private Integer numContentieux;

    @NotNull
    private TypeContentieux.Nature nature;

    public Integer getNumContentieux() { return numContentieux; }
    public void setNumContentieux(Integer numContentieux) { this.numContentieux = numContentieux; }

    public TypeContentieux.Nature getNature() { return nature; }
    public void setNature(TypeContentieux.Nature nature) { this.nature = nature; }
}
