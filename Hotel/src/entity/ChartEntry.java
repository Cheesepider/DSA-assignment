package entity;

/**
 
 * @author Kao Yong Feng
 */
public class ChartEntry {

    private String label;
    private int value;

    public ChartEntry() {
    }

    public ChartEntry(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChartEntry)) {
            return false;
        }
        ChartEntry other = (ChartEntry) obj;
        return this.value == other.value
                && (this.label == null ? other.label == null : this.label.equals(other.label));
    }

    @Override
    public int hashCode() {
        int result = label == null ? 0 : label.hashCode();
        result = 31 * result + value;
        return result;
    }

    @Override
    public String toString() {
        return "ChartEntry{" +
                "label='" + label + '\'' +
                ", value=" + value +
                '}';
    }
}