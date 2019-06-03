package spritegenerator;

/**
 *
 * @author Everol
 */
public class SelectColsRows {
    
    int cols;
    int rows;
    String text;
    
    public SelectColsRows() {
    }
    
    public SelectColsRows(int cols, int rows, String text) {
        this.cols = cols;
        this.rows = rows;
        this.text = text;
    }
    
    public int getCols() {
        return cols;
    }
    public void setCols(int cols) {
        this.cols = cols;
    }
    
    public int getRows() {
        return rows;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    
    
    @Override
    public String toString() {
        //return "[ cols=" + cols + ", rows=" + rows + ", text=" + text + "]";
        return text;
    }
    
    
    
}
