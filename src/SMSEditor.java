import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class SMSEditor {
	static ArrayList<String> contacts;
	static ArrayList<String> numbers;
	static BufferedReader br;
	static ArrayList<String> smsList; //to store the filtered sms messages
	
	public static void main(String[] args) throws IOException {
		Scanner input = new Scanner(System.in);		//init user input
		
		System.out.println("Enter path to sms file including filename and extension:");
		String filepath = input.nextLine();
		System.out.println("You have selected the file located at: " + filepath);
		
		//open input file
		filepath = filepath.replace("\\", File.separator);
		File file = new File(filepath);
		FileInputStream finstream;
		finstream = new FileInputStream(file);
		br = new BufferedReader(new InputStreamReader(finstream)); //used to read file
		
		//select program mode
		System.out.println("Enter 1 to remove duplicates from sms file, 2 to filter sms by contact name, 3 to filter sms by number:");
		int mode = Integer.parseInt(input.nextLine());
		if (mode == 1) smsList = removeDuplicates();
		else if (mode == 2) {
			System.out.println("Enter the contacts one line at a time to keep/discard: (enter 0 when done)");
			contacts = new ArrayList<String>();
			String contact = "1";
			while(!(contact = input.nextLine()).equals("0")) {
				contacts.add(contact);
			}
			System.out.println("Do you wish to keep these contacts (enter 1), or remove them (enter 2)?");
			int keep = Integer.parseInt(input.nextLine());
			boolean keepContact;
			if (keep == 1) keepContact = true;
			else keepContact = false;
			smsList = filterContact(keepContact);
		}
		else if (mode == 3) {
			System.out.println("Enter the numbers one line at a time to keep/discard: (enter 0 when done)");
			numbers = new ArrayList<String>();
			String number = "1";
			while(!(number = input.nextLine()).equals("0")) {
				numbers.add(number);
			}
			System.out.println("Do you wish to keep these numbers (enter 1), or remove them (enter 2)?");
			int keep = Integer.parseInt(input.nextLine());
			boolean keepNumber;
			if (keep == 1) keepNumber = true;
			else keepNumber = false;
			smsList = filterNumber(keepNumber);
		}
		//close input streams
		br.close();
		finstream.close();
		
		//output file info
		System.out.println("Enter name of output file (including extension): ");
		filepath = filepath.substring(0, filepath.lastIndexOf(File.separatorChar) + 1);
		String outFile = filepath + input.nextLine();
		System.out.println("The output is stored in: " + outFile);
		File fout = new File(outFile);
		FileOutputStream foutstream = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(foutstream));
		
		ArrayList<String> writelist = sortSMS(smsList);//arraylist of output (sorted)
		
		for (int z = 0; z < writelist.size(); z++) {
			bw.write(writelist.get(z));
			bw.newLine();
		}
		
		/*
		 * Use to write to file after adding to arrayList 
		 * bw.write(strLine);
		 * bw.newLine();
		 */
		input.close();
		// Close the output streams
		bw.close();
		foutstream.close();
		System.out.println("Number of records written: " + writelist.size());

	}


	static ArrayList<String> removeDuplicates() throws IOException {
		ArrayList<String> strList = new ArrayList<String>();
		String strLine, oldDate = "", newDate = "";
		int i = 0, w = 0;
		// Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			i += 1;
			int b = strLine.indexOf("date=") + 6;
			if (b >= 6) {
				int eId = strLine.indexOf('"', b);
				newDate = strLine.substring(b, eId);
				/*b = strLine.indexOf("contact_name=") + 14;
				eId = strLine.indexOf('"', b);
				contactName = strLine.substring(b, eId);
				b = strLine.indexOf("address=") + 9;
				eId = strLine.indexOf('"', b);
				number = strLine.substring(b, eId);*/
				if (!(oldDate.equals(newDate))) { 
					w += 1;
					strList.add(strLine);
				}
			}
			oldDate = newDate;
		}
		System.out.println("Number of records read: " + String.valueOf(i)
		+ ", Number of records kept: " + String.valueOf(w));
		return strList;
	}

	static ArrayList<String> filterContact(boolean keepContact) throws IOException {
		ArrayList<String> strList = new ArrayList<String>();
		String strLine, contactName = "";
		int i = 0, w = 0;
		// Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			i += 1;
			int b = strLine.indexOf("date=") + 6; //not needed for contact filtering but left to ensure proper sms entry
			if (b >= 6) {
				int eId = 0;
				b = strLine.indexOf("contact_name=") + 14;
				eId = strLine.indexOf('"', b);
				contactName = strLine.substring(b, eId);
				if (keepContact && matchContact(contactName) || (!keepContact && !matchContact(contactName))) { 
					w += 1;
					strList.add(strLine);
				}
			}
		}
		System.out.println("Number of records read: " + String.valueOf(i)
		+ ", Number of records kept: " + String.valueOf(w));
		return strList;
	}
	
	static ArrayList<String> filterNumber(boolean keepNumber) throws IOException {
		ArrayList<String> strList = new ArrayList<String>();
		String strLine, number = "";
		int i = 0, w = 0;
		// Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			i += 1;
			int b = strLine.indexOf("date=") + 6; //not needed for number filtering but left to ensure proper sms entry
			if (b >= 6) {
				int eId = 0;
				b = strLine.indexOf("address=") + 9;
				eId = strLine.indexOf('"', b);
				number = strLine.substring(b, eId);;
				if (keepNumber && matchNumber(number) || (!keepNumber && !matchNumber(number))) { 
					w += 1;
					strList.add(strLine);
				}
			}
		}
		System.out.println("Number of records read: " + String.valueOf(i)
		+ ", Number of records kept: " + String.valueOf(w));
		return strList;
	}
	
	
	static boolean matchContact(String s) {
		for (int i = 0; i < contacts.size(); i++) {
			if (s.indexOf(contacts.get(i)) >= 0 && s.length() == contacts.get(i).length()) { //strict matching used as separate contacts with similar names possible (for example Jon and Jonas)
				return true;
			}
		}
		return false;
	}

	static boolean matchNumber(String s) {
		for (int i = 0; i < numbers.size(); i++) {
			if (s.indexOf(numbers.get(i)) >= 0) { //note: strict number matching not used due to possible prefixes/suffixes (areacodes)
				return true;
			}
		}
		return false;
	}
	
	static ArrayList<String> sortSMS(ArrayList<String> l) {
		boolean sortMade = true;
		String date = "", oldDate = "";
		int b = 0, eId = 0;
		while (sortMade == true) {
			sortMade = false;
			for (int i = 1; i < l.size(); i++) {
				b = l.get(i - 1).indexOf("date=") + 6;
				eId = l.get(i - 1).indexOf('"', b);
				oldDate = l.get(i - 1).substring(b, eId);
				b = l.get(i).indexOf("date=") + 6;
				eId = l.get(i).indexOf('"', b);
				date = l.get(i).substring(b, eId);
				if (Long.parseLong(oldDate) > Long.parseLong(date)) {
					sortMade = true;
					Collections.swap(l, i, i - 1);
				}
			}
		}
		return l;
	}
}
