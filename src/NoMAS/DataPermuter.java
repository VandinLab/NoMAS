package NoMAS;
import java.util.*;

public abstract class DataPermuter {
	public Model model;
	public Random rng;
	
	public void initialize(Model model, Integer seed) {
		this.model = model;
		rng = (seed != null) ? new Random(seed) : new Random();
	}
	
	public abstract void permute();
	
	public static DataPermuter permuterFromName(String name) {
		if(name.equals("GI")) {
			return new IdentityPermuter();
		}
		if(name.equals("MS")) {
			return new SwitchPermuter();
		}
		System.err.println("No such mutations null model: "+name);
		System.exit(1);
		return null;
	}
}