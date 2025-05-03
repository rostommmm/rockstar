package fun.rockstarity.api.helpers.game.model;

import java.util.List;

import com.google.gson.annotations.Expose;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

@UtilityClass
public class ModelFix {
	
	private static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
	
    @Expose
    public final double quad_indent = 0.0001;
	
	@Expose
	public final double quad_expansion = 0.002;
	
    public double getRecess() {
        return quad_indent;
    }
	
	public double getExpansion() {
		return quad_expansion;
	}
	
	public static float getShrinkRatio(AtlasTexture atlas, float defaultValue, float returnValue) {
        if(atlas.getTextureLocation().equals(BLOCK_ATLAS) && defaultValue == returnValue) {
            return 0.0f;
        }
		
		return -1;
	}
	
	public void enlargeFaces(List<BlockPart> cir) {
		float inc = (float) ModelFix.getRecess();
		float inc2 = (float) ModelFix.getExpansion();
		
		for (var e : cir) {
			Vector3f from = e.positionFrom;
			Vector3f to = e.positionTo;
			var set = e.mapFaces.keySet();
			
			if (set.size() == 1) {
				var dir = set.stream().findAny().get();
				
				switch (dir) {
					case UP -> {
                        from.set(from.x - inc2, from.y - inc, from.z - inc2);
                        to.set(to.x + inc2, to.y - inc, to.z + inc2);
					}
					
                    case DOWN -> {
                        from.set(from.x - inc2, from.y + inc, from.z - inc2);
                        to.set(to.x + inc2, to.y + inc, to.z + inc2);
                    }
                    
                    case WEST -> {
                        from.set(from.x - inc, from.y + inc2, from.z - inc2);
                        to.set(to.x - inc, to.y - inc2, to.z + inc2);
                    }
                    
                    case EAST -> {
                        from.set(from.x + inc, from.y + inc2, from.z - inc2);
                        to.set(to.x + inc, to.y - inc2, to.z + inc2);
                    }
                    
                    default -> {
                    	
                    }
				}
			}
		}
	}
	
	public void createOrExpandSpan(List<ItemModelGenerator.Span> listSpans, ItemModelGenerator.SpanFacing spanFacing, int pixelX, int pixelY) {
		int length;
		 ItemModelGenerator.Span existingSpan = null;
		 
		 for (ItemModelGenerator.Span span2 : listSpans) {
			 if (span2.getFacing() == spanFacing) {
				 int i = spanFacing.isHorizontal() ? pixelY : pixelX;
				 if (span2.getAnchor() != i) continue;
				 if (getExpansion() != 0 && span2.getMax() != (!spanFacing.isHorizontal() ? pixelY : pixelX) - 1)
					 continue;
				 
				 existingSpan = span2;
				 break;
			 }
		 }
		 
		 length = spanFacing.isHorizontal() ? pixelX : pixelY;
		 
		 if (existingSpan == null) {
			 int newStart = spanFacing.isHorizontal() ? pixelY : pixelX;
			 listSpans.add(new ItemModelGenerator.Span(spanFacing, length, newStart));
		 } else {
			 existingSpan.expand(length);
		 }
	}
}
