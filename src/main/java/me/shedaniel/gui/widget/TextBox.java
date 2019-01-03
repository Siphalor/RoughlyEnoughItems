package me.shedaniel.gui.widget;

import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;

import java.awt.*;

/**
 * Created by James on 8/3/2018.
 */
public class TextBox extends Control implements IFocusable {
    
    private GuiTextField textField;
    
    public TextBox(int x, int y, int width, int height) {
        super(x, y, width, height);
        textField = new GuiTextField(-1, REIRenderHelper.getFontRenderer(), x, y, width, height);
        this.onClick = this::doMouseClick;
        this.onKeyDown = this::onKeyPressed;
        this.charPressed = this::charTyped;
    }
    
    public TextBox(Rectangle rectangle) {
        super(rectangle);
        textField = new GuiTextField(-1, REIRenderHelper.getFontRenderer(), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        this.onClick = this::doMouseClick;
        this.onKeyDown = this::onKeyPressed;
        this.charPressed = this::charTyped;
    }
    
    @Override
    public void draw() {
        RenderHelper.disableStandardItemLighting();
        textField.drawTextField(0, 0, 0);
    }
    
    @Override
    public boolean hasFocus() {
        return textField.isFocused();
    }
    
    @Override
    public void setFocused(boolean val) {
        textField.setFocused(val);
    }
    
    protected boolean doMouseClick(int button) {
        Point mouseLoc = REIRenderHelper.getMouseLoc();
        if (!hasFocus())
            setFocused(true);
        return textField.mouseClicked(mouseLoc.x, mouseLoc.y, 0);
    }
    
    protected boolean onKeyPressed(int first, int second, int third) {
        boolean handled = textField.keyPressed(first, second, third);
        if (handled) {
            REIRenderHelper.updateSearch();
        }
        
        return handled;
    }
    
    public String getText() {
        return textField.getText();
    }
    
    public void setText(String value) {
        textField.setText(value);
    }
    
    protected void charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        textField.charTyped(p_charTyped_1_, p_charTyped_2_);
        REIRenderHelper.updateSearch();
    }
    
    @Override
    public void tick() {
        textField.tick();
    }
}
