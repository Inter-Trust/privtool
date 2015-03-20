package cat.urv.intertrust.main;
public class Consola{
	
	public String entrarString (String pregunta) { // Recopilar dades per retornar un String
		System.out.print(pregunta);
		char car=0; String nombre=""; 
		do {
			try {
				car = (char)System.in.read();
			} catch (java.io.IOException io){}
			if ((car != '\n')&&(car != '\r')) nombre+=(char)car;
		} while (car != '\n');
		return nombre;
	}
	
	// M�todes d'emmagatzematge de dades per retornar diferents tipus de valor.
	public int entrarInt(String pregunta) {
		try {return Integer.valueOf(entrarString(pregunta)).intValue();}
		catch (NumberFormatException nf) {return -1;}
	}
	
	public long entrarLong(String pregunta) {
		try {return Long.valueOf(entrarString(pregunta)).longValue(); }
		catch (NumberFormatException nf) {return -1;}
		
	}
	
	public float entrarFloat(String pregunta) {
		try {return Float.valueOf(entrarString(pregunta)).floatValue(); }
		catch (NumberFormatException nf) {return -1;}
	}
	
	public double entrarDouble(String pregunta) {
		try {return Double.valueOf(entrarString(pregunta)).doubleValue();}
		catch (NumberFormatException nf) {return -1;}
	}
}

