import java.net.*;
import java.io.*;
import java.util.*; 

class Client{

    static String serverIp = "10.6.15.118";
    //static int serverPort = 2222;
    
    static String zonalServerIp = null; //no information initially
    //static int zonalServerPort = 3333;
    
    static String gridNodeIp = null;//no information initially
    //static int gridNodePort = 4444;


    // function to do the compute use case
    void compute(String fileName){
    }

    //function to do the join use case
    void join(){
    }
    

    public static void main(String args[]){
	System.out.println("Vishwa Based Campus Cloud");
	System.out.println("User Options");
	System.out.println("1.Join the Grid");
	System.out.println("2. Avail the compute facility");
	System.out.println("3. Quit");
	System.out.println("Enter your choice");
	// TODO Get input 

	if( ch ==1){
	    join();
	}
	else if(ch ==2){
	    System.out.println("Give the file name which has to be put in the grid for computation");
	}
	else{

	    //TODO Exit from the program
	}
    }
}