package nubisave.web.interfaces;

public interface Browser {
	
	public void initialize();
	
	public void setEngine();
	
	public void setLayout();
	
	public void setStyle();
	
	public void browseTo(String url);
	
	public void reloadPage();
	
}
