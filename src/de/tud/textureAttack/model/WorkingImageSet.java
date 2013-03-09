package de.tud.textureAttack.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.tud.textureAttack.controller.ActionController;
import de.tud.textureAttack.model.algorithms.Options;
import de.tud.textureAttack.model.algorithms.attacks.AbstractAttackAlgorithm;
import de.tud.textureAttack.model.algorithms.selection.AbstractSelectionAlgorithm;
import de.tud.textureAttack.model.io.InvalidTextureException;

public class WorkingImageSet {

	private ArrayList<AdvancedTextureImage> imageList;
	private ActionController actionController;

	public WorkingImageSet(ActionController actionController) {
		imageList = new ArrayList<AdvancedTextureImage>();
		this.actionController = actionController;
	}

	public ArrayList<BufferedImage> getOriginialImages() {
		ArrayList<BufferedImage> list = new ArrayList<BufferedImage>();
		for (AdvancedTextureImage img : imageList) {
			list.add(img.getOriginalBufferedImage());
		}
		return list;
	}

	public void clearImages() {
		imageList.clear();
	}

	public void addAdvancedImageTexture(AdvancedTextureImage img) {
		imageList.add(img);
	}

	public void removeAdvancedTextureImage(int i) {
		imageList.remove(i);
	}

	/**
	 * Gets the absoluteFilePaths of loaded images, creates from them the
	 * AdvancedTextureImages and stores it in the working set
	 * 
	 * @param files
	 * @param baseDir Base Directory of the files, for saving later with dir structure
	 */
	public void setTextures(File[] files, String baseDir) {
		if (files != null) {
			imageList.clear();
			for (int i = 0; i < files.length; i++) {
				try {
					AdvancedTextureImage img = new AdvancedTextureImage(
							files[i].getAbsolutePath(), baseDir);
					imageList.add(img);
				} catch (InvalidTextureException | IOException e) {
					System.out.println("ERROR:        " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public ArrayList<BufferedImage> getEditedImages() {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for (AdvancedTextureImage img : imageList)
			images.add(img.getEditedBufferedImage());
		return images;
	}

	public BufferedImage getEditedImage(int index) {
		return imageList.get(index).getEditedBufferedImage();
	}

	/**
	 * Returns the original Image of the specified index (loads the image,
	 * before) if it exists, else null
	 * 
	 * @param selectedImagePosition
	 * @return BufferedImage
	 */
	public BufferedImage getOriginialImage(int selectedImagePosition) {
		if (imageList.isEmpty() || (selectedImagePosition > imageList.size()))
			return null;
		else {
			if (imageList.get(selectedImagePosition).getEditedBufferedImage() == null)
				imageList.get(selectedImagePosition).loadImage();
			return imageList.get(selectedImagePosition)
					.getOriginalBufferedImage();
		}
	}

	/**
	 * Returns the AdvancedTextureImage not loaded with the given Index i if it
	 * is in the set, else null
	 * 
	 * @param i
	 * @return AdvancedTextureImage
	 */
	public AdvancedTextureImage getAdvancedTextureImage(int i) {
		if (imageList.isEmpty() || (i > imageList.size()))
			return null;
		else {
			return imageList.get(i);
		}
	}

	/**
	 * Returns the AdvancedTextureImage loaded (load image data before, if not
	 * loaded) with the given Index i if it is in the set, else null
	 * 
	 * @param i
	 * @return AdvancedTextureImage
	 */
	public AdvancedTextureImage getLoadedAdvancedTextureImage(int i) {
		if (imageList.isEmpty() || (i > imageList.size()))
			return null;
		else {
			if (imageList.get(i).getEditedBufferedImage() == null) {
				imageList.get(i).loadImage();
			}
			return imageList.get(i);
		}
	}

	public ArrayList<AdvancedTextureImage> getAdvancedTextureImages() {
		return imageList;
	}

	/**
	 * Saves all Textures of the set with their folder structure to the
	 * specified absolutePath and deletes their tmp image files (created during
	 * manipulation)
	 * 
	 * @param absolutePath
	 */
	public void saveTextures(String absolutePath) {
		for (AdvancedTextureImage img : imageList) {
			img.writeTextureToPath(absolutePath);
			// tmpSavePath: delete tmp image
			File file = new File(img.getTmpSavePath());
			if (file.exists())
				file.delete();
		}
	}

	/**
	 * Returns the advances texture image, which matches the given
	 * absoluteFilePath (the path from the source texture), or null if there is
	 * no such a advanced texture image
	 * 
	 * @param path
	 * @return AdvancedTextureImage
	 */
	public AdvancedTextureImage getAdvancedTextureImageFromAbsoluteFilePath(
			String path) {
		for (int i = 0; i < imageList.size(); i++) {
			if (imageList.get(i).getAbsolutePath().equals(path))
				return imageList.get(i);
		}
		return null;
	}

	/**
	 * Manipulated all images in the set, with the given background selection
	 * algorithm, pixel manipulation algorithm with the given options
	 * 
	 * @param attackAlgorithm
	 * @param selectAlgorithm
	 * @param options
	 */
	public void manipulateAllImages(AbstractAttackAlgorithm attackAlgorithm,
			AbstractSelectionAlgorithm selectAlgorithm, Options options) {
		for (AdvancedTextureImage advImage : imageList) {
			advImage.processManipulation(attackAlgorithm, selectAlgorithm,
					options);
			actionController.setTextureIcon(advImage);
		}

	}

}