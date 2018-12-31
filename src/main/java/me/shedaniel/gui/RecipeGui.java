package me.shedaniel.gui;

import me.shedaniel.api.IDisplayCategory;
import me.shedaniel.api.IRecipe;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.Tab;
import me.shedaniel.impl.REIRecipeManager;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecipeGui extends GuiContainer {
    
    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private final MainWindow mainWindow;
    private final Container container;
    private final GuiScreen prevScreen;
    private final Map<IDisplayCategory, List<IRecipe>> recipes;
    private int guiWidth = 176;
    private int guiHeight = 222;
    ArrayList<IDisplayCategory> categories = new ArrayList<>();
    private int categoryTabPage = 0;
    private IDisplayCategory selectedCategory;
    private int recipePointer = 0;
    private List<REISlot> slots;
    private int cycleCounter = 0;
    private int[] itemPointer;
    List<Control> controls = new LinkedList<>();
    private List<Tab> tabs;
    private boolean tabsEnabled = false;
    
    public RecipeGui(Container p_i1072_1_, GuiScreen prevScreen, Map<IDisplayCategory, List<IRecipe>> recipes) {
        super(new RecipeContainer());
        this.container = p_i1072_1_;
        this.prevScreen = prevScreen;
        this.recipes = recipes;
        this.mc = Minecraft.getInstance();
        this.itemRender = mc.getItemRenderer();
        this.fontRenderer = mc.fontRenderer;
        this.mainWindow = Minecraft.getInstance().mainWindow;
        
        setupCategories();
    }
    
    private void setupCategories() {
        for(IDisplayCategory adapter : REIRecipeManager.instance().getDisplayAdapters())
            if (recipes.containsKey(adapter))
                categories.add(adapter);
        selectedCategory = categories.get(0);
        categoryTabPage = 0;
        tabs = new ArrayList<>();
        for(int i = 0; i < 6; i++)
            tabs.add(new Tab(i, 0, 0, 0, 28, 32));
        tabs.forEach(tab -> tab.setOnClick(i -> {
            return onClickTab(tab.getId());
        }));
        updateRecipe();
    }
    
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        int y = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        drawCenteredString(this.fontRenderer, selectedCategory.getDisplayName(), guiLeft + guiWidth / 2, y + 11, -1);
        drawCenteredString(this.fontRenderer, String.format("%d/%d", 1 + getCurrentPage(), getTotalPages()), guiLeft + guiWidth / 2, y + 34, -1);
        controls.forEach(Control::draw);
    }
    
    private int getCurrentPage() {
        return recipePointer / 2;
    }
    
    @Override
    public void tick() {
        super.tick();
        slots.forEach(REISlot::tick);
        controls.forEach(Control::tick);
    }
    
    
    @Override
    public void onResize(Minecraft p_onResize_1_, int p_onResize_2_, int p_onResize_3_) {
        super.onResize(p_onResize_1_, p_onResize_2_, p_onResize_3_);
        updateRecipe();
    }
    
    private void updateRecipe() {
        int categoryPointer = categories.indexOf(selectedCategory);
        IRecipe recipe = recipes.get(categories.get(categoryPointer)).get(recipePointer);
        categories.get(categoryPointer).resetRecipes();
        categories.get(categoryPointer).addRecipe(recipe);
        slots = categories.get(categoryPointer).setupDisplay(0);
        if (recipes.get(categories.get(categoryPointer)).size() >= categoryPointer + 2) {
            IRecipe recipe2 = recipes.get(categories.get(categoryPointer)).get(recipePointer + 1);
            categories.get(categoryPointer).addRecipe(recipe2);
            slots.addAll(categories.get(categoryPointer).setupDisplay(1));
        }
        
        guiLeft = (int) ((mainWindow.getScaledWidth() / 2 - this.guiWidth / 2));
        guiTop = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        
        slots.forEach(reiSlot -> reiSlot.move(guiLeft, guiTop));
        
        Button btnCategoryLeft = new Button(guiLeft + 10, guiTop + 5, 15, 20, "<");
        Button btnCategoryRight = new Button(guiLeft + guiWidth - 25, guiTop + 5, 15, 20, ">");
        btnCategoryRight.onClick = this::btnCategoryRight;
        btnCategoryLeft.onClick = this::btnCategoryLeft;
        
        Button btnRecipeLeft = new Button(guiLeft + 10, guiTop + 28, 15, 20, "<");
        Button btnRecipeRight = new Button(guiLeft + guiWidth - 25, guiTop + 28, 15, 20, ">");
        btnRecipeLeft.setEnabled(recipes.get(categories.get(categoryPointer)).size() > 1 && recipePointer > 0);
        btnRecipeRight.setEnabled(recipes.get(categories.get(categoryPointer)).size() > 1 && getCurrentPage() + 1 < getTotalPages());
        btnRecipeRight.onClick = this::btnRecipeRight;
        btnRecipeLeft.onClick = this::btnRecipeLeft;
        
        controls.clear();
        controls.add(btnCategoryLeft);
        controls.add(btnCategoryRight);
        if (categories.size() <= 1) {
            btnCategoryLeft.setEnabled(false);
            btnCategoryRight.setEnabled(false);
        }
        
        controls.add(btnRecipeLeft);
        controls.add(btnRecipeRight);
        
        itemPointer = new int[9];
        for(int i = 0; i < itemPointer.length; i++) {
            itemPointer[i] = 0;
        }
        
        List<Control> newControls = new LinkedList<>();
        categories.get(categoryPointer).addWidget(newControls, 0);
        if (recipes.get(categories.get(categoryPointer)).size() >= categoryPointer + 2)
            categories.get(categoryPointer).addWidget(newControls, 1);
        newControls.forEach(f -> f.move(guiLeft, guiTop));
        controls.addAll(newControls);
        
        updateTabs();
    }
    
    private void updateTabs() {
        tabsEnabled = guiTop - 28 > 4;
        if (tabsEnabled) {
            tabs.forEach(tab -> tab.moveTo(guiLeft + 4, guiLeft + 2 + tabs.indexOf(tab) * 28, guiTop - 28));
            for(int i = 0; i < tabs.size(); i++) {
                int ref = i + categoryTabPage * 6;
                if (categories.size() > ref) {
                    tabs.get(i).setItem(categories.get(ref).getCategoryIcon(), categories.get(ref).getDisplayName(), categories.get(ref).equals(selectedCategory));
                } else tabs.get(i).setItem(null, null, false);
            }
            controls.addAll(tabs);
        }
    }
    
    private boolean onClickTab(int index) {
        if (index + categoryTabPage * 6 == categories.indexOf(selectedCategory))
            return false;
        selectedCategory = categories.get(index + categoryTabPage * 6);
        updateRecipe();
        return true;
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
        //Tabs
        if (tabsEnabled) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableGUIStandardItemLighting();
            this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
            tabs.stream().filter(tab -> tab.getId() + categoryTabPage * 6 == categories.indexOf(selectedCategory)).forEach(Tab::drawTab);
        }
        
        drawDefaultBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        
        int lvt_4_1_ = (int) ((mainWindow.getScaledWidth() / 2 - this.guiWidth / 2));
        int lvt_5_1_ = (int) ((mainWindow.getScaledHeight() / 2 - this.guiHeight / 2));
        
        this.drawTexturedModalRect(lvt_4_1_, lvt_5_1_, 0, 0, this.guiWidth, this.guiHeight);
        slots.forEach(REISlot::draw);
        
        if (tabsEnabled)
            tabs.stream().filter(tab -> tab.getId() + categoryTabPage * 6 != categories.indexOf(selectedCategory)).forEach(tab -> {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderHelper.enableGUIStandardItemLighting();
                this.mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);
                tab.drawTab();
            });
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 259 && prevScreen != null && REIRenderHelper.focusedControl == null) {
            Minecraft.getInstance().displayGuiScreen(prevScreen);
            return true;
        }
        
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
    
    private boolean btnCategoryLeft(int button) {
        recipePointer = 0;
        int categoryPointer = categories.indexOf(selectedCategory);
        categoryPointer--;
        if (categoryPointer < 0)
            categoryPointer = categories.size() - 1;
        selectedCategory = categories.get(categoryPointer);
        categoryTabPage = categoryPointer / 6;
        updateRecipe();
        return true;
    }
    
    private boolean btnCategoryRight(int button) {
        recipePointer = 0;
        int categoryPointer = categories.indexOf(selectedCategory);
        categoryPointer++;
        if (categoryPointer >= categories.size())
            categoryPointer = 0;
        selectedCategory = categories.get(categoryPointer);
        categoryTabPage = categoryPointer / 6;
        updateRecipe();
        return true;
    }
    
    private boolean btnRecipeLeft(int button) {
        recipePointer -= 2;
        if (recipePointer < 0) {
            recipePointer = (getTotalPages() - 1) * 2;
        }
        updateRecipe();
        return true;
    }
    
    private boolean btnRecipeRight(int button) {
        recipePointer += 2;
        if (recipePointer >= recipes.get(selectedCategory).size()) {
            recipePointer = 0;
        }
        updateRecipe();
        return true;
    }
    
    private int riseDoublesToInt(double i) {
        return (int) (i + (i % 1 == 0 ? 0 : 1));
    }
    
    private int getTotalPages() {
        return MathHelper.clamp(riseDoublesToInt(recipes.get(selectedCategory).size() / 2), 1, Integer.MAX_VALUE);
    }
}
