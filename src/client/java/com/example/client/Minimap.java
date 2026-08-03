package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class Minimap {
    public static boolean enabled = false;

    private static final int TEX = 96;
    private static final int VIEW = 96;

    private static DynamicTexture texture = null;
    private static Identifier location = null;
    private static int frame = 0;

    public static void render(GuiGraphics gui, Minecraft client) {
        if (!enabled || client.player == null || client.level == null) {
            return;
        }
        try {
            int[] pos = HudPos.get("minimap", 4, 20);
            int px = pos[0];
            int py = pos[1];
            HudDrag.setArea("minimap", px - 1, py - 1, VIEW + 2, VIEW + 2);
            frame++;
            if (frame >= 4) {
                frame = 0;
                update(client);
            }
            if (texture == null || location == null) {
                return;
            }
            gui.fill(px - 1, py - 1, px + VIEW + 1, py + VIEW + 1, 0xFF000000);
            gui.blit(RenderPipelines.GUI_TEXTURED, location, px, py, 0.0f, 0.0f, VIEW, VIEW, TEX, TEX);
            gui.renderOutline(px, py, VIEW, VIEW, 0xFF3A4A66);
            drawPlayer(gui, client, px, py);
        } catch (Throwable ignored) {
        }
    }

    private static void update(Minecraft client) {
        if (texture == null || location == null
            || client.getTextureManager().getTexture(location) != texture) {
            texture = new DynamicTexture("flash_visual_minimap", TEX, TEX, false);
            location = Identifier.fromNamespaceAndPath("flashvisual", "minimap");
            client.getTextureManager().register(location, texture);
        }
        Level level = client.level;
        BlockPos center = client.player.blockPosition();
        NativeImage img = texture.getPixels();
        if (img == null) {
            return;
        }
        int half = TEX / 2;
        for (int j = 0; j < TEX; j++) {
            for (int i = 0; i < TEX; i++) {
                int wx = center.getX() + i - half;
                int wz = center.getZ() + j - half;
                img.setPixelABGR(i, j, toAbgr(colorFor(level, wx, wz)));
            }
        }
        texture.upload();
    }

    private static int toAbgr(int rgba) {
        int a = (rgba >>> 24) & 0xFF;
        int r = (rgba >> 16) & 0xFF;
        int g = (rgba >> 8) & 0xFF;
        int b = rgba & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private static int colorFor(Level level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        if (state.isAir()) {
            return 0xFF0D0D0D;
        }
        if (state.getFluidState().is(FluidTags.WATER)) {
            return 0xFF2860D8;
        }
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return 0xFFFF5A00;
        }
        Block b = state.getBlock();
        if (b == Blocks.GRASS_BLOCK || b == Blocks.MOSS_BLOCK || b == Blocks.SHORT_GRASS
            || b == Blocks.TALL_GRASS || b == Blocks.FERN || b == Blocks.AZALEA
            || b == Blocks.FLOWERING_AZALEA) {
            return 0xFF53B83A;
        }
        if (b == Blocks.OAK_LEAVES || b == Blocks.SPRUCE_LEAVES || b == Blocks.BIRCH_LEAVES
            || b == Blocks.JUNGLE_LEAVES || b == Blocks.ACACIA_LEAVES || b == Blocks.DARK_OAK_LEAVES
            || b == Blocks.MANGROVE_LEAVES || b == Blocks.CHERRY_LEAVES
            || b == Blocks.AZALEA_LEAVES || b == Blocks.FLOWERING_AZALEA_LEAVES
            || b == Blocks.VINE || b == Blocks.CACTUS || b == Blocks.BAMBOO) {
            return 0xFF3C8C2F;
        }
        if (b == Blocks.OAK_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.BIRCH_LOG
            || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG || b == Blocks.DARK_OAK_LOG
            || b == Blocks.MANGROVE_LOG || b == Blocks.CHERRY_LOG) {
            return 0xFF6B4E2B;
        }
        if (b == Blocks.SAND || b == Blocks.RED_SAND) {
            return 0xFFE9D9A6;
        }
        if (b == Blocks.SANDSTONE || b == Blocks.RED_SANDSTONE) {
            return 0xFFD9C58A;
        }
        if (b == Blocks.DIRT || b == Blocks.COARSE_DIRT || b == Blocks.PODZOL
            || b == Blocks.MUD || b == Blocks.MUDDY_MANGROVE_ROOTS) {
            return 0xFF8B6B4A;
        }
        if (b == Blocks.STONE || b == Blocks.ANDESITE || b == Blocks.GRANITE
            || b == Blocks.DIORITE || b == Blocks.TUFF || b == Blocks.DEEPSLATE) {
            return 0xFF8A8A8A;
        }
        if (b == Blocks.GRAVEL) {
            return 0xFF9C9C9C;
        }
        if (b == Blocks.OBSIDIAN || b == Blocks.BEDROCK || b == Blocks.BLACKSTONE
            || b == Blocks.BASALT || b == Blocks.GILDED_BLACKSTONE) {
            return 0xFF2B2B2B;
        }
        if (b == Blocks.SNOW_BLOCK || b == Blocks.SNOW) {
            return 0xFFF0F0F0;
        }
        if (b == Blocks.ICE || b == Blocks.FROSTED_ICE) {
            return 0xFFA5D6F7;
        }
        if (b == Blocks.PACKED_ICE || b == Blocks.BLUE_ICE) {
            return 0xFF8AC7E8;
        }
        if (b == Blocks.NETHERRACK) {
            return 0xFF7A3B2E;
        }
        if (b == Blocks.NETHER_BRICKS || b == Blocks.RED_NETHER_BRICKS) {
            return 0xFF4A1F1F;
        }
        if (b == Blocks.SOUL_SAND || b == Blocks.SOUL_SOIL) {
            return 0xFF5A4A32;
        }
        if (b == Blocks.END_STONE || b == Blocks.END_STONE_BRICKS) {
            return 0xFFE3E0A6;
        }
        if (b == Blocks.SCULK || b == Blocks.SCULK_VEIN || b == Blocks.SCULK_SENSOR
            || b == Blocks.SCULK_SHRIEKER) {
            return 0xFF0E2B4A;
        }
        if (b == Blocks.SLIME_BLOCK) {
            return 0xFF6CDE4A;
        }
        if (b == Blocks.GLOWSTONE || b == Blocks.SHROOMLIGHT) {
            return 0xFFE8C96A;
        }
        return 0xFF7A7A7A;
    }

    private static void drawPlayer(GuiGraphics gui, Minecraft client, int px, int py) {
        int cx = px + VIEW / 2;
        int cy = py + VIEW / 2;
        double rad = Math.toRadians(client.player.getYRot());
        int ex = cx + (int) (-Math.sin(rad) * 7.0);
        int ey = cy + (int) (Math.cos(rad) * 7.0);
        drawLine(gui, cx, cy, ex, ey, 0xFFFFFFFF);
        gui.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private static void drawLine(GuiGraphics gui, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            gui.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
}
