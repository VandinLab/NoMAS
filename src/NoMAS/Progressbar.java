package NoMAS;

public class Progressbar {
	int width;

	//Constructor that specifies the width of the progress bar
	public Progressbar(int w) {
		width = w;
	}
	
	//Update method that redraws the progress bar
	public void update(double progressPercentage) {
		//The percentage string that is placed in the middle
		String percentage = " " + (int)(progressPercentage*100) + "% ";

		//Current position
		int currentPos = 0;

		//Start position where the percentage string should be written
		int start = (int)Math.ceil((width/2 - percentage.length()/2));

		System.err.print("\r[");
		int i = 0;

		//The position where the progress bar has progressed to
		int termValue = (int)(progressPercentage*width);

		//Write out #'s according to the progress
		for (; i < termValue; i++) {
			System.err.print("#");
			currentPos++;
			if (i >= start){
				break;
			}
		}

		//Write out any necessary white space before the middle
		while(currentPos < start + 1){
			System.err.print(" ");
			currentPos++;
		}

		//Write out the percentage string
		System.err.print(percentage);

		//Write out #'s occuring after the percentage
		int initValue = (int)(start + percentage.length());
		for (i = initValue; i <= termValue; i++) {
			System.err.print("#");
		}

		//Write out any necessary white space after the percentage
		for (; i < width; i++) {
			System.err.print(" ");
		}
		System.err.print("]");
	}
	
	public void finish() {
		System.err.print("\n");
	}
}