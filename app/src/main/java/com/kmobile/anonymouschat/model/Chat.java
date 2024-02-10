package com.kmobile.anonymouschat.model;

public class Chat {
    private String mesajIcerigi, gonderen, alici, mesajTipi, docID;

    public Chat() {
    }

    public Chat(String mesajIcerigi, String gonderen, String alici, String mesajTipi, String docID) {
        this.mesajIcerigi = mesajIcerigi;
        this.gonderen = gonderen;
        this.alici = alici;
        this.mesajTipi = mesajTipi;
        this.docID = docID;
    }

    public String getMesajIcerigi() {
        return mesajIcerigi;
    }

    public void setMesajIcerigi(String mesajIcerigi) {
        this.mesajIcerigi = mesajIcerigi;
    }

    public String getGonderen() {
        return gonderen;
    }

    public void setGonderen(String gonderen) {
        this.gonderen = gonderen;
    }

    public String getAlici() {
        return alici;
    }

    public void setAlici(String alici) {
        this.alici = alici;
    }

    public String getMesajTipi() {
        return mesajTipi;
    }

    public void setMesajTipi(String mesajTipi) {
        this.mesajTipi = mesajTipi;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }
}
