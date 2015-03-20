package cat.urv.intertrust.data;
import java.io.*;
import java.util.*;


public class Utilities {

	
    /**
     * Sort a map by values in ascending order keeping the duplicate entries.
     * @param map map to be sorted.
     */
	public static Map sortByAscendingValues(Map unsortMap) {
		 
        List list = new LinkedList(unsortMap.entrySet());
 
        //sort list based on comparator
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
	           return ((Comparable) ((Map.Entry) (o1)).getValue())
	           .compareTo(((Map.Entry) (o2)).getValue());

             }
        });
 
        //put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
   }
	
	
	/**
     * Sort a map by values in descending order keeping the duplicate entries.
     * @param map map to be sorted.
     */
	public static Map sortByDescendingValues(Map unsortMap) {
		 
        List list = new LinkedList(unsortMap.entrySet());
 
        //sort list based on comparator
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
	           return -((Comparable) ((Map.Entry) (o1)).getValue())
	           .compareTo(((Map.Entry) (o2)).getValue());

             }
        });
 
        //put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
   }
	
	
	
	/**
	 * A numeric attribute can be integer or double. This method checks if 
	 * the attribute is integer.
	 * @param value the value to be checked.
	 * @return TRUE if the value is a String representation of an integer
	 */
	public static boolean isNumericInteger(String value){
		try {
			Integer.valueOf(value);
			return true;
		}catch(NumberFormatException e) {
			return false;
		}
	}

	
    
	
	public static double min(List<Double> list) {
		Collections.sort(list);
		return list.get(0);
	}
	
	public static double max(List<Double> list) {
		Collections.sort(list);
		return list.get(list.size()-1);
	}
	
	public static List<Double> listStringToDouble (List<String> list) {
		List<Double> numericValues = new ArrayList<Double>();
		for (String s: list){
			numericValues.add(Double.valueOf(s));
		}
		return numericValues;
	}
	
	public static boolean isStringListEqual (List<String> list) {
		String s = list.get(0);
		for (String a : list) {
			if (!a.equalsIgnoreCase(s)) return false;
		}
		return true;
	}
	
	public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v1 instanceof Comparable) {
                    return -((Comparable) v1).compareTo(v2);
                }
                else {
                    return 0;
                }
            }
        });
        return keys;
    }
	
	//Metodo para leer toda una dataset y pasarlo a un formato EXCEL
	//Formato EXCEL --> atributos separados por TABULACIONES.
	public static void fileConversor (String fileIni, String fileEnd) {
        String record = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileIni));
            FileWriter fw = new FileWriter(fileEnd);
            String line = br.readLine(), attr = "";
                        
            StringTokenizer str = null;
            
            while (line != null) {
                str = new StringTokenizer(line, ":,;");
                record = "";
                while (str.hasMoreTokens()) {
                    attr = str.nextToken().trim();
                    record+=attr;
                    if (str.hasMoreTokens()) record+="\t";
                }
                fw.write(record + "\n");
                
                line = br.readLine();
            }
            
            fw.close();
            br.close();
            
            System.out.println("File Conversion completed.");
        } catch (IOException io) {
            io.printStackTrace();
            System.out.println("Error at conversing File.");
        }
        
    }
	
	public static void fileIVEFusion (int k, File dirK, int numDatasets) {
		try {
			BufferedWriter iveWriter = new BufferedWriter(new FileWriter (dirK + "/IVEwareFinal.set"));
			
			for (int i = 0; i < numDatasets; i++) {
				BufferedReader iveReader = new BufferedReader(new FileReader(dirK + "/Dataset " + i + "/IVEware.set"));
				String line = null;
				
				while ((line=iveReader.readLine()) != null) {
					iveWriter.write(line);
					iveWriter.newLine();
				}
				iveReader.close();
				iveWriter.newLine();
			}
			
			iveWriter.close();
			System.out.println("Fusion of IVEfiles correctly executed.");
		} catch (IOException io) {
			System.out.println("Error in Java I/O");
		}
	
	}
}

