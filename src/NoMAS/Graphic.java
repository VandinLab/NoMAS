package NoMAS;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**
 * Class of utilities used in the realization of graphic render of the output of the algorithm
 * 
 * @author Federico Altieri
 * @author Tommy V. Hansen
 * @author Fabio Vandin
 *
 */
public class Graphic {
	public static final int MATRIX_CELL = 3;
	public static final int VERTEX_RADIUS = 12;
	public static final int VERTEX_DIAMETER = 2*VERTEX_RADIUS;
	public static final int NETWORK_RADIUS = 55;
	public static final int NETWORK_DIAMETER = 2*NETWORK_RADIUS;
	public static final int SOLUTION_WIDTH = NETWORK_DIAMETER+VERTEX_DIAMETER;
	public static final int LABEL_WIDTH = 180;
	public static final int GAP = 20;
	public static final int HEADER_HEIGHT = 110;
	public static final int HEADER_WIDTH = 800;
	public static final Color[][] MATRIX_COLOR = {
		{new Color(190, 230, 255), new Color(0, 100, 150)},
		{new Color(240, 240, 240), new Color(0, 0, 0)}
	};
	public static final Color POSITIVE_COLOR = new Color(100, 240, 85);
	public static final Color NEGATIVE_COLOR = new Color(235, 45, 45);
	
	/**
	 * Instantiates the objects for rendering and organizes the print of {@link Solution} instances.
	 * 
	 * @param filename {@link String} path to the file to write.
	 * @param model {@link Model} containing data.
	 * @param solutions {@link Solution} instances to render.
	 */
	public static void render(String filename, Model model, Solution... solutions) {
		int width = getWidth(model, solutions);
		int height = getHeight(model, solutions);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		draw(g, width, model, solutions);
		try {
			ImageIO.write(img, "png", new File(filename+".png"));
		}catch(Exception e) {}
	}
	
	/**
	 * Instantiates the objects for rendering and organizes the print of {@link Solution} instances when using the statistical verification with the holdout approach. 
	 * 
	 * @param filename {@link String} path to the file to write.
	 * @param train {@link Model} containing data about the training set.
	 * @param control {@link Model} containing data about the validation set.
	 * @param all {@link Model} containing all data.
	 * @param solutions array of {@link Solution} instances to render.
	 */
	public static void renderCrossval(String filename, Model train, Model control, Model all, Solution[] solutions) {
		int width = getWidth(all, solutions);
		int height = getHeight(all, solutions);
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		drawCrossval(g, width, train, control, all, solutions);
		try {
			ImageIO.write(img, "png", new File(filename+".png"));
		}catch(Exception e) {}
		}
	
	/**
	 * Coordinates the rendering of {@link Solution} instances when using the statistical verification with the holdout approach. 
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param width the width of the render space.
	 * @param train {@link Model} containing data about the training set.
	 * @param control {@link Model} containing data about the validation set.
	 * @param all {@link Model} containing all data.
	 * @param solutions array of {@link Solution} instances to render.
	 */
	public static void drawCrossval(Graphics2D g, int width, Model train, Model control, Model all, Solution[] solutions) {
		g.translate(GAP, GAP);
		g.setColor(Color.BLACK);
		drawHeader(g, train);
		g.translate(350, 0);
		drawLegend(g);
		g.translate(-350, HEADER_HEIGHT);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
		for(int i = 0; i < solutions.length; i++) {
			g.translate(0, GAP);
			int height = SOLUTION_WIDTH + (solutions[i].vertices.size()+2)*MATRIX_CELL + GAP;
			g.drawRect(0, 0, width-(2*GAP), height+2*GAP);
			drawSolutionCrossVal(g, train, control, all, solutions[i]);
			g.translate(0, height+GAP);
		}
		
	}

	/**
	 * Renders the passed instance of {@link Solution} when using the statistical verification with the holdout approach. 
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param train {@link Model} containing data about the training set.
	 * @param control {@link Model} containing data about the validation set.
	 * @param all {@link Model} containing all data.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawSolutionCrossVal(Graphics2D g, Model train, Model control, Model all, Solution solution) {
		double[] contributions = Solution.contributions(train, solution); 
		AffineTransform state = g.getTransform();
		g.translate(GAP, GAP);
		drawSummaryCrossval(g, solution);
		g.translate(LABEL_WIDTH, 0);
		drawNetwork(g, train, solution, contributions);
		g.translate(SOLUTION_WIDTH+GAP, 0);
		drawLabelsCrossval(g, train, control, solution, contributions);
		g.translate(-SOLUTION_WIDTH-GAP-LABEL_WIDTH, SOLUTION_WIDTH+GAP);
		drawMatrixCrossval(g, all, solution);
		g.setTransform(state);
	}
	
	/**
	 * Draws the statistical summary of the passed instance of {@link Solution} when using the statistical verification with the holdout approach. 
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawSummaryCrossval(Graphics2D g, Solution solution) {
		AffineTransform state = g.getTransform();
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.translate(0, font_height/2);
		g.drawString("Log-rank: "+Utils.round(solution.lr, 4), 0, 0);
		g.drawString("Norm. log-rank: "+Utils.round(solution.nlr, 4), 0, font_height);
		g.drawString("p-value tr: "+solution.pv, 0, 2*font_height);
		g.drawString("p-value ctrl: "+solution.pcv, 0, 3*font_height);
		g.drawString("ntr: "+solution.m1, 0, 4*font_height);
		g.drawString("nctr: "+solution.m1cv, 0, 5*font_height);
		g.setTransform(state);
	}

	/**
	 * Computes the overall height of the output image.
	 * 
	 * @param model {@link Model} containing all data.
	 * @param solutions {@link Solution} instances to render.
	 * @return the height
	 */
	public static int getHeight(Model model, Solution... solutions) {
		int height = 3*GAP + HEADER_HEIGHT;
		for(Solution s : solutions) {
			height += (s.vertices.size()+2)*MATRIX_CELL + SOLUTION_WIDTH + 3*GAP;
		}
		return height;
	}
	
	/**
	 * Computes the overall width of the output image.
	 * 
	 * @param model {@link Model} containing all data.
	 * @param solutions {@link Solution} instances to render.
	 * @return the width 
	 */
	public static int getWidth(Model model, Solution... solutions) {
		int width = Math.max(4*GAP + model.m*MATRIX_CELL, 2*GAP+HEADER_WIDTH);
		for(Solution s : solutions) {
			int network_width = 5*GAP + SOLUTION_WIDTH + ((s.vertices.size()+11)/6)*LABEL_WIDTH;
			width = Math.max(width, network_width);
		}
		return width;
	}
	
	/**
	 * Coordinates the rendering of {@link Solution} instances.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param width the width of the render space.
	 * @param model {@link Model} instance containing data.
	 * @param solutions {@link Solution} instances to render.
	 */
	public static void draw(Graphics2D g, int width, Model model, Solution... solutions) {
		g.translate(GAP, GAP);
		g.setColor(Color.BLACK);
		drawHeader(g, model);
		g.translate(350, 0);
		drawLegend(g);
		g.translate(-350, HEADER_HEIGHT);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
		for(Solution s : solutions) {
			g.translate(0, GAP);
			int height = SOLUTION_WIDTH + (s.vertices.size()+2)*MATRIX_CELL + GAP;
			g.drawRect(0, 0, width-(2*GAP), height+2*GAP);
			drawSolution(g, model, s);
			g.translate(0, height+GAP);
		}
	}
	
	/**
	 * Renders the legend of symbols on top of the image.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 */
	public static void drawLegend(Graphics2D g) {
		AffineTransform state = g.getTransform();
		String[][] labels = {
			{"No mutation, Censored", "Mutation, Censored"},
			{"No Mutation, Uncensored", "Mutation Uncensored"}
		};
		g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.translate(0, font_height);
		for(int i=0; i<4; i++) {
			g.setColor(MATRIX_COLOR[i/2][i%2]);
			g.fillRect(0, -font_height+4, font_height, font_height);
			g.setColor(Color.BLACK);
			g.drawRect(0, -font_height+4, font_height, font_height);
			g.drawString(labels[i/2][i%2], font_height+GAP, 0);
			g.translate(0, font_height);
		}
		g.translate(220, -4*font_height);
		String[] labels2 = {"Positive contribution", "No contribution", "Negative contribution"};
		for(int i=0; i<3; i++) {
			g.setColor(vertexColor(1-i));
			g.fillOval(0, -font_height+4, font_height, font_height);
			g.setColor(Color.BLACK);
			g.drawOval(0, -font_height+4, font_height, font_height);
			g.drawString(labels2[i], font_height+GAP, 0);
			g.translate(0, font_height);
		}
		g.setTransform(state);
	}
	
	/**
	 * Renders the header of the output image, with all overall information.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing data.
	 */
	public static void drawHeader(Graphics2D g, Model model) {
		AffineTransform state = g.getTransform();
		g.setFont(new Font("TimesRoman", Font.PLAIN, 16));
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.drawString(""+model.graph_file, 0, font_height);
		g.drawString(""+model.matrix_file, 0, 2*font_height);
		g.drawString(model.n+" genes,  "+Graph.numberOfEdges(model)+" interactions", 0, 3*font_height);
		g.drawString(model.m+" patients", 0, 4*font_height);
		g.drawString(Mutations.numberOfMutations(model)+" mutations", 0, 5*font_height);
		
		
		g.setTransform(state);
	}
	
	/**
	 * Renders the passed instance of {@link Solution}.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing data.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawSolution(Graphics2D g, Model model, Solution solution) {
		double[] contributions = Solution.contributions(model, solution); 
		AffineTransform state = g.getTransform();
		g.translate(GAP, GAP);
		drawSummary(g, solution);
		g.translate(LABEL_WIDTH, 0);
		drawNetwork(g, model, solution, contributions);
		g.translate(SOLUTION_WIDTH+GAP, 0);
		drawLabels(g, model, solution, contributions);
		g.translate(-SOLUTION_WIDTH-GAP-LABEL_WIDTH, SOLUTION_WIDTH+GAP);
		drawMatrix(g, model, solution);
		g.setTransform(state);
	}
	
	/**
	 * Renders the subnetwork of the passed instance of {@link Solution}.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing data.
	 * @param solution {@link Solution} instance to render.
	 * @param contributions the array with the contribution of every node to the score of the subnetwork.
	 */
	public static void drawNetwork(Graphics2D g, Model model, Solution solution, double[] contributions) {
		AffineTransform state = g.getTransform();
		int[] x = new int[solution.vertices.size()];
		int[] y = new int[solution.vertices.size()];
		for(int i=0; i<solution.vertices.size(); i++) {
			double angle = i*2*Math.PI/solution.vertices.size();
			x[i] = (int)(NETWORK_RADIUS*Math.cos(angle)) + SOLUTION_WIDTH/2 - VERTEX_RADIUS;
			y[i] = (int)(NETWORK_RADIUS*Math.sin(angle)) + SOLUTION_WIDTH/2 - VERTEX_RADIUS;
		}
		for(int i=0; i<solution.vertices.size(); i++) {
			for(Vertex v : solution.vertices.get(i).neighbors) {
				int v_index = getIndex(solution, v);
				if(v_index != -1) {
					g.drawLine(x[i]+VERTEX_RADIUS, y[i]+VERTEX_RADIUS, x[v_index]+VERTEX_RADIUS, y[v_index]+VERTEX_RADIUS);
				}
			}
		}
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		for(int i=0; i<solution.vertices.size(); i++) {
			g.setColor(vertexColor(contributions[i]));
			g.fillOval(x[i], y[i], VERTEX_DIAMETER, VERTEX_DIAMETER);
			g.setColor(Color.BLACK);
			int font_width = metrics.stringWidth(""+(i+1));
			g.drawString(""+(i+1), x[i]+(VERTEX_DIAMETER-font_width)/2, y[i]+(VERTEX_RADIUS+font_height)/2);
			g.drawOval(x[i], y[i], VERTEX_DIAMETER, VERTEX_DIAMETER);
		}
		g.setTransform(state);
	}
	
	/**
	 * Returns a color depending on the contribution of the node to the score of the subnetwork. Returns white if zero.
	 * 
	 * @param contribution the contribution of the node to the score of the subnetwork.
	 * @return the instance of {@link Color} to be colored
	 */
	public static Color vertexColor(double contribution) {
		if(contribution > 0.0) {
			return POSITIVE_COLOR;
		}else if(contribution < 0.0) {
			return NEGATIVE_COLOR;
		}
		return Color.WHITE;
	}
	
	/**
	 * Looks for a vertex and returns its index within the passed instance of {@link Solution}.
	 * 
	 * @param solution {@link Solution} instance.
	 * @param v {@link Vertex} instance to look for.
	 * @return index of the vertex in the solution.
	 */
	public static int getIndex(Solution solution, Vertex v) {
		for(int i=0; i<solution.vertices.size(); i++) {
			if(solution.vertices.get(i) == v) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Renders the labels of the nodes of the subnetwork of an instance of {@link Solution}.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing data.
	 * @param solution {@link Solution} instance to render.
	 * @param contributions the array with the contribution of every node to the score of the subnetwork.
	 */
	public static void drawLabels(Graphics2D g, Model model, Solution solution, double[] contributions) {
		AffineTransform state = g.getTransform();
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.translate(0, font_height/2);
		for(int i=0; i<solution.vertices.size(); i++) {
			if(i>0 && i%6 == 0) {
				g.translate(LABEL_WIDTH, -6*font_height);
			}
			g.drawString("("+(i+1)+") "+model.genes[solution.vertices.get(i).id].symbol+"   "+contributions[i], 0, 0);
			g.translate(0, font_height);
		}
		g.setTransform(state);
	}
	
	/**
	 * Renders the labels of the nodes of the subnetwork of an instance of {@link Solution} when using the statistical verification with the holdout approach.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param train {@link Model} instance containing training data.
	 * @param control {@link Model} instance containing validation data.
	 * @param solution {@link Solution} instance to render.
	 * @param contributions the array with the contribution of every node to the score of the subnetwork.
	 */
	public static void drawLabelsCrossval(Graphics2D g, Model train, Model control, Solution solution, double[] contributions) {
		AffineTransform state = g.getTransform();
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.translate(0, font_height/2);
		for(int i=0; i<solution.vertices.size(); i++) {
			if(i>0 && i%6 == 0) {
				g.translate(LABEL_WIDTH, -6*font_height);
			}
			g.drawString("("+(i+1)+") "+train.genes[solution.vertices.get(i).id].symbol+"  "+contributions[i]+" ntr:"+train.genes[solution.vertices.get(i).id].m1+" nctrl:"+Graph.getVertexBySymbol(control, solution.vertices.get(i).gene.symbol).gene.m1, 0, 0);
			g.translate(0, font_height);
		}
		g.setTransform(state);
	}
	
	/**
	 * Renders statistical summary of the passed instance of {@link Solution}.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawSummary(Graphics2D g, Solution solution) {
		AffineTransform state = g.getTransform();
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		int font_height = metrics.getHeight();
		g.translate(0, font_height/2);
		g.drawString("Log-rank: "+Utils.round(solution.lr, 4), 0, 0);
		g.drawString("Norm. log-rank: "+Utils.round(solution.nlr, 4), 0, font_height);
		g.drawString("p-value: "+solution.pv, 0, 2*font_height);
		String ppv = (solution.ppv < 0) ? "< 0.01" : ""+solution.ppv;
		g.drawString("Perm. p-value: "+ppv, 0, 3*font_height);
		g.setTransform(state);
	}
	
	/**
	 * Renders the mutation matrix evidencing mutations in the passed instance of {@link Solution}.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing data.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawMatrix(Graphics2D g, Model model, Solution solution) {
		AffineTransform state = g.getTransform();
		for(int i=0; i<solution.vertices.size(); i++) {
			int[] x = solution.vertices.get(i).gene.x;
			for(int j=0; j<model.m; j++) {
				int mutation = Bitstring.getBit(x, j);
				g.setColor(MATRIX_COLOR[model.c[j]][mutation]);
				g.fillRect(j*MATRIX_CELL, 0, MATRIX_CELL, MATRIX_CELL);
			}
			g.translate(0, MATRIX_CELL);
		}
		g.translate(0, MATRIX_CELL);
		for(int j=0; j<model.m; j++) {
			int mutation = Bitstring.getBit(solution.x, j);
			g.setColor(MATRIX_COLOR[model.c[j]][mutation]);
			g.fillRect(j*MATRIX_CELL, 0, MATRIX_CELL, MATRIX_CELL);
		}
		g.setColor(Color.BLACK);
		g.setTransform(state);
	}
	
	/** 
	 * Renders the mutation matrix evidencing mutations in the passed instance of {@link Solution} when using the statistical verification with the holdout approach.
	 * 
	 * @param g The {@link Graphics2D} instance that performs the render.
	 * @param model {@link Model} instance containing the mutation matrix.
	 * @param solution {@link Solution} instance to render.
	 */
	public static void drawMatrixCrossval(Graphics2D g, Model model, Solution solution) {
		AffineTransform state = g.getTransform();
		for(int i=0; i<solution.vertices.size(); i++) {
			int[] x = (Graph.getVertexBySymbol(model, solution.vertices.get(i).gene.symbol)).gene.x;
//			int[] x = solution.vertices.get(i).gene.x;
			for(int j=0; j<model.m; j++) {
				int mutation = Bitstring.getBit(x, j);
				g.setColor(MATRIX_COLOR[model.c[j]][mutation]);
				g.fillRect(j*MATRIX_CELL, 0, MATRIX_CELL, MATRIX_CELL);
			}
			g.translate(0, MATRIX_CELL);
		}
		g.translate(0, MATRIX_CELL);
		for(int j=0; j<model.m; j++) {
			int mutation = Bitstring.getBit(solution.xall, j);
			g.setColor(MATRIX_COLOR[model.c[j]][mutation]);
			g.fillRect(j*MATRIX_CELL, 0, MATRIX_CELL, MATRIX_CELL);
		}
		g.setColor(Color.BLACK);
		g.setTransform(state);
	}

	
}