/**
 * This program uses K-means partitional clustering algorithm to analyze a data set.
 * Input the number of cluster centers you want, it will find out those centers' features 
 * and compute IV and EV.
 */
import java.io.*;
import java.util.*;

public class Kmeans {
	static final int featureNum=7;//how many features in the file
	static int numPoints;//number of data points
	static int numCentroids;//number of centroids
	static double pointsInfo[][];//store all the features of points
	static int[] centroidsIndices;//store the initial random indices of centroids
	static String title;//the title of dataset file
	
	public static void main(String[] args){
		System.out.println("Please specify the file name: ");
		try{
			title = getFileName();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//get the number of data points from the file
		try{
			numPoints = readPoints(title);
		}catch(Exception e){
			e.printStackTrace();
		}//end try-catch
		
		//put the data points of the file into the array pointsInfo
		pointsInfo = new double[numPoints][featureNum];
		try{
			initializePoints("D:/dataset.txt");
		}catch(Exception e){
			e.printStackTrace();
		}//end try-catch
		
		//get the number of centroids
		System.out.println("Please input the number of centroids: ");
		try{
			numCentroids = getNumCentroid();
		}catch(Exception e){
			e.getMessage();
		}//end try-catch
		
		//initialize the indices of centroids randomly
		centroidsIndices = new int[numCentroids];
		initializeCentroids();
		

		//get each centroid's features
		double[][] oldCentroidContent = new double[numCentroids][featureNum];
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<featureNum;j++){
				//since initially each centroid is picked randomly from the data set,
				//update each centroid's features simply from the data set.
				oldCentroidContent[i][j] = pointsInfo[centroidsIndices[i]][j];
			}
		}//end for
		
		//print starting centroids:
		System.out.println("Starting Centroids: ");
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<featureNum;j++){
				System.out.print(oldCentroidContent[i][j] + ", ");
			}
			System.out.println();
		}//end for
		
		//get each point's closest centroid for the first time
		double[][] pointMembershipContent = new double[numPoints][featureNum];//record each point's closest centroid's infomation
		for(int i=0;i<numPoints;i++){
			double shortestDistance = distance(i,0, pointsInfo, oldCentroidContent);
			int closestCluster = 0;
			for(int j=1; j<numCentroids; j++){
				if(shortestDistance>distance(i,j, pointsInfo, oldCentroidContent)){
					shortestDistance=distance(i,j,pointsInfo,oldCentroidContent);
					closestCluster = j;
				}//end if
			}//end for
			for(int k=0;k<featureNum;k++){
				pointMembershipContent[i][k] = oldCentroidContent[closestCluster][k];
			}
		}//end for
		
		//calculate new centroids' contents
		double newCentroidContent[][] = new double[numCentroids][featureNum];
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<featureNum;j++){
				newCentroidContent[i][j] = averageFeature(pointMembershipContent,oldCentroidContent,i,j);
			}
		}//end for
	
		//get each point's new centroid content.
		double[][] newPointMembershipContent = new double[numPoints][featureNum];
		for(int i=0;i<numPoints;i++){
			double shortestDistance = distance(i,0, pointsInfo, newCentroidContent);
			int closestCluster = 0;
			for(int j=1;j<numCentroids;j++){
				if(shortestDistance>distance(i,j, pointsInfo, newCentroidContent)){
					shortestDistance=distance(i,j,pointsInfo,newCentroidContent);
					closestCluster = j;
				}//end if
			}//end for

			for(int k=0;k<featureNum;k++){
				newPointMembershipContent[i][k] = newCentroidContent[closestCluster][k];
			}
		}//end for
		//repeat until no point changes its cluster centroid
		while(!same(pointMembershipContent, newPointMembershipContent)){
			for(int i=0;i<numPoints;i++){
				for(int j=0;j<featureNum;j++){
					pointMembershipContent[i][j] = newPointMembershipContent[i][j];
				}
			}//end for
			for(int i=0;i<numCentroids;i++){
				for(int j=0;j<featureNum;j++){
					oldCentroidContent[i][j] = newCentroidContent[i][j];
				}
			}//end for
			//update each centroid based on data points that are assigned to it. 
			for(int i=0;i<numCentroids;i++){
				for(int j=0;j<featureNum;j++){
					newCentroidContent[i][j] = averageFeature(pointMembershipContent,oldCentroidContent,i,j);
				}
			}//end for
			//update each point's new centroid
			for(int i=0;i<numPoints;i++){
				double shortestDistance = distance(i,0, pointsInfo, newCentroidContent);
				int closestCluster = 0;
				for(int j=1;j<numCentroids;j++){
					if(shortestDistance>distance(i,j, pointsInfo, newCentroidContent)){
						shortestDistance=distance(i,j,pointsInfo,newCentroidContent);
						closestCluster = j;
					}//end if
				}//end for
				for(int k=0;k<featureNum;k++){
					newPointMembershipContent[i][k] = newCentroidContent[closestCluster][k];
				}
			}//end for			
		}//end while
		
		System.out.println("Final Centroid: ");
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<featureNum;j++){
				System.out.print(String.format("%.3f", newCentroidContent[i][j])+", ");
			}
			System.out.println();
		}//end for

		double IV = computeIV(newPointMembershipContent, newCentroidContent);
		System.out.println("IV = " +String.format("%.3f", IV));
		double EV = computeEV(newPointMembershipContent)/numCentroids;
		System.out.println("EV = " + String.format("%.3f", EV));
		System.out.println("IV/EV = " + String.format("%.10f", IV/EV));
	}//end main
	
	//get the number of centroids from user
	public static int getNumCentroid() throws Exception{
			BufferedReader bfrd = new BufferedReader(new InputStreamReader(System.in));
			String input = bfrd.readLine();
			return Integer.parseInt(input);		
	}//end getNumCentroid
	
	//get the file name which stores the data information
	public static String getFileName()throws Exception{
		BufferedReader bfrd = new BufferedReader(new InputStreamReader(System.in));
		String input = bfrd.readLine();
		return input;
	}//end getFileName
	
	//calculate the the mean of features of data points that are assigned to one cluster center
	public static double averageFeature(double[][] pointMembershipContent, double[][] centroidContent, int centroidIndex, int feature){
		double sum=0;
		int size=0;
		for(int i=0;i<numPoints;i++){
			//find out each centroid's points and calculate average.
			if(equal(pointMembershipContent[i], centroidContent[centroidIndex])){
				sum += pointsInfo[i][feature];
				size++;
			}
		}//end for
		return sum/size;
	}//end averageFeature
	
	//compare if two arrays are the same
	public static boolean equal(double[] a, double[] b){
		for(int i=0;i<featureNum;i++){
			if(a[i]!=b[i])
				return false;
		}
		return true;
	}//end equal
	
	//compute IV of the final cluster
	public static double computeIV(double[][] pointMembershipContent, double[][] centroidContent){
		double IV=0;
		double[] IVCentroid = new double[numCentroids];
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<numPoints;j++){
				if(equal(pointMembershipContent[j],centroidContent[i])){
					IVCentroid[i] += distance(j, i, pointsInfo, centroidContent);
				}
			}
		}
		for(int i=0;i<numCentroids;i++)
			IV += IVCentroid[i];
		return IV;
	}//end computeIV
	
	//compute the EV of final cluster
	public static double computeEV(double[][] pointMembershipContent){
		double EV=0;
		for(int i=0;i<numPoints;i++){
			for(int j=i+1;j<numPoints;j++){
				if(!equal(pointMembershipContent[i],pointMembershipContent[j])){
					EV += distance(i, j, pointsInfo, pointsInfo);
				}//end if
			}
		}//end for
		return EV;
	}//end computeEV
	
	//compare if any point has changed cluster centroid.
	public static boolean same(double[][] newPointMembershipContent, double[][] oldPointMembershipContent){
		for(int i=0;i<numCentroids;i++){
			for(int j=0;j<featureNum;j++){
				if(newPointMembershipContent[i][j]!=oldPointMembershipContent[i][j])
					return false;
			}
		}
		return true;
	}//end same
	
	
	//calculate distance between two data points using Euclidean distance
		public static double distance(int i, int j, double[][] pointsA, double[][] pointsB){
			double distance = Math.sqrt((pointsA[i][0]-pointsB[j][0])*(pointsA[i][0]-pointsB[j][0])+
																 (pointsA[i][1]-pointsB[j][1])*(pointsA[i][1]-pointsB[j][1])+
																 (pointsA[i][2]-pointsB[j][2])*(pointsA[i][2]-pointsB[j][2])+
																 (pointsA[i][3]-pointsB[j][3])*(pointsA[i][3]-pointsB[j][3])+
																 (pointsA[i][4]-pointsB[j][4])*(pointsA[i][4]-pointsB[j][4])+
																 (pointsA[i][5]-pointsB[j][5])*(pointsA[i][5]-pointsB[j][5])+
																 (pointsA[i][6]-pointsB[j][6])*(pointsA[i][6]-pointsB[j][6]));
			return distance;
		}//end distance
		
	
	
	//randomly select centroids from the data set
	public static void initializeCentroids(){
		Random rand = new Random();
		for(int i=0;i<numCentroids;i++){
			centroidsIndices[i] = rand.nextInt(numPoints);
			//if two indices are the same, this is meaningless; so avoid this situation.
			while(equalToAnyOtherIndex(centroidsIndices[i], i)){
				centroidsIndices[i] = rand.nextInt(numPoints);
			}
		}
	}//end initializeCentroids
	
	//avoid same indices
	public static boolean equalToAnyOtherIndex(int k, int in){
		for(int i=1;i<numCentroids;i++){
			if(k == centroidsIndices[(in+i)%numCentroids])
				return true;
		}
		return false;
	}//end equalToAnyOtherIndex
	
	//store all data points' infomation into 2-D array
	public static void initializePoints(String title) throws IOException{
		int lineNum=0;
		FileReader fr = new FileReader(title);
		BufferedReader br = new BufferedReader(fr);
		String line = "";
		while((line = br.readLine())!=null){
			String[] tokenized = line.split("\t");
			for(int i=0;i<featureNum;i++){
				pointsInfo[lineNum][i] = Double.parseDouble(tokenized[i]);
			}//for
			lineNum++;
		}//while3
		br.close();
		fr.close();
	}//end initializePoints
	
	//retrieve data point infomation from text file
	public static int readPoints(String title) throws IOException{
		int lineNumber=0;
		FileReader fr = new FileReader(title);
		BufferedReader br = new BufferedReader(fr);
		String line="";
		while((line = br.readLine())!=null){
			lineNumber++;
		}
		br.close();
		fr.close();
		return lineNumber;
	}//end readPoints
}//end class
