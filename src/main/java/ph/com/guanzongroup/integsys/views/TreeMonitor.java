/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ph.com.guanzongroup.integsys.views;

/**
 *
 * @author GMC_SEG09
 */
public class TreeMonitor {
    private String sSysMnuCd;
    private String psMenuGrpx;
    private String psMenuName;
    private String psDescription;
    private String psMenuCode;
    private String psIndustry;
    private String psCategory;
    private Runnable prAction;
    
     public TreeMonitor(String id, String groupId, String name, String description,
                       String menuCode,String industry,String category) {
        this.sSysMnuCd = id;
        this.psMenuGrpx = groupId;
        this.psMenuName = name;
        this.psDescription = description;
        this.psMenuCode = menuCode;
        this.psIndustry = industry;
        this.psCategory = category;
    }

    public String getSystemId() { return sSysMnuCd; }
    public String getGroup() { return psMenuGrpx; }
    public String getName() { return psMenuName; }
    public String getDescription() { return psDescription; }
    public String getMenuCode() { return psMenuCode; }
    public String getIndustry() { return psIndustry; }
    public String getCategory() { return psCategory; }
    
     public Runnable getAction() {
        return prAction;
    }
     public void setAction(Runnable action) {
        prAction = action;
    }

    @Override
    public String toString() {
        return psMenuName;
    }

}
