package fun.rockstarity.api.render.ui.draggables.grid;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;

import fun.rockstarity.api.IAccess;
import fun.rockstarity.api.helpers.game.Chat;
import fun.rockstarity.api.helpers.math.TimerUtility;
import fun.rockstarity.api.render.ui.draggables.Draggable;
import fun.rockstarity.api.render.ui.draggables.grid.line.GridLine;
import fun.rockstarity.api.render.ui.draggables.grid.line.GridRotationType;

/**
 * @author ConeTin
 * @since 10 Р°РІРі. 2024вЂЇРі.
 */

public class Grid implements IAccess {
	
	private final ArrayList<GridLine> lines = new ArrayList<>();
	private TimerUtility updateTimer = new TimerUtility();
	private GridLine activeVerticalLine, activeHorizontalLine;
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.activeHorizontalLine = null;
		this.activeVerticalLine = null;
		
		float closestXDistance = Float.MAX_VALUE;
		float closestYDistance = Float.MAX_VALUE;

		for (GridLine line : this.lines) {
			boolean hovered = false;
			for (Draggable drag : rock.getDraggableHandler().getDraggables()) {
				if (!drag.isCanDrag() || !drag.isDragging()) continue;
				
				float[] data = this.calcDist(drag, line);
				float dist = data[0]; // Р”РёСЃС‚Р°РЅС†РёСЏ РѕС‚ Р»РёРЅРёРё РґРѕ РґСЂР°РіР°
				
				if (line.getRotationType() == GridRotationType.HORIZONTAL) {
    				if (dist < 15) hovered  = true; // Р СЏРґРѕРј  СЃ Р»РёРЅРёРµР№, РЅРѕ РЅРµ РІС‹СЂР°РІРЅРёРІР°РµС‚СЃСЏ РїРѕ РЅРµР№
    				if (dist < 5 && dist < closestYDistance) { // Р СЏРґРѕРј  СЃ Р»РёРЅРёРµР№ Рё РІС‹СЂР°РІРЅРёРІР°РµС‚СЃСЏ
    					closestYDistance = dist;
    					this.activeHorizontalLine = line;
    				}
    			} else {
    				if (dist < 15) hovered  = true; // Р СЏРґРѕРј  СЃ Р»РёРЅРёРµР№, РЅРѕ РЅРµ РІС‹СЂР°РІРЅРёРІР°РµС‚СЃСЏ РїРѕ РЅРµР№
    				if (dist < 5 && dist < closestXDistance) { // Р СЏРґРѕРј  СЃ Р»РёРЅРёРµР№ Рё РІС‹СЂР°РІРЅРёРІР°РµС‚СЃСЏ
    					closestXDistance = dist;
    					this.activeVerticalLine = line;
    				}
    			}
			}
			line.getHoveredAnim().setForward(hovered);
        }
		
		if (!this.updateTimer.passed(300)) return;
		
		for (GridLine line : this.lines) {
			line.getActiveAnim().setForward(this.activeHorizontalLine == line || this.activeVerticalLine == line);
            line.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
	
	public void handleDragPositions() {
		for (Draggable drag : rock.getDraggableHandler().getDraggables()) {
			if (!drag.isCanDrag() || !drag.isDragging()) continue;
			
			if (this.activeHorizontalLine !=  null) {
				float[] data = this.calcDist(drag, this.activeHorizontalLine);
				float distY = data[0];
				if (distY < 5) {
					drag.setY(this.activeHorizontalLine.getCoord() - data[1]);
				}
			}
			
			if (this.activeVerticalLine != null) {
				float[] data = this.calcDist(drag, this.activeVerticalLine);
				float distX = data[0];
				if (distX < 5) {
					drag.setX(this.activeVerticalLine.getCoord() - data[1]);
				}
			}
		}
	}
	
	private float[] calcDist(Draggable drag, GridLine line) {
	    boolean horizontal = line.getRotationType() == GridRotationType.HORIZONTAL;
	    float coord = horizontal ? drag.getY() : drag.getX();
	    float size = horizontal ? drag.getHeight() : drag.getWidth();
	    float modif = 0;
	    
	    float dist = Math.abs(coord - line.getCoord()); // Р”РёСЃС‚Р°РЅС†РёСЏ РѕС‚ Р»РёРЅРёРё РґРѕ РґСЂР°РіР°
	    float distHalfSize = Math.abs(coord + (size / 2) - line.getCoord()); // Р”РёСЃС‚Р°РЅС†РёСЏ РѕС‚ Р»РёРЅРёРё РґРѕ РїРѕР»РѕРІРёРЅС‹ РґСЂР°РіР°
	    float distSize = Math.abs(coord + size - line.getCoord()); // Р”РёСЃС‚Р°РЅС†РёСЏ РѕС‚ Р»РёРЅРёРё РґРѕ РґСЂР°РіР°
	    
	    float minDist = Math.min(dist, Math.min(distHalfSize, distSize));

	    if (minDist == distHalfSize) {
    	    modif = size/2;
	    } else if (minDist == distSize) {
    	    modif = size;
        }
	    
	    return new float[] {
	    		minDist,
	    		modif
	    };
	  }
	
	public void addHorizontalLine(float y) {
        this.lines.add(new GridLine(y, GridRotationType.HORIZONTAL));
    }
	
	public void addVerticalLine(float x) {
        this.lines.add(new GridLine(x, GridRotationType.VERTICAL));
    }
	
	public void updateLineList() {
	    this.updateTimer.reset();
	    this.lines.clear();
	    
	    // РџРµСЂРµРєСЂРµСЃС‚СЊРµ
	    this.addHorizontalLine(sr.getScaledHeight() / 2 - 0.5f);
	    this.addVerticalLine(sr.getScaledWidth() / 2 - 0.5f);
	    
	    // Р›РёРЅРёРё РїРѕ РєСЂР°СЏРј СЌРєСЂР°РЅР°
	    this.addHorizontalLine(6);
	    this.addHorizontalLine(sr.getScaledHeight() - 7);
	    this.addVerticalLine(6);
	    this.addVerticalLine(sr.getScaledWidth() - 7);
	    
	    // Р›РёРЅРёРё РЅР° Р°РґРЅР° С‡РµС‚РІРµСЂС‚Р°СЏ
	    this.addHorizontalLine(sr.getScaledHeight() / 4 - 0.5f);
	    this.addHorizontalLine(3 * sr.getScaledHeight() / 4 - 0.5f);
	    this.addVerticalLine(sr.getScaledWidth() / 4 - 0.5f);
	    this.addVerticalLine(3 * sr.getScaledWidth() / 4 - 0.5f);
	}
	
}
