package com.bfu.family;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Member {
    private String code;
    private String nameEnglish;
    private String nameSinhala;
    private String gender;
    private String country;
    private String city;
    private String citizenship;
    private String telephone;
    private String email;
    private String profession;

    public Member(String code, String nameEnglish, String nameSinhala, String gender, String country, String city, String citizenship, String telephone, String email, String profession, String father, String mother) {
        this.code = code;
        this.nameEnglish = nameEnglish;
        this.nameSinhala = nameSinhala;
        this.gender = gender;
        this.country = country;
        this.city = city;
        this.citizenship = citizenship;
        this.telephone = telephone;
        this.email = email;
        this.profession = profession;
        this.father = father;
        this.mother = mother;
        children = new LinkedHashMap<>();
    }

    public Member(String code, String nameEnglish, String nameSinhala, String gender, String father, String mother) {
        this.code = code;
        this.nameEnglish = nameEnglish;
        this.nameSinhala = nameSinhala;
        this.gender = gender;
        this.father = father;
        this.mother = mother;
        children = new LinkedHashMap<>();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    private String father;
    private String mother;

    public String getFather() {
        return father;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    // *Due to the design of the js tree, In  a given scenario one member only have spouse or children*
    private HashMap<String, Member> children;


    public HashMap<String, Member> getChildren() {
        return children;
    }


    public void addChild(final Member child) {
        this.children.put(child.getCode(), child);
    }

    public void addChildren(final LinkedHashMap children) {
        this.children.putAll(children);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getNameSinhala() {
        return nameSinhala;
    }

    public void setNameSinhala(String nameSinhala) {
        this.nameSinhala = nameSinhala;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
