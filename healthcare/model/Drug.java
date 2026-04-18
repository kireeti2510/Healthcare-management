package com.healthcare.model;

import java.util.List;

public class Drug {
    private String drugCode;
    private String name;
    private List<String> contraindications;

    public Drug(String drugCode, String name, List<String> contraindications) {
        this.drugCode = drugCode;
        this.name = name;
        this.contraindications = contraindications;
    }

    public String getDrugCode() { return drugCode; }
    public String getName() { return name; }
    public List<String> getContraindications() { return contraindications; }
}
