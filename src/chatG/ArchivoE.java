package chatG;

/**
* @author CHIN PECH JESSICA J. ESTEFANÍA, 57039, 8A, ISC
* @author PÉREZ PÉREZ KAREN E., 57569, 8A, ISC
* @author RODRIGUEZ CAB OMAR J., 56964, 8A, ISC
* @author UC VAZQUEZ D. CAROLINA, 57618, 8A, ISC
* @since FEBRERO 2023
 */

public class ArchivoE {
	
	private int id;
	private String nombre;
	private byte[] data;
	private String extension;
	
	// CONSTRUCTOR
	public ArchivoE(int id, String nombre, byte[] data, String extension) {
		// super();
		this.id = id;
		this.nombre = nombre;
		this.data = data;
		this.extension = extension;
	}

	public int getId() {
		return id;
	}

	public void setId(int idArchivo) {
		this.id = idArchivo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}