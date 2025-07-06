package io.beanchain.models;

public class TrustEntry {
    private String pubKey;
    private String address;       // derived from pubKey if needed
    private String ip;
    private int port;
    private boolean isPublic;
    private long firstSeen;
    private long lastPinged;
    private int successfulPings;
    private double trustScore;

    public TrustEntry() {}

    public TrustEntry(String pubKey, String address, String ip, int port) {
        this.pubKey = pubKey;
        this.address = address;
        this.ip = ip;
        this.port = port;
        this.isPublic = true;
        this.firstSeen = System.currentTimeMillis();
        this.trustScore = 0.5; // default initial trust score
    }



    
    public String getPubKey() { return pubKey; }
    public void setPubKey(String pubKey) { this.pubKey = pubKey; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public boolean isPublic() { return isPublic; }
    public void setValidator(boolean validator) { isPublic = validator; }

    public long getFirstSeen() { return firstSeen; }
    public void setFirstSeen(long firstSeen) { this.firstSeen = firstSeen; }

    public long getLastPinged() { return lastPinged; }
    public void setLastPinged(long lastPinged) { this.lastPinged = lastPinged; }

    public int getSuccessfulPings() { return successfulPings; }
    public void setSuccessfulPings(int successfulPings) { this.successfulPings = successfulPings; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }
}
