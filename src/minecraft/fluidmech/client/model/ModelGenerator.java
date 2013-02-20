// Date: 12/23/2012 8:44:55 PM
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package fluidmech.client.model;

import fluidmech.common.tileentity.TileEntityGenerator;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.tileentity.TileEntity;

public class ModelGenerator extends ModelBase
{
	// fields
	ModelRenderer Bottom;
	ModelRenderer Left;
	ModelRenderer CenterRod;
	ModelRenderer Right;
	ModelRenderer RightTopCorner;
	ModelRenderer LeftTopCorner;
	ModelRenderer LeftBottomCorner;
	ModelRenderer RightBottomCorner;
	ModelRenderer BottomCase;
	ModelRenderer TopCase;
	ModelRenderer LeftBrace;
	ModelRenderer RightBrace;
	ModelRenderer BackBracer;
	ModelRenderer FrontBracer;
	ModelRenderer FrontDisc;
	ModelRenderer FrontDisc2;
	ModelRenderer BackDisc;
	ModelRenderer BackDisc2;

	public ModelGenerator()
	{
		textureWidth = 128;
		textureHeight = 128;

		Bottom = new ModelRenderer(this, 0, 74);
		Bottom.addBox(-7F, -1F, -7F, 14, 2, 14);
		Bottom.setRotationPoint(0F, 23F, 0F);
		Bottom.setTextureSize(128, 128);
		Bottom.mirror = true;
		setRotation(Bottom, 0F, 0F, 0F);
		Left = new ModelRenderer(this, 17, 49);
		Left.addBox(-1F, -4F, -3F, 2, 8, 6);
		Left.setRotationPoint(7F, 15F, 0F);
		Left.setTextureSize(128, 128);
		Left.mirror = true;
		setRotation(Left, 0F, 0F, 0F);
		CenterRod = new ModelRenderer(this, 62, 0);
		CenterRod.addBox(-1.5F, -1.5F, -8F, 3, 3, 16);
		CenterRod.setRotationPoint(0F, 15F, 0F);
		CenterRod.setTextureSize(128, 128);
		CenterRod.mirror = true;
		setRotation(CenterRod, 0F, 0F, 0F);
		Right = new ModelRenderer(this, 0, 49);
		Right.addBox(-1F, -4F, -3F, 2, 8, 6);
		Right.setRotationPoint(-7F, 15F, 0F);
		Right.setTextureSize(128, 128);
		Right.mirror = true;
		setRotation(Right, 0F, 0F, 0F);
		RightTopCorner = new ModelRenderer(this, 0, 35);
		RightTopCorner.addBox(-2F, -6F, -3F, 2, 6, 6);
		RightTopCorner.setRotationPoint(-7F, 13F, 0F);
		RightTopCorner.setTextureSize(128, 128);
		RightTopCorner.mirror = true;
		setRotation(RightTopCorner, 0F, 0F, 1.047198F);
		LeftTopCorner = new ModelRenderer(this, 17, 35);
		LeftTopCorner.addBox(0F, -6F, -3F, 2, 6, 6);
		LeftTopCorner.setRotationPoint(7F, 13F, 0F);
		LeftTopCorner.setTextureSize(128, 128);
		LeftTopCorner.mirror = true;
		setRotation(LeftTopCorner, 0F, 0F, -1.047198F);
		LeftBottomCorner = new ModelRenderer(this, 17, 91);
		LeftBottomCorner.addBox(0F, 0F, -3F, 2, 6, 6);
		LeftBottomCorner.setRotationPoint(7F, 17F, 0F);
		LeftBottomCorner.setTextureSize(128, 128);
		LeftBottomCorner.mirror = true;
		setRotation(LeftBottomCorner, 0F, 0F, 1.047198F);
		RightBottomCorner = new ModelRenderer(this, 0, 91);
		RightBottomCorner.addBox(-2F, 0F, -3F, 2, 6, 6);
		RightBottomCorner.setRotationPoint(-7F, 17F, 0F);
		RightBottomCorner.setTextureSize(128, 128);
		RightBottomCorner.mirror = true;
		setRotation(RightBottomCorner, 0F, 0F, -1.047198F);
		BottomCase = new ModelRenderer(this, 3, 64);
		BottomCase.addBox(0F, 0F, -3F, 6, 2, 6);
		BottomCase.setRotationPoint(-3F, 20F, 0F);
		BottomCase.setTextureSize(128, 128);
		BottomCase.mirror = true;
		setRotation(BottomCase, 0F, 0F, 0F);
		TopCase = new ModelRenderer(this, 3, 26);
		TopCase.addBox(0F, 0F, -3F, 6, 2, 6);
		TopCase.setRotationPoint(-3F, 8F, 0F);
		TopCase.setTextureSize(128, 128);
		TopCase.mirror = true;
		setRotation(TopCase, 0F, 0F, 0F);
		LeftBrace = new ModelRenderer(this, 44, 64);
		LeftBrace.addBox(0F, 0F, -1.5F, 3, 6, 3);
		LeftBrace.setRotationPoint(3F, 17F, 0F);
		LeftBrace.setTextureSize(128, 128);
		LeftBrace.mirror = true;
		setRotation(LeftBrace, 0F, 0F, 0F);
		RightBrace = new ModelRenderer(this, 31, 64);
		RightBrace.addBox(0F, 0F, -1.5F, 3, 6, 3);
		RightBrace.setRotationPoint(-6F, 17F, 0F);
		RightBrace.setTextureSize(128, 128);
		RightBrace.mirror = true;
		setRotation(RightBrace, 0F, 0F, 0F);
		BackBracer = new ModelRenderer(this, 50, 0);
		BackBracer.addBox(-2F, -3F, 5F, 4, 10, 1);
		BackBracer.setRotationPoint(0F, 15F, 0F);
		BackBracer.setTextureSize(128, 128);
		BackBracer.mirror = true;
		setRotation(BackBracer, 0F, 0F, 0F);
		FrontBracer = new ModelRenderer(this, 50, 0);
		FrontBracer.addBox(-2F, -3F, -6F, 4, 10, 1);
		FrontBracer.setRotationPoint(0F, 15F, 0F);
		FrontBracer.setTextureSize(128, 128);
		FrontBracer.mirror = true;
		setRotation(FrontBracer, 0F, 0F, 0F);
		FrontDisc = new ModelRenderer(this, 65, 25);
		FrontDisc.addBox(-5F, -5F, -5F, 10, 10, 2);
		FrontDisc.setRotationPoint(0F, 15F, 0F);
		FrontDisc.setTextureSize(128, 128);
		FrontDisc.mirror = true;
		setRotation(FrontDisc, 0F, 0F, 0.7853982F);
		FrontDisc2 = new ModelRenderer(this, 65, 25);
		FrontDisc2.addBox(-5F, -5F, -5F, 10, 10, 2);
		FrontDisc2.setRotationPoint(0F, 15F, 0F);
		FrontDisc2.setTextureSize(128, 128);
		FrontDisc2.mirror = true;
		setRotation(FrontDisc2, 0F, 0F, 0F);
		BackDisc = new ModelRenderer(this, 65, 25);
		BackDisc.addBox(-5F, -5F, 3F, 10, 10, 2);
		BackDisc.setRotationPoint(0F, 15F, 0F);
		BackDisc.setTextureSize(128, 128);
		BackDisc.mirror = true;
		setRotation(BackDisc, 0F, 0F, 0.7853982F);
		BackDisc2 = new ModelRenderer(this, 65, 25);
		BackDisc2.addBox(-5F, -5F, 3F, 10, 10, 2);
		BackDisc2.setRotationPoint(0F, 15F, 0F);
		BackDisc2.setTextureSize(128, 128);
		BackDisc2.mirror = true;
		setRotation(BackDisc2, 0F, 0F, 0F);
	}

	public void render(TileEntity ent)
	{
		float f5 = 0.0625F;
		// noMoving renderParts
		Bottom.render(f5);
		Left.render(f5);
		CenterRod.render(f5);
		Right.render(f5);
		RightTopCorner.render(f5);
		LeftTopCorner.render(f5);
		LeftBottomCorner.render(f5);
		RightBottomCorner.render(f5);
		BottomCase.render(f5);
		TopCase.render(f5);
		LeftBrace.render(f5);
		RightBrace.render(f5);
		BackBracer.render(f5);
		FrontBracer.render(f5);
		// Moving parts
		float pos = 0;
		if (ent instanceof TileEntityGenerator)
			pos = 45 * ((TileEntityGenerator) ent).getAnimationPos();

		// change
		FrontDisc.rotateAngleZ = (float) Math.toRadians(pos);
		FrontDisc2.rotateAngleZ = (float) Math.toRadians(pos + 45);
		BackDisc.rotateAngleZ = (float) Math.toRadians(pos);
		BackDisc2.rotateAngleZ = (float) Math.toRadians(pos + 45);

		FrontDisc.render(f5);
		FrontDisc2.render(f5);
		BackDisc.render(f5);
		BackDisc2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
