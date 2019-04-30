// In order to run this program you should have installed OpenCV 2.4 (compatible with 
// Highgui instead of Imgcodecs). Also, there should be four directories containing images
// exported from a neural network for semantic segmentation. "trainTrav" and "trainObst" 
// with the traversable and not-traversable images you want to use for training. "evalTrav" 
// and "evalObst" with the traversable and not-traversable images to use for evaluation.

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

// Use java-8-openjdk library
import javafx.util.Pair;

public class TravEstimation {

	// Path to file that contains classes and colors that correspond to them
	static final String color_file = "<path>/color150info.txt";
	// Files representing the directories that contain 1-3.traversable and 2-4.non-traversable images 
	// for 1-2.training and 3-4.evaluation
	static final File train_trav_dir = new File("<path>/trainTrav/");
	static final File train_obst_dir = new File("<path>/trainObst/");
	static final File eval_trav_dir = new File("<path>/evalTrav/");
	static final File eval_obst_dir = new File("<path>/evalObst/");
	// Map RGB colors to 1.classes, 2.number of traversable and 3. number of non-traversable instances
	static Map<String, String> rgbToClass = new HashMap<String, String>();
	static Map<String, Integer> rgbToTrav = new HashMap<String, Integer>();
	static Map<String, Integer> rgbToObst = new HashMap<String, Integer>();
	
    private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	
	// This method has nothing to do with the effectiveness of the program. It exists only for 
	// us to understand easier the way the computer deals with each image and the colors in it.
	static void readClasses() {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(color_file));
			String line =  null;
			
			while ((line = br.readLine()) != null) {
				String str[] = line.split("\t");
				// Keep RGB images and their corresponding classes
				for (int i = 0; i < str.length; i++)
					rgbToClass.put(str[0], str[3].split(";")[0]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Get all RGB colors present in an image
	static Set<String> getRGBset(File img) {
		Mat matrix;
		String rgb;
		Set<String> rgb_set = new HashSet<String>();
		
		// OpenCV reads image in BGR order
		matrix = Highgui.imread(img.getAbsolutePath());
		// Convert to RGB order to be compatible with the file containing the classes
		Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);
		// Keep all colors found in the image
		for (int i = 0; i < matrix.rows(); i++) {
			for (int j = 0; j < matrix.cols(); j++) {
				rgb = "(";
				for (int k = 0; k < matrix.channels(); k++) {
					rgb += (int) matrix.get(i,j)[k];
					if (k != 2) rgb += ", ";
					else rgb += ")";
				}
				rgb_set.add(rgb);
			}
		}
		
		return rgb_set;
	}
	
	
	static void train(File dir, Map<String, Integer> map) {
		
		if (dir.isDirectory()) {
			for (File img : dir.listFiles()) {
				Set<String> rgb_set = getRGBset(img);
				
				// Train
				// In map: increase by one the instances of each color found in the image
				for (String temp : rgb_set) {
					if (map.get(temp) != null) {
						map.put(temp, map.get(temp) + 1);
					}
					else {
						map.put(temp, 1);
					}
				}
			}
		}
	}
	
	
	static Pair<Integer, Integer> eval(File dir) {
		// Count traversable images for two calculating methods
		int trav_count = 0, trav_count2 = 0;
		
		if (dir.isDirectory()) {
			for (File img : dir.listFiles()) {
				Set<String> rgb_set = getRGBset(img);
				
				// Evaluate
				double sum_of_trav_perc = 0; 
				int sum_of_trav = 0, sum_of_obst = 0;
				Integer trav, obst;

				// For each image:
				// Summarize all RGB colors' traversable instances
				// Summarize all RGB colors' non-traversable instances
				// Summarize all RGB colors' traversable percentages
				for (String temp : rgb_set) {
					trav = rgbToTrav.get(temp);
					obst = rgbToObst.get(temp);
					
					if (trav != null && obst != null) {
						sum_of_trav_perc += (double) trav * 100 / (double) (trav + obst);
						sum_of_trav += trav;
						sum_of_obst += obst;
					}
					else if (trav != null && obst == null) {
						sum_of_trav_perc += 100;
						sum_of_trav += trav;
					}
					else if (trav == null && obst != null) {
						sum_of_obst += obst;
					}
				}
				// Method 1: Average = traversable percentages of all RGB colors / total of RGB colors 
				double avg = sum_of_trav_perc / (double) rgb_set.size();
				// Method 2: Average = traversable instances of all RGB colors / instances of all RGB colors
				double avg2 = (double) sum_of_trav * 100 / (double) (sum_of_trav + sum_of_obst);

				// If the average is more than 50% count the image as traversable
				if (avg >= 50) trav_count++; 
				if (avg2 >= 50) trav_count2++;
			}
		}
		
		return new Pair<Integer, Integer>(trav_count, trav_count2);
	}
	
	
	public static void main(String[] args) {
		// Load the OpenCV core library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		readClasses();
		
		train(train_trav_dir, rgbToTrav);
		System.out.println("System trained with traversable images");
		
		train(train_obst_dir, rgbToObst);
		System.out.println("System trained with non-traversable images\n");
		
		// Print traversability percentages, traversable and obstacle instances of each RGB color (or class label)
		for (Entry<String, Integer> entry : rgbToTrav.entrySet()) {
			if (rgbToObst.get(entry.getKey()) != null)
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
					+ " : trav percent -> " + df2.format((double) entry.getValue() * 100 / (double) (entry.getValue() 
					+ rgbToObst.get(entry.getKey()))) + "%\t" + "traversable -> " + entry.getValue() 
					+ " & obstable -> " + rgbToObst.get(entry.getKey()));
			else
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
				+ " : traversable -> " + entry.getValue());
		}
		
		for (Entry<String, Integer> entry : rgbToObst.entrySet()) {
			if (rgbToTrav.get(entry.getKey()) == null)
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
				+ " : obstacle -> " + entry.getValue());
		}
		
		System.out.println("\n");
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		};
		
		int trav_images = eval_trav_dir.listFiles(filter).length;
		int obst_images = eval_obst_dir.listFiles(filter).length;
		Pair<Integer, Integer> pair_of_trav = eval(eval_trav_dir);
		int correct_trav = pair_of_trav.getKey();
		int correct_trav2 = pair_of_trav.getValue();
		System.out.println("Way 1:");
		System.out.println(trav_images + " traversable images of which " + correct_trav + " found correctly"); 
		System.out.println("Success on traversable images: " + df2.format((double) correct_trav * 100 / 
				(double) trav_images) + "%\n");
		
		System.out.println("Way 2:");
		System.out.println(trav_images + " traversable images of which " + correct_trav2 + " found correctly"); 
		System.out.println("Success on traversable images: " + df2.format((double) correct_trav2 * 100 / 
				(double) trav_images) + "%\n");
		
		
		Pair<Integer, Integer> pair_of_obst = eval(eval_obst_dir);
		int correct_obst = obst_images - pair_of_obst.getKey();
		int correct_obst2 = obst_images - pair_of_obst.getValue();
		System.out.println("Way 1:");
		System.out.println(obst_images + " obstacle images of which " + correct_obst + " found correctly"); 
		System.out.println("Success on non-traversable images: " + df2.format((double) correct_obst * 100 / 
				(double) obst_images) + "%\n");
		
		System.out.println("Way 2:");
		System.out.println(obst_images + " obstacle images of which " + correct_obst2 + " found correctly"); 
		System.out.println("Success on non-traversable images: " + df2.format((double) correct_obst2 * 100 / 
				(double) obst_images) + "%\n");
		
		
		System.out.println("Way 1:");
		System.out.println("Total success: " + df2.format((double) (correct_trav + correct_obst) * 100 / 
				(double) (trav_images + obst_images)) + "%");
		System.out.println("Way 2:");
		System.out.println("Total success: " + df2.format((double) (correct_trav2 + correct_obst2) * 100 / 
				(double) (trav_images + obst_images)) + "%");
	}
	
}
