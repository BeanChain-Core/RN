package io.beanchain.models;

public class TrustListEntry {
    private String pubKey;
    private double trustScore;
    private String ip;
    private int port;

    public TrustListEntry(TrustEntry entry) {
        this.pubKey = entry.getPubKey();
        this.trustScore = entry.getTrustScore();
        this.ip = entry.getIp();
        this.port = entry.getPort();
    }

    // Getters/setters omitted for brevity
}
