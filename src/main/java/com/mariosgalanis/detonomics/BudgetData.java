package com.mariosgalanis.detonomics;

import java.util.ArrayList;

public class BudgetData {
    private Information information;
    private ArrayList<BudgetItem> revenues;
    private ArrayList<BudgetItem> expenditures;
    private ArrayList<GovernmentEntity> entities;

    public Information getInformation() {
        return information;
    }

    public ArrayList<BudgetItem> getRevenues() {
        return revenues;
    }

    public ArrayList<BudgetItem> getExpenditures() {
        return expenditures;
    }

    public ArrayList<GovernmentEntity> getEntities() {
        return entities;
    }
}
