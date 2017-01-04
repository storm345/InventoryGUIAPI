package me.eddie.inventoryguiapi.gui.elements;

/**
 * A basic and incomplete implementation of GUIElement that provides code for managing the desired slot for a GUIElement.
 */
public abstract class AbstractGUIElement implements GUIElement {
    /**
     * The value of slot that, internally, indicates that the element has no desired slot. This shouldn't be relied on, use of
     * {@link #hasDesiredDisplayPosition()} should be used instead
     */
    public static final int NO_DESIRED_SLOT = -1;
    private int slot = NO_DESIRED_SLOT;

    //Blank constructor
    public AbstractGUIElement(){

    }

    //Constructor that allows specifying of a slot
    public AbstractGUIElement(int slot){
        if(slot < 0 && slot != NO_DESIRED_SLOT){
            throw new IllegalArgumentException("Desired position must be greater than or equal to 0");
        }
        this.slot = slot;
    }

    /**
     * Sets if this GUIElement has a desired display position. True will indicate that it has, false will indicate it hasn't
     * @param has Whether or not it has a desired display position
     */
    public void setHasDesiredDisplayPosition(boolean has){
        slot = has ? (slot >= 0 ? slot : 0) : NO_DESIRED_SLOT; //If false then set to -1 (< 0 so indicates that it has not). If true then use current slot if possible, 0 if not
    }

    /**
     * Sets the desired display position of this GUIElement. Will also make {@link #hasDesiredDisplayPosition()} be true
     * @param slot The slot in the GUI to make this element's desired display position
     */
    public void setDesiredDisplayPosition(int slot){
        if(slot < 0 && slot != NO_DESIRED_SLOT){
            throw new IllegalArgumentException("Desired display position must be >= 0");
        }
        this.slot = slot;
    }

    @Override
    public boolean hasDesiredDisplayPosition() {
        return slot >= 0;
    }

    @Override
    public int getDesiredDisplayPosition() {
        return slot;
    }
}
