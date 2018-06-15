package objects;

import java.util.Objects;

public class WinnerDetails {
    //may or may not include all the details
    String playerName, yourTeam, botsTeam, battleHistory;
    int wonCount = 0, lossCount = 0;

    public WinnerDetails(String oppsName) {
        this.playerName = oppsName;
    }

    public void incrementLoss() {
        this.lossCount++;
    }
    public void incrementWin() {
        this.wonCount++;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getYourTeam() {
        return yourTeam;
    }

    public void setYourTeam(String yourTeam) {
        this.yourTeam = yourTeam;
    }

    public String getBotsTeam() {
        return botsTeam;
    }

    public void setBotsTeam(String botsTeam) {
        this.botsTeam = botsTeam;
    }

    public String getBattleHistory() {
        return battleHistory;
    }

    public void setBattleHistory(String battleHistory) {
        this.battleHistory = battleHistory;
    }

    public int getWonCount() {
        return wonCount;
    }

    public void setWonCount(int wonCount) {
        this.wonCount = wonCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WinnerDetails that = (WinnerDetails) o;
        return wonCount == that.wonCount &&
                lossCount == that.lossCount &&
                Objects.equals(playerName, that.playerName) &&
                Objects.equals(yourTeam, that.yourTeam) &&
                Objects.equals(botsTeam, that.botsTeam) &&
                Objects.equals(battleHistory, that.battleHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, yourTeam, botsTeam, battleHistory, wonCount, lossCount);
    }

    @Override
    public String toString() {
        return "WinnerDetails{" +
                "playerName='" + playerName + '\'' +
                ", yourTeam='" + yourTeam + '\'' +
                ", botsTeam='" + botsTeam + '\'' +
                ", battleHistory='" + battleHistory + '\'' +
                ", wonCount=" + wonCount +
                ", lossCount=" + lossCount +
                '}';
    }

}
