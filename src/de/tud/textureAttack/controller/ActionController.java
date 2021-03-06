/*******************************************************************************
 * Copyright (c) 2012 Sebastian Funke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Sebastian Funke - initial API and implementation
 ******************************************************************************/
package de.tud.textureAttack.controller;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.tud.textureAttack.model.AdvancedTextureImage;
import de.tud.textureAttack.model.AdvancedTextureImage.EditState;
import de.tud.textureAttack.model.WorkingImageSet;
import de.tud.textureAttack.model.actionHandler.ToolsMenuActionHandler;
import de.tud.textureAttack.model.algorithms.AbstractAlgorithm;
import de.tud.textureAttack.model.algorithms.AbstractAlgorithmFactory;
import de.tud.textureAttack.model.algorithms.Options;
import de.tud.textureAttack.model.algorithms.Options.AttackToolEnum;
import de.tud.textureAttack.model.algorithms.Options.OptionIdentifierEnum;
import de.tud.textureAttack.model.algorithms.Options.SelectionToolEnum;
import de.tud.textureAttack.model.algorithms.attacks.AbstractAttackAlgorithm;
import de.tud.textureAttack.model.algorithms.attacks.AttackAlgorithms;
import de.tud.textureAttack.model.algorithms.selection.AbstractSelectionAlgorithm;
import de.tud.textureAttack.model.algorithms.selection.SelectionAlgorithms;
import de.tud.textureAttack.model.io.IOUtils;
import de.tud.textureAttack.model.utils.PropertiesException;
import de.tud.textureAttack.model.utils.PropertyUtils;
import de.tud.textureAttack.view.MainWindow;
import de.tud.textureAttack.view.components.StatusBar;
import de.tud.textureAttack.view.dialogs.TexturesDialog;

public class ActionController {

	private MainWindow mainWindow;

	private WorkingImageSet workingImageSet;

	private AbstractAlgorithmFactory attackAlgoFactory;
	private AbstractAlgorithmFactory selectionAlgoFactory;

	private Options options;

	/**
	 * Initializes all algorithm factorys, image working set, options, loads
	 * properties from property file and starts the GUI
	 */
	public ActionController() {

		try {
			PropertyUtils.loadProperties();
		} catch (PropertiesException e) {
			System.out.println(e.getMessage());
		}
		// initialize needed classes and algorithms
		workingImageSet = new WorkingImageSet(this);

		// init Algos
		attackAlgoFactory = new AttackAlgorithms(this);
		selectionAlgoFactory = new SelectionAlgorithms(this);

		options = new Options();

		// start GUI
		mainWindow = new MainWindow(this);
	}

	public void setOption(Options.OptionIdentifierEnum id, Object value) {
		options.setOption(id, value);
	}

	public void saveTextures() {
		TexturesDialog textureSaveDialog = new TexturesDialog(
				TexturesDialog.SAVE_DIALOG);
		if (textureSaveDialog.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
			workingImageSet.saveTextures(textureSaveDialog.getSelectedFile()
					.getAbsolutePath());
		}
	}

	public void loadTextures() {
		TexturesDialog textureChooser = new TexturesDialog(
				TexturesDialog.OPEN_DIALOG);
		if (textureChooser.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION) {
			PropertyUtils.properties.setProperty("lastDir", textureChooser
					.getCurrentDirectory().getAbsolutePath());
			try {
				PropertyUtils.properties.store(new FileOutputStream(
						PropertyUtils.PROPERTIES_LOCATION), null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			File[] loadedFiles = IOUtils.getAllTextureFiles(textureChooser
					.getSelectedFiles());
			if (loadedFiles.length > 3000) {
				JOptionPane.showMessageDialog(null,
						"To much textures to load, not enough memory! (Not more than 3000 textures at once...)");
			} else {
				String baseDir = IOUtils.getBaseDir(textureChooser
						.getSelectedFiles());

				workingImageSet.setTextures(loadedFiles, baseDir);
				mainWindow.loadTextureImages(workingImageSet
						.getAdvancedTextureImages());

			}

		}

	}

	public void loadTexturesInPreviewList(String filter, boolean complement) {

		ArrayList<AdvancedTextureImage> filteredImageList = new ArrayList<AdvancedTextureImage>();
		ArrayList<AdvancedTextureImage> originalImageList = workingImageSet
				.getAdvancedTextureImages();

		EditState filterState = null;
		if (filter.equals(ToolsMenuActionHandler.FILTER_FINISHED))
			filterState = EditState.finished;
		if (filter.equals(ToolsMenuActionHandler.FILTER_SUPPORTED))
			filterState = EditState.unsupported;

		for (AdvancedTextureImage img : originalImageList) {
			if (complement) {
				if (img.getState() != filterState)
					filteredImageList.add(img);

			} else {
				if (img.getState() == filterState)
					filteredImageList.add(img);

			}

		}

		mainWindow.loadTextureImages(filteredImageList);
	}

	public void setStatus(String newStatus) {
		mainWindow.setStatus(newStatus);
	}

	public void setProgress(int i) {
		mainWindow.setProgress(i);
	}

	public void setProgressCount(int i) {
		mainWindow.setProgressCount(i);
	}

	public AdvancedTextureImage getSelectedAdvancedTextureImage() {
		return workingImageSet.getAdvancedTextureImage(mainWindow
				.getSelectedImagePosition());
	}

	public ArrayList<AdvancedTextureImage> getSelectedAdvancedTextureImages() {
		return workingImageSet.getAdvancedTextureImages();
	}

	public Component getMainWindow() {
		return mainWindow;
	}

	/**
	 * Sets the PreviewImage in the Preview Panel in the center of the
	 * application
	 * 
	 * @param img
	 */
	public void setPreviewImage(AdvancedTextureImage img) {
		mainWindow.getContentContainer().getImagePreviewScrollPane()
				.setImage(img);
	}

	/**
	 * To fire menu actions from other packages
	 * 
	 * @param action
	 */
	public void fireAction(String action) {
		mainWindow.getMenu().fireAction(ToolsMenuActionHandler.FILTER_FINISHED,
				this, this.hashCode(), ToolsMenuActionHandler.FILTER_FINISHED);
	}

	/**
	 * Updates the PreviewImage in the center panel
	 * 
	 * @param updatedImage
	 */
	public void update(AdvancedTextureImage updatedImage) {
		setPreviewImage(updatedImage);
	}

	/**
	 * Iterates over the working image set and manipulates every image with the
	 * algorithms, which are set in the options
	 */
	public void manipulateAllImages() {
		AttackToolEnum attack = (AttackToolEnum) getOption(Options.OptionIdentifierEnum.Attack_Algorithm);
		SelectionToolEnum selection = (SelectionToolEnum) getOption(Options.OptionIdentifierEnum.Selection_Algorithm);
		workingImageSet.manipulateAllImages(
				(AbstractAttackAlgorithm) attackAlgoFactory
						.getAlgorithm(attack),
				(AbstractSelectionAlgorithm) selectionAlgoFactory
						.getAlgorithm(selection), options, getStatusBar());
	}

	/**
	 * Returns the AdvancedTextureImage which matches to a image in the working
	 * set
	 * 
	 * @param path
	 * @return AdvancedTextureImage or null if image with path is not in the
	 *         working set
	 */
	public AdvancedTextureImage getAdvancedTextureImageFromAbsolutePath(
			String path) {
		return workingImageSet
				.getAdvancedTextureImageFromAbsoluteFilePath(path);
	}

	/**
	 * Filters the images in the left preview scroll list with given filter
	 * string
	 * 
	 * @param filter
	 */
	public void filterPreviewList(String filter, boolean complement) {
		loadTexturesInPreviewList(filter, complement);
	}

	public Object getOption(OptionIdentifierEnum id) {
		return options.getOption(id);
	}

	/**
	 * Sets the icon of the given AdvancedTextureImage in the left preview
	 * scroll list
	 * 
	 * @param advImage
	 */
	public void setTextureIcon(AdvancedTextureImage advImage) {
		mainWindow.getContentContainer().getImageScrollPane().setIcon(advImage);
	}

	/**
	 * Manipulates the image which is selected in the left preview scroll list
	 * with the attack and select algo specified with the option object
	 */
	public void manipulateSelectedImage() {
		AttackToolEnum attack = (AttackToolEnum) getOption(Options.OptionIdentifierEnum.Attack_Algorithm);
		SelectionToolEnum selection = (SelectionToolEnum) getOption(Options.OptionIdentifierEnum.Selection_Algorithm);
		String imagePath = mainWindow.getContentContainer()
				.getImageScrollPane().getSelectedImageByPath();
		if (imagePath != null) {
			AdvancedTextureImage img = workingImageSet
					.getAdvancedTextureImageFromAbsoluteFilePath(imagePath);
			if (img.getState() != EditState.unsupported) {
				getStatusBar().setImageProgressParameter(1);
				getStatusBar().setImageProgress(1);
				img.processManipulation(
						(AbstractAttackAlgorithm) attackAlgoFactory
								.getAlgorithm(attack),
						(AbstractSelectionAlgorithm) selectionAlgoFactory
								.getAlgorithm(selection), options,
						getStatusBar());
				setTextureIcon(workingImageSet
						.getAdvancedTextureImageFromAbsoluteFilePath(imagePath));
				resetAlgorithms();
			}

		} else {
			JOptionPane.showMessageDialog(null,
					"No Texture selected on the left preview list");
		}
	}

	public StatusBar getStatusBar() {
		return mainWindow.getStatusBar();
	}

	/**
	 * Resets/Creates new the given algorithms in the factory
	 * 
	 * @param algoList
	 */
	public void resetAlgorithms() {
		attackAlgoFactory = new AttackAlgorithms(this);
		selectionAlgoFactory = new SelectionAlgorithms(this);
	}

	public AbstractAlgorithm getAlgorithm(Enum name) {
		if (attackAlgoFactory.contains(name)) {
			return attackAlgoFactory.getAlgorithm(name);
		} else if (selectionAlgoFactory.contains(name)) {
			return selectionAlgoFactory.getAlgorithm(name);
		}
		return null;
	}

	public void saveTexture() {
		TexturesDialog textureSaveDialog = new TexturesDialog(
				TexturesDialog.SAVE_DIALOG);
		if (textureSaveDialog.showDialog(null, "Save") == JFileChooser.APPROVE_OPTION) {
			workingImageSet.saveTexture(textureSaveDialog.getSelectedFile()
					.getAbsolutePath(), mainWindow.getContentContainer()
					.getImageScrollPane().getSelectedImageByPath());
		}
	}

	public void createListOfUnsupportedTextures() {
		ArrayList<String> unsupportedTexturePaths = workingImageSet
				.getUnsupportedTexturePaths();
		JFileChooser pathChooser = new JFileChooser(new File(
				PropertyUtils.properties.getProperty("lastDir")));
		pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (pathChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			IOUtils.writePathsToTextFile(
					unsupportedTexturePaths,
					pathChooser.getSelectedFile().getAbsolutePath()
							+ System.getProperty("file.separator")
							+ "unsupportedTextures.txt");
		}
	}

	public int getTextureCount() {
		return workingImageSet.getTextureCount();
	}

}
