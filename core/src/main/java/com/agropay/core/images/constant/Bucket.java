package com.agropay.core.images.constant;

public enum Bucket {
    EMPLOYEE_PROFILE("employee-profiles"),
    PERSON_PHOTO("person-photos"),
    COMPANY_LOGO("company-logos"),
    CONTRACT_DOCUMENT("contract-documents"),
    MENU_ICON("menu-icons"),
    SIGNATURE("signatures");

    private final String folderName;

    Bucket(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
