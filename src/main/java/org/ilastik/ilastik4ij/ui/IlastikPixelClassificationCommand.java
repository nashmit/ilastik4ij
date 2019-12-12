/**
 * MIT License
 * <p>
 * Copyright (c) 2017 ilastik
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * Author: Carsten Haubold
 */
package org.ilastik.ilastik4ij.ui;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import org.ilastik.ilastik4ij.executors.PixelClassification;
import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.File;
import java.io.IOException;

import static org.ilastik.ilastik4ij.executors.AbstractIlastikExecutor.*;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>ilastik>Run Pixel Classification Prediction")
public class IlastikPixelClassificationCommand implements Command {

    @Parameter
	public LogService logService;

    @Parameter
	public StatusService statusService;

    @Parameter
	public OptionsService optionsService;

    @Parameter
	public UIService uiService;

    @Parameter(label = "Trained ilastik project file")
    public File projectFileName;

    @Parameter(label = "Raw input image")
	public Dataset inputImage;

    @Parameter(label = "Output type", choices = { UiConstants.PIXEL_PREDICTION_TYPE_PROBABILITIES, UiConstants.PIXEL_PREDICTION_TYPE_SEGMENTATION }, style = "radioButtonHorizontal")
	public String pixelClassificationType = UiConstants.PIXEL_PREDICTION_TYPE_PROBABILITIES;

	@Parameter(type = ItemIO.OUTPUT)
	private ImgPlus<? extends NativeType<?>> predictions;

	public IlastikOptions ilastikOptions;

	/**
     * Run method that calls ilastik
     */
    @Override
    public void run() {

    	if ( ilastikOptions == null )
			ilastikOptions = optionsService.getOptions(IlastikOptions.class);

		try
		{
			runClassification();
		} catch ( IOException e )
		{
			e.printStackTrace();
		}
    }

	private void runClassification() throws IOException
	{
		final PixelClassification pixelClassification = new PixelClassification( ilastikOptions.getExecutableFile(), projectFileName, logService, statusService, ilastikOptions.getNumThreads(), ilastikOptions.getMaxRamMb() );

		predictions = pixelClassification.classifyPixels( inputImage.getImgPlus(), PixelPredictionType.valueOf( pixelClassificationType ) );

		showOutput();
	}

	private void showOutput()
	{
		if ( ! uiService.isHeadless() )
		{
			ImageJFunctions.show( ( ImgPlus ) predictions );

			if ( pixelClassificationType.equals( "Segmentation" ) )
			{
				DisplayUtils.applyGlasbeyLUT();
			}
		}
	}
}
