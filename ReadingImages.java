import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.ldap.LdapContext;

import java.util.Properties;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class ReadingImages {

	//Path to file that contains classes and colors that correspond to them
	static final String color_file = "/home/nefeli/workspace/eclipse/roula/src/color150info.txt";
	//Files representing the folders that contain 1.colors, 2.traversable and 3.non-traversable images
//	static final File color_dir = new File("/home/nefeli/workspace/eclipse/roula/src/color150/");
//	static final File my_dir = new File("/home/nefeli/workspace/eclipse/roula/src/colors/");
	static final File trav_dir = new File("/home/nefeli/workspace/eclipse/roula/src/yes/");
	static final File obst_dir = new File("/home/nefeli/workspace/eclipse/roula/src/no/");	
	//Map rgb colors to 1.classes, 2.number of traversable and 3. number of non-traversable instances
	static Map<String, String> rgbToClass = new HashMap<String, String>();
//	static Map<List<Integer>, String> myMap = new HashMap<List<Integer>, String>();
	static Map<String, Integer> rgbToTrav = new HashMap<String, Integer>();
	static Map<String, Integer> rgbToObst = new HashMap<String, Integer>();
	
	static void readClasses() {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(color_file));
			String line =  null;
			
			while((line = br.readLine()) != null){
				String str[] = line.split("\t");
				for(int i = 0; i < str.length; i++) {
					rgbToClass.put(str[0], str[3].split(";")[0]);
				}
			}
			System.out.println(rgbToClass);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		//Load the OpenCV core library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Instantiating the Imagecodecs/Highgui class
//		Highgui imageCodecs = new Highgui();
		
		
		Mat matrix;
//		List<Integer> list;
		String colors;
		Set<String> hash_set = new HashSet<String>();
		//Set<List<Integer>> hash_set;
		
//		if (color_dir.isDirectory()) { // make sure it's a directory
//			for (File input_img : color_dir.listFiles()) {
//				matrix = Highgui.imread(input_img.getAbsolutePath());
//				Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);
////				System.out.println(input_img.getAbsolutePath());
//
//				list = new ArrayList<Integer>();
//				for(int k = 0; k < matrix.channels(); k++)
//					list.add((int) matrix.get(2,2)[k]);
////				System.out.println("list: " + list);
//				rgbToClass.put(list, input_img.getName().substring(0, (input_img.getName().indexOf("."))));
//			}
//		}
		
//		if (my_dir.isDirectory()) { // make sure it's a directory
//			for (File input_img : my_dir.listFiles()) {
//				matrix = Highgui.imread(input_img.getAbsolutePath());
////				System.out.println(input_img.getAbsolutePath());
//
//				list = new ArrayList<Integer>();
//				for(int k = 0; k < matrix.channels(); k++)
//					list.add((int) matrix.get(0,0)[k]);
////				System.out.println("list: " + list);
//				myMap.put(list, input_img.getName().substring(0, (input_img.getName().indexOf("."))));
//			}
//		}
		
//		System.out.println(Arrays.asList(numToColor));
//		System.out.println(Collections.singletonList(numToColor));
		
		readClasses();
		for (Entry<String, String> entry : rgbToClass.entrySet()) {
		    System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		
		
		if (trav_dir.isDirectory()) {
			for (File input_img : trav_dir.listFiles()) {
				matrix = Highgui.imread(input_img.getAbsolutePath());
				Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);
				for(int i = 0; i < matrix.rows(); i++) {
					for (int j = 0; j < matrix.cols(); j++) {
//						list = new ArrayList<Integer>();
						colors = "(";
						for(int k = 0; k < matrix.channels(); k++) {
//							list.add((int) matrix.get(i,j)[k]);
							colors += (int) matrix.get(i,j)[k];
							if (k != 2) colors += ", ";
							else colors += ")";
						}
						hash_set.add(colors);
					}
				}
//				System.out.println("hash_set size: " + hash_set.size());
				System.out.println("hash_set: " + hash_set);
				
				
				for (String temp : hash_set) {
					if (rgbToTrav.get(temp) != null) {
						rgbToTrav.put(temp, rgbToTrav.get(temp) + 1);
					}
					else {
						rgbToTrav.put(temp, 1);
					}
				}
				hash_set.clear();
			}
		}
		
		System.out.println("Done with traversable images");
		
		if (obst_dir.isDirectory()) {
			for (File input_img : obst_dir.listFiles()) {
				matrix = Highgui.imread(input_img.getAbsolutePath());
				Imgproc.cvtColor(matrix, matrix, Imgproc.COLOR_BGR2RGB);
				for(int i = 0; i < matrix.rows(); i++) {
					for (int j = 0; j < matrix.cols(); j++) {
//						list = new ArrayList<Integer>();
//						for(int k = 0; k < matrix.channels(); k++) {
//							list.add((int) matrix.get(i,j)[k]);
//						}
//						hash_set.add(list);
						colors = "(";
						for(int k = 0; k < matrix.channels(); k++) {
//							list.add((int) matrix.get(i,j)[k]);
							colors += (int) matrix.get(i,j)[k];
							if (k != 2) colors += ", ";
							else colors += ")";
						}
						hash_set.add(colors);
					}
				}	
				
				for (String temp : hash_set) {
					if (rgbToObst.get(temp) != null) {
						rgbToObst.put(temp, rgbToObst.get(temp) + 1);
					}
					else {
						rgbToObst.put(temp, 1);
					}
				}
				hash_set.clear();
			}
		}
		
		System.out.println("Done with non-traversable images\n");
		
//		System.out.println();
//		for (Entry<List<Integer>, String> entry : myMap.entrySet()) {
//			System.out.println(entry.getValue() + " : " + numToColor.get(entry.getKey()));
//		}
		
		System.out.println();
		for (Entry<String, Integer> entry : rgbToTrav.entrySet()) {
			if (rgbToObst.get(entry.getKey()) != null)
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
					+ " : trav percent -> " + entry.getValue() * 100 / (entry.getValue() + rgbToObst.get(entry.getKey())) + "%");
			else
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
				+ " : traversable -> " + entry.getValue());
		}
		
		for (Entry<String, Integer> entry : rgbToObst.entrySet()) {
			if (rgbToTrav.get(entry.getKey()) == null)
				System.out.println(entry.getKey() + " : " + rgbToClass.get(entry.getKey())  
				+ " : obstacle -> " + entry.getValue());
		}
		
	}
	
}
