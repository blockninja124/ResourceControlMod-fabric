package com.hagan.resourcecontrol.util;

import java.io.File;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;

public class ResourceUtils {

	public static void reloadSingleTexture(String resourcePath) {

		Minecraft mc = Minecraft.getInstance();


		File textureFile = null;

		ResourceLocation rl = new ResourceLocation(resourcePath);

		TextureManager tm = mc.getTextureManager();


		/*try {
	    	textureFile = new File(rl.getPath());
	    } catch (Exception ex) {

	    }

	    if (textureFile != null && textureFile.exists()) {
	        ResourceLocation MODEL_TEXTURE = Resources.OTHER_TESTMODEL_CUSTOM;

	        TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
	        texturemanager.deleteTexture(MODEL_TEXTURE);
	        Object object = new ThreadDownloadImageData(textureFile, null, MODEL_TEXTURE, new ImageBufferDownload());
	        texturemanager.loadTexture(MODEL_TEXTURE, (ITextureObject)object);

	        return;
	    } else {
	        return;
	    }*/

		//        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

		//        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

		//        ModelManager modelManager = Minecraft.getInstance().getModelManager();

		//        ResourceLocation resourceLocation = new ResourceLocation(resourcePath);

		//        System.out.println(resourceManager.getResource(resourceLocation));

		//        System.out.println("Resource location:");
		//        System.out.println(resourceLocation);

		//        AbstractTexture texture = textureManager.getTexture(resourceLocation);
		//        if (texture == null) {
		//        	return;
		//        }

		//        textureManager.release(resourceLocation);

		//SimpleTexture simpleTexture = (SimpleTexture) texture;

		//        SimpleTexture newTexture = new SimpleTexture(resourceLocation);
		//        textureManager.register(resourceLocation, newTexture);

		// Invalidate the existing texture (if already loaded)
		//textureManager.release(resourceLocation);

		// Rebind the texture, forcing it to be loaded
		//textureManager.bindForSetup(resourceLocation);

		//System.out.println(textureManager.getTexture(resourceLocation));
		//        ResourceLocation modelLocation = new ResourceLocation("minecraft:models/block/oak_leaves1.json");

		//        BakedModel bakedModel = modelManager.getModel(modelLocation);


		//        if (bakedModel != null) {
		//            System.out.println("Re-baking the model: " + modelLocation);

		// Trigger a full reload of the model manager to refresh baked models
		//            modelManager.onResourceManagerReload(resourceManager);

		//            System.out.println("Model reloaded and baked: " + modelLocation);
		//        } else {
		//            System.out.println("Model not found: " + modelLocation);
		//        }
	}


	/**
	 * Reloads all of the resources on the client its run. Basically forcing an F3+T
	 */
	public static void reloadAll() {
		Minecraft mc = Minecraft.getInstance();
		mc.reloadResourcePacks();

		// This just prevents another reload when using the reload menu after this command has already done it
		PackRepository rm = mc.getResourcePackRepository();
		mc.options.resourcePacks = new ArrayList<>(rm.getSelectedIds());
		mc.options.save();
	}

}