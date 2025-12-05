package de.jackson.creatoros.domain;

public class Reward {

    private RewardType type;

    // ITEM
    private String itemMaterial; // z.B. "DIAMOND"
    private int itemAmount;

    // MONEY (Vault)
    private double moneyAmount;

    // TITLE / TAG
    private String text;

    public Reward() {
    }

    public Reward(RewardType type) {
        this.type = type;
    }

    public RewardType getType() {
        return type;
    }

    public void setType(RewardType type) {
        this.type = type;
    }

    public String getItemMaterial() {
        return itemMaterial;
    }

    public void setItemMaterial(String itemMaterial) {
        this.itemMaterial = itemMaterial;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public double getMoneyAmount() {
        return moneyAmount;
    }

    public void setMoneyAmount(double moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
