package de.leuc.adt.quickfix.preferences;

public class OrderByPrefEntry implements Comparable<OrderByPrefEntry> {
    private String tableMatch;
    private String orderBy;

    public String getTableMatch() {
        return tableMatch;
    }

    public void setTableMatch(String tableMatch) {
        this.tableMatch = tableMatch;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int compareTo(OrderByPrefEntry ue) {
        return getTableMatch().compareTo(ue.getTableMatch());
    }

}