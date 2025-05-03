package fun.rockstarity.api.scripts.wrappers.base;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import fun.rockstarity.api.IAccess;

public class GL11Base implements IAccess {
	/* BeginMode */
	public int GL_POINTS = 0x0000;
	public int GL_LINES = 0x0001;
	public int GL_LINE_LOOP = 0x0002;
	public int GL_LINE_STRIP = 0x0003;
	public int GL_TRIANGLES = 0x0004;
	public int GL_TRIANGLE_STRIP = 0x0005;
	public int GL_TRIANGLE_FAN = 0x0006;
	public int GL_QUADS = 0x0007;
	public int GL_QUAD_STRIP = 0x0008;
	public int GL_POLYGON = 0x0009;
	
	/* GetTarget */
	public int GL_CURRENT_COLOR = 0x0B00;
	public int GL_CURRENT_INDEX = 0x0B01;
	public int GL_CURRENT_NORMAL = 0x0B02;
	public int GL_CURRENT_TEXTURE_COORDS = 0x0B03;
	public int GL_CURRENT_RASTER_COLOR = 0x0B04;
	public int GL_CURRENT_RASTER_INDEX = 0x0B05;
	public int GL_CURRENT_RASTER_TEXTURE_COORDS = 0x0B06;
	public int GL_CURRENT_RASTER_POSITION = 0x0B07;
	public int GL_CURRENT_RASTER_POSITION_VALID = 0x0B08;
	public int GL_CURRENT_RASTER_DISTANCE = 0x0B09;
	public int GL_POINT_SMOOTH = 0x0B10;
	public int GL_POINT_SIZE = 0x0B11;
	public int GL_POINT_SIZE_RANGE = 0x0B12;
	public int GL_POINT_SIZE_GRANULARITY = 0x0B13;
	public int GL_LINE_SMOOTH = 0x0B20;
	public int GL_LINE_WIDTH = 0x0B21;
	public int GL_LINE_WIDTH_RANGE = 0x0B22;
	public int GL_LINE_WIDTH_GRANULARITY = 0x0B23;
	public int GL_LINE_STIPPLE = 0x0B24;
	public int GL_LINE_STIPPLE_PATTERN = 0x0B25;
	public int GL_LINE_STIPPLE_REPEAT = 0x0B26;
	public int GL_LIST_MODE = 0x0B30;
	public int GL_MAX_LIST_NESTING = 0x0B31;
	public int GL_LIST_BASE = 0x0B32;
	public int GL_LIST_INDEX = 0x0B33;
	public int GL_POLYGON_MODE = 0x0B40;
	public int GL_POLYGON_SMOOTH = 0x0B41;
	public int GL_POLYGON_STIPPLE = 0x0B42;
	public int GL_EDGE_FLAG = 0x0B43;
	public int GL_CULL_FACE = 0x0B44;
	public int GL_CULL_FACE_MODE = 0x0B45;
	public int GL_FRONT_FACE = 0x0B46;
	public int GL_LIGHTING = 0x0B50;
	public int GL_LIGHT_MODEL_LOCAL_VIEWER = 0x0B51;
	public int GL_LIGHT_MODEL_TWO_SIDE = 0x0B52;
	public int GL_LIGHT_MODEL_AMBIENT = 0x0B53;
	public int GL_SHADE_MODEL = 0x0B54;
	public int GL_COLOR_MATERIAL_FACE = 0x0B55;
	public int GL_COLOR_MATERIAL_PARAMETER = 0x0B56;
	public int GL_COLOR_MATERIAL = 0x0B57;
	public int GL_FOG = 0x0B60;
	public int GL_FOG_INDEX = 0x0B61;
	public int GL_FOG_DENSITY = 0x0B62;
	public int GL_FOG_START = 0x0B63;
	public int GL_FOG_END = 0x0B64;
	public int GL_FOG_MODE = 0x0B65;
	public int GL_FOG_COLOR = 0x0B66;
	public int GL_DEPTH_RANGE = 0x0B70;
	public int GL_DEPTH_TEST = 0x0B71;
	public int GL_DEPTH_WRITEMASK = 0x0B72;
	public int GL_DEPTH_CLEAR_VALUE = 0x0B73;
	public int GL_DEPTH_FUNC = 0x0B74;
	public int GL_ACCUM_CLEAR_VALUE = 0x0B80;
	public int GL_STENCIL_TEST = 0x0B90;
	public int GL_STENCIL_CLEAR_VALUE = 0x0B91;
	public int GL_STENCIL_FUNC = 0x0B92;
	public int GL_STENCIL_VALUE_MASK = 0x0B93;
	public int GL_STENCIL_FAIL = 0x0B94;
	public int GL_STENCIL_PASS_DEPTH_FAIL = 0x0B95;
	public int GL_STENCIL_PASS_DEPTH_PASS = 0x0B96;
	public int GL_STENCIL_REF = 0x0B97;
	public int GL_STENCIL_WRITEMASK = 0x0B98;
	public int GL_MATRIX_MODE = 0x0BA0;
	public int GL_NORMALIZE = 0x0BA1;
	public int GL_VIEWPORT = 0x0BA2;
	public int GL_MODELVIEW_STACK_DEPTH = 0x0BA3;
	public int GL_PROJECTION_STACK_DEPTH = 0x0BA4;
	public int GL_TEXTURE_STACK_DEPTH = 0x0BA5;
	public int GL_MODELVIEW_MATRIX = 0x0BA6;
	public int GL_PROJECTION_MATRIX = 0x0BA7;
	public int GL_TEXTURE_MATRIX = 0x0BA8;
	public int GL_ATTRIB_STACK_DEPTH = 0x0BB0;
	public int GL_CLIENT_ATTRIB_STACK_DEPTH = 0x0BB1;
	public int GL_ALPHA_TEST = 0x0BC0;
	public int GL_ALPHA_TEST_FUNC = 0x0BC1;
	public int GL_ALPHA_TEST_REF = 0x0BC2;
	public int GL_DITHER = 0x0BD0;
	public int GL_BLEND_DST = 0x0BE0;
	public int GL_BLEND_SRC = 0x0BE1;
	public int GL_BLEND = 0x0BE2;
	public int GL_LOGIC_OP_MODE = 0x0BF0;
	public int GL_INDEX_LOGIC_OP = 0x0BF1;
	public int GL_COLOR_LOGIC_OP = 0x0BF2;
	public int GL_AUX_BUFFERS = 0x0C00;
	public int GL_DRAW_BUFFER = 0x0C01;
	public int GL_READ_BUFFER = 0x0C02;
	public int GL_SCISSOR_BOX = 0x0C10;
	public int GL_SCISSOR_TEST = 0x0C11;
	public int GL_INDEX_CLEAR_VALUE = 0x0C20;
	public int GL_INDEX_WRITEMASK = 0x0C21;
	public int GL_COLOR_CLEAR_VALUE = 0x0C22;
	public int GL_COLOR_WRITEMASK = 0x0C23;
	public int GL_INDEX_MODE = 0x0C30;
	public int GL_RGBA_MODE = 0x0C31;
	public int GL_DOUBLEBUFFER = 0x0C32;
	public int GL_STEREO = 0x0C33;
	public int GL_RENDER_MODE = 0x0C40;
	public int GL_PERSPECTIVE_CORRECTION_HINT = 0x0C50;
	public int GL_POINT_SMOOTH_HINT = 0x0C51;
	public int GL_LINE_SMOOTH_HINT = 0x0C52;
	public int GL_POLYGON_SMOOTH_HINT = 0x0C53;
	public int GL_FOG_HINT = 0x0C54;
	public int GL_TEXTURE_GEN_S = 0x0C60;
	public int GL_TEXTURE_GEN_T = 0x0C61;
	public int GL_TEXTURE_GEN_R = 0x0C62;
	public int GL_TEXTURE_GEN_Q = 0x0C63;
	public int GL_PIXEL_MAP_I_TO_I = 0x0C70;
	public int GL_PIXEL_MAP_S_TO_S = 0x0C71;
	public int GL_PIXEL_MAP_I_TO_R = 0x0C72;
	public int GL_PIXEL_MAP_I_TO_G = 0x0C73;
	public int GL_PIXEL_MAP_I_TO_B = 0x0C74;
	public int GL_PIXEL_MAP_I_TO_A = 0x0C75;
	public int GL_PIXEL_MAP_R_TO_R = 0x0C76;
	public int GL_PIXEL_MAP_G_TO_G = 0x0C77;
	public int GL_PIXEL_MAP_B_TO_B = 0x0C78;
	public int GL_PIXEL_MAP_A_TO_A = 0x0C79;
	public int GL_PIXEL_MAP_I_TO_I_SIZE = 0x0CB0;
	public int GL_PIXEL_MAP_S_TO_S_SIZE = 0x0CB1;
	public int GL_PIXEL_MAP_I_TO_R_SIZE = 0x0CB2;
	public int GL_PIXEL_MAP_I_TO_G_SIZE = 0x0CB3;
	public int GL_PIXEL_MAP_I_TO_B_SIZE = 0x0CB4;
	public int GL_PIXEL_MAP_I_TO_A_SIZE = 0x0CB5;
	public int GL_PIXEL_MAP_R_TO_R_SIZE = 0x0CB6;
	public int GL_PIXEL_MAP_G_TO_G_SIZE = 0x0CB7;
	public int GL_PIXEL_MAP_B_TO_B_SIZE = 0x0CB8;
	public int GL_PIXEL_MAP_A_TO_A_SIZE = 0x0CB9;
	public int GL_UNPACK_SWAP_BYTES = 0x0CF0;
	public int GL_UNPACK_LSB_FIRST = 0x0CF1;
	public int GL_UNPACK_ROW_LENGTH = 0x0CF2;
	public int GL_UNPACK_SKIP_ROWS = 0x0CF3;
	public int GL_UNPACK_SKIP_PIXELS = 0x0CF4;
	public int GL_UNPACK_ALIGNMENT = 0x0CF5;
	public int GL_PACK_SWAP_BYTES = 0x0D00;
	public int GL_PACK_LSB_FIRST = 0x0D01;
	public int GL_PACK_ROW_LENGTH = 0x0D02;
	public int GL_PACK_SKIP_ROWS = 0x0D03;
	public int GL_PACK_SKIP_PIXELS = 0x0D04;
	public int GL_PACK_ALIGNMENT = 0x0D05;
	public int GL_MAP_COLOR = 0x0D10;
	public int GL_MAP_STENCIL = 0x0D11;
	public int GL_INDEX_SHIFT = 0x0D12;
	public int GL_INDEX_OFFSET = 0x0D13;
	public int GL_RED_SCALE = 0x0D14;
	public int GL_RED_BIAS = 0x0D15;
	public int GL_ZOOM_X = 0x0D16;
	public int GL_ZOOM_Y = 0x0D17;
	public int GL_GREEN_SCALE = 0x0D18;
	public int GL_GREEN_BIAS = 0x0D19;
	public int GL_BLUE_SCALE = 0x0D1A;
	public int GL_BLUE_BIAS = 0x0D1B;
	public int GL_ALPHA_SCALE = 0x0D1C;
	public int GL_ALPHA_BIAS = 0x0D1D;
	public int GL_DEPTH_SCALE = 0x0D1E;
	public int GL_DEPTH_BIAS = 0x0D1F;
	public int GL_MAX_EVAL_ORDER = 0x0D30;
	public int GL_MAX_LIGHTS = 0x0D31;
	public int GL_MAX_CLIP_PLANES = 0x0D32;
	public int GL_MAX_TEXTURE_SIZE = 0x0D33;
	public int GL_MAX_PIXEL_MAP_TABLE = 0x0D34;
	public int GL_MAX_ATTRIB_STACK_DEPTH = 0x0D35;
	public int GL_MAX_MODELVIEW_STACK_DEPTH = 0x0D36;
	public int GL_MAX_NAME_STACK_DEPTH = 0x0D37;
	public int GL_MAX_PROJECTION_STACK_DEPTH = 0x0D38;
	public int GL_MAX_TEXTURE_STACK_DEPTH = 0x0D39;
	public int GL_MAX_VIEWPORT_DIMS = 0x0D3A;
	public int GL_MAX_CLIENT_ATTRIB_STACK_DEPTH = 0x0D3B;
	public int GL_SUBPIXEL_BITS = 0x0D50;
	public int GL_INDEX_BITS = 0x0D51;
	public int GL_RED_BITS = 0x0D52;
	public int GL_GREEN_BITS = 0x0D53;
	public int GL_BLUE_BITS = 0x0D54;
	public int GL_ALPHA_BITS = 0x0D55;
	public int GL_DEPTH_BITS = 0x0D56;
	public int GL_STENCIL_BITS = 0x0D57;
	public int GL_ACCUM_RED_BITS = 0x0D58;
	public int GL_ACCUM_GREEN_BITS = 0x0D59;
	public int GL_ACCUM_BLUE_BITS = 0x0D5A;
	public int GL_ACCUM_ALPHA_BITS = 0x0D5B;
	public int GL_NAME_STACK_DEPTH = 0x0D70;
	public int GL_AUTO_NORMAL = 0x0D80;
	public int GL_MAP1_COLOR_4 = 0x0D90;
	public int GL_MAP1_INDEX = 0x0D91;
	public int GL_MAP1_NORMAL = 0x0D92;
	public int GL_MAP1_TEXTURE_COORD_1 = 0x0D93;
	public int GL_MAP1_TEXTURE_COORD_2 = 0x0D94;
	public int GL_MAP1_TEXTURE_COORD_3 = 0x0D95;
	public int GL_MAP1_TEXTURE_COORD_4 = 0x0D96;
	public int GL_MAP1_VERTEX_3 = 0x0D97;
	public int GL_MAP1_VERTEX_4 = 0x0D98;
	public int GL_MAP2_COLOR_4 = 0x0DB0;
	public int GL_MAP2_INDEX = 0x0DB1;
	public int GL_MAP2_NORMAL = 0x0DB2;
	public int GL_MAP2_TEXTURE_COORD_1 = 0x0DB3;
	public int GL_MAP2_TEXTURE_COORD_2 = 0x0DB4;
	public int GL_MAP2_TEXTURE_COORD_3 = 0x0DB5;
	public int GL_MAP2_TEXTURE_COORD_4 = 0x0DB6;
	public int GL_MAP2_VERTEX_3 = 0x0DB7;
	public int GL_MAP2_VERTEX_4 = 0x0DB8;
	public int GL_MAP1_GRID_DOMAIN = 0x0DD0;
	public int GL_MAP1_GRID_SEGMENTS = 0x0DD1;
	public int GL_MAP2_GRID_DOMAIN = 0x0DD2;
	public int GL_MAP2_GRID_SEGMENTS = 0x0DD3;
	public int GL_TEXTURE_1D = 0x0DE0;
	public int GL_TEXTURE_2D = 0x0DE1;
	public int GL_FEEDBACK_BUFFER_POINTER = 0x0DF0;
	public int GL_FEEDBACK_BUFFER_SIZE = 0x0DF1;
	public int GL_FEEDBACK_BUFFER_TYPE = 0x0DF2;
	public int GL_SELECTION_BUFFER_POINTER = 0x0DF3;
	public int GL_SELECTION_BUFFER_SIZE = 0x0DF4;
	
	public void pushMatrix() {
		GlStateManager.pushMatrix();
	}
	
	public void popMatrix() {
		GlStateManager.popMatrix();
	}
	
	public void lineWidth(float width) {
		GL11.glLineWidth(width);
	}
	
	public void init() {
		GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();
        GlStateManager.disableCull();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableDepthTest();
		GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
	}
	
	public void enable(int en) {
		GL11.glEnable(en);
	}
	
	public void begin(int en) {
		GL11.glBegin(en);
	}
	
	public void glEnd() {
		GL11.glEnd();
	}
	
	public void finish() {
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.enableDepthTest();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableCull();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
        color(ColorBase.WHITE);
	}
	public void blendFunc(int a, int b) {
		GlStateManager.blendFunc(a, b);
	}
	
	public void start2D() {
		init();
	}
	
	public void end2D() {
		finish();
	}
	
	public void disable(int en) {
		GL11.glDisable(en);
	}
	
	public void vertex2d(float x, float y) {
		GL11.glVertex2d(x, y);
	}
	
	public void vertex3d(float x, float y, float z) {
		GL11.glVertex3d(x, y, z);
	}
	
	public void scale(float x, float y, float z) {
		GL11.glScaled(x, y, z);
	}
	
	public void translate(float x, float y, float z) {
		GL11.glTranslated(x, y, z);
	}
	
	public void rotate(float angle, float x, float y, float z) {
		GL11.glRotated(angle, x, y, z);
	}
	
	public void color(ColorBase color) {
		GL11.glColor4d(color.getRed() / 255D, color.getGreen() / 255D, color.getBlue() / 255D, color.getAlpha() / 255D);	
	}
	
	public VectorBase pos() {
		return mc.getRenderManager().info.getProjectedView();
	}
}