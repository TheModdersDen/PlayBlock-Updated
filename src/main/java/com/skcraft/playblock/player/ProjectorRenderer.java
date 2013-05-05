package com.skcraft.playblock.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.skcraft.playblock.util.DrawUtils;

/**
 * Renders the screen for the projector blocks.
 */
public class ProjectorRenderer extends TileEntitySpecialRenderer {

    /**
     * Determines the scale of the text on the screen.
     */
    private static final float TEXT_SCALE = 0.05f;
    
    private final MediaManager mediaManager;

    /**
     * Construct a new instance.
     * 
     * @param mediaManager the media manager.
     */
    public ProjectorRenderer(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y,
            double z, float direction) {

        ProjectorTileEntity projector = ((ProjectorTileEntity) tileEntity);
        float width = projector.getWidth();
        float height = projector.getHeight();
        int metadata = tileEntity.getBlockMetadata();
        float rot;

        // Calculate the rotation
        switch (metadata) {
        case 0:
            rot = 180;
            break;
        case 2:
            rot = 0;
            break;
        default:
            rot = metadata * 90;
        }

        GL11.glPushMatrix();
        { // TODO: Consider using FBOs?
            GL11.glTranslatef((float) x + 0.5f, (float) y + 0.5f, (float) z + 0.5f);
            GL11.glRotatef(rot, 0, 1, 0);
            GL11.glTranslatef((float) (width / 2.0), (float) (height / 2.0), -0.51f);
            GL11.glScalef(-1, -1, 1);
            GL11.glDisable(GL11.GL_LIGHTING);
            drawScreen(projector, width, height);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        GL11.glPopMatrix();
    }

    /**
     * Draw the video screen, which may include error messages.
     * 
     * @param projector the projector
     * @param width the width
     * @param height the height
     */
    private void drawScreen(ProjectorTileEntity projector, float width, float height) {
        if (!mediaManager.isAvailable()) {
            DrawUtils.drawRect(0, 0, width, height, 0xff000000);
            drawMessage("See skcraft.com/vlc!", width, height, 0xffff0000);
        } else {
            MediaRenderer renderer = projector.getRenderer();

            if (renderer != null) {
                // A hack to have the main MC volume apply to the player
                mediaManager
                        .setVolume(Minecraft.getMinecraft().gameSettings.soundVolume);

                renderer.drawMedia(0, 0, width, height);
                MediaStatus status = renderer.getStatus();

                if (status == MediaStatus.BUFFERING) {
                    drawMessage("Buffering...", width, height);
                } else if (status == MediaStatus.ERROR) {
                    drawMessage("Error occurred!", width, height, 0xffff0000);
                } else if (status == MediaStatus.PAUSED) {
                    drawMessage("Media paused.", width, height);
                } else if (status == MediaStatus.STOPPED) {
                    drawMessage("Stopped.", width, height);
                }
            } else {
                DrawUtils.drawRect(0, 0, width, height, 0xff000000);
            }
        }
    }

    /**
     * Calls {@link #drawMessage(String, float, float, int)} but with a default
     * background.
     * 
     * @param text the text to draw
     * @param width the width of the screen
     * @param height the height of the screen
     */
    private void drawMessage(String text, float width, float height) {
        drawMessage(text, width, height, 0xff0000ff);
    }

    /**
     * Draw a message in the middle of the screen that's easy to read but may obscure
     * any image behind the text.
     * 
     * @param text the text to draw
     * @param width the width of the screen
     * @param height the height of the screen
     * @param bgColor the background color of the text
     */
    private void drawMessage(String text, float width, float height,
            int bgColor) {
        float textWidth = getFontRenderer().getStringWidth(text) * TEXT_SCALE;
        float textHeight = getFontRenderer().FONT_HEIGHT * TEXT_SCALE;

        float x = (float) ((width - textWidth) / 2.0);
        float y = (float) ((height - textHeight) / 2.0);

        GL11.glTranslatef(0.1f + x, 0.1f + y, -0.01f);
        DrawUtils.drawRect(0, 0, textWidth, textHeight, bgColor);
        GL11.glTranslatef(0, 0, -0.01f);
        GL11.glScalef(TEXT_SCALE, TEXT_SCALE, 1f);
        getFontRenderer().drawString(text, 0, 0, 0xffffffff, false);
    }

}