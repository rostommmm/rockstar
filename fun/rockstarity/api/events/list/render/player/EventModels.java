package fun.rockstarity.api.events.list.render.player;

import fun.rockstarity.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

/**
 * @author ConeTin
 * @since 4 авг. 2024 г.
 */

@AllArgsConstructor
public class EventModels extends Event {
	
	@Getter
	private LivingEntity owner;
	public ModelRenderer bipedHead;
    public ModelRenderer bipedHeadwear;
    public ModelRenderer bipedBody;
    public ModelRenderer bipedRightArm;
    public ModelRenderer bipedLeftArm;
    public ModelRenderer bipedRightLeg;
    public ModelRenderer bipedLeftLeg;
    public float limbSwingAmount;
	
    public <T extends Event> EventModels hook() {
		return (EventModels) super.hook();
	}
    
}
