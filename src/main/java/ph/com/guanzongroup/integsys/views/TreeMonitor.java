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
    private String id;
    private String parentId;
    private String name;
    private String description;
    private Runnable action; // what to do when clicked

    public TreeMonitor(String id, String parentId, String name, String description, Runnable action) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.description = description;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Runnable getAction() {
        return action;
    }

    public void runAction() {
        if (action != null) {
            action.run();
        } else {
            System.out.println("No action assigned for " + name);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
