package ph.com.guanzongroup.integsys.model;


/**
 *
 * @author User
 */
public class ModelLog_In_Category {
    private String categoryID;
    private String categoryName;

    public ModelLog_In_Category(String categoryID, String categoryName) {
        this.categoryID = categoryID;
            this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}
